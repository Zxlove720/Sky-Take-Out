package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private UserMapper userMapper;

	@Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 提交订单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 先处理异常情况（收获地址为空、购物车为空），可以根据OrdersSubmitDTO对象传递的数据进行查询操作
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            // 此时根据OrdersSubmitDTO中的地址id查询，是查询不到该地址的，所以说抛出地址为空异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 获取这次提交订单请求的用户id
        Long userId = BaseContext.getCurrentId();
        // 创建ShoppingCart对象并封装UserId，便于Mapper查询
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);

        // 查询当前提交用户的购物车数据
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            // 若当前提交订单的用户的购物车为null或没有商品，那么肯定是无法正常提交订单的，抛出购物车为空异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 当异常处理完毕后，就可以正常进入业务了
        // 首先需要将DTO对象封装为Order对象
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        // 拷贝之后的封装，必须特别小心，必须仔细对比二者的差异，然后进行封装
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        // TODO这种使用时间戳作为订单编号的方法理论上是有问题的（如果两个用户真的碰巧同一时间下单）则会出现重大业务问题
        // 用当前时间的时间戳当作订单编号，需要把Long转换为String
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(userId);
        // 设置该订单的支付状态
        order.setStatus(Orders.PENDING_PAYMENT);
        // 设置订单的支付方式
        order.setPayStatus(Orders.UN_PAID);
        // 设置下单时间
        order.setOrderTime(LocalDateTime.now());

        // 向订单表中插入这个订单，相当于提交成功
        orderMapper.insert(order);

        // 订单明细数据
        // 一个订单中也许会有大量明细，所以说需要用一个集合封装
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            // 因为订单明细主要存储的是商品的明细，所以说可以直接将购物车拷贝到OrderDetail对象中
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            // 给订单明细对象封装订单id
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }

        // 直接向明细表中插入n条数据
        orderDetailMapper.insertBatch(orderDetailList);
        // 提交订单后，需要清空该用户的购物车数据
        shoppingCartMapper.deleteByUerId(userId);

        // 封装返回的VO

        return OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getByOpenid(String.valueOf(userId));

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 历史订单查询
     *
     * @param ordersPageQueryDTO@return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 使用PageHelper分页插件
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        // 创建一个集合用于存储订单
        List<OrderVO> list = new ArrayList<>();
        // 查询订单的明细，并封装为OrderVO对象进行响应
        if (page != null && page.getTotal() > 0) {
            // 说明这个用户是有历史订单的
            for (Orders orders : page) {
                // 获取订单id
                Long orderId = orders.getId();
                // 查询订单明细
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
                // 创建OrderVO对象
                OrderVO orderVO = new OrderVO();
                // 将orders中的属性拷贝给VO对象
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);

    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO selectDetails(Long id) {
        // 根据id查询订单
        Orders order = orderMapper.getById(id);
        // 根据该订单的id查询对应的明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(order.getId());
        // 将订单明细封装为OrderVO返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    @Override
    public void userCancelById(Long id) throws Exception{
        // 先根据id查询订单
        Orders orderDelete = orderMapper.getById(id);
        // 检验该订单是否存在
        if (orderDelete == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 查询订单状态 1.待付款 2.待接单 3.已接单 4.派送中 5.已完成 6.已取消
        if (orderDelete.getStatus() > 2) {
            // 只能在待付款或者待接单的状态下进行取消订单的操作
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(orderDelete.getId());

        // 若订单处于待接单的状态下取消，那么不能直接删除，需要先退款，然后再进行删除
        if (orderDelete.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            // 调用微信支付的退款接口
            weChatPayUtil.refund(
                    orderDelete.getNumber(), // 获取商品订单号
                    orderDelete.getNumber(), // 获取商品退款单号
                    orderDelete.getAmount(), // 退款金额，单位：元
                   orderDelete.getAmount()); // 原订单金额，单位：元
        }
        // 修改订单支付状态：退款
        orders.setPayStatus(Orders.REFUND);
        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(MessageConstant.USER_CANCEL_ORDER);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    public void repetition(Long id) {
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();
        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        // 将订单详情对象转换为购物车对象
        // 这里的实现是使用stream流遍历OrderDetail集合中的每一个OrderDetail对象
        // map方法接收一个方法作为参数，这个方法实现了将集合中的元素按照方法逻辑变化，此处还使用了lambda表达式
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            // 将原订单详情中的菜品信息复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id"); // 拷贝属性，但是忽略id（购物车和菜品详细的id是不同的）
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        // 将购物车对象批量添加到数据库中
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 订单分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 使用PageHelper分页插件辅助分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        // 部分订单状态，需要额外返回订单菜品信息，所以说将Orders对象转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);
        return new PageResult(page.getTotal(), orderVOList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();
        // 这是分页查询出来的所有的Orders订单信息，
        List<Orders> ordersList = page.getResult();
        // 先确保分页查询有结果
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将Orders对象封装为对应的OrderVO对象

                // 将Orders中和OrderVO相同的字段复制到OrderVO中
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                // 将菜品详细信息封装为一个字符串
                String orderDishes = getOrderDishesStr(orders);
                // 将订单菜品信息封装到orderVO中，并添加到orderVOList返回
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
    return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询菜品的详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        // 将每一条订单菜品信息拼接为字符串（如：宫保鸡丁 * 3）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + " * " + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起返回
        // 将orderDishList中的元素以空字符串（""）作为连接符连接成一个字符串。
        return String.join("", orderDishList);
    }

    /**
     * 各状态的订单数量统计
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        // 根据不同的订单状态，分别查询待接单、待派送、派送中的订单数量
        // 待接单
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        // 待派送
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        // 派送中
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        // 将查询出的不同状态订单的数据封装到OrderStatisticsVO对象中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 接单
     *
     * @param ordersConfirmDTO
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        // 接单的逻辑其实就是将订单的状态改为CONFIRMED
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }
}
