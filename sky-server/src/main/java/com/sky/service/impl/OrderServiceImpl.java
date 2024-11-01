package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        if (shoppingCartList == null || !shoppingCartList.isEmpty()) {
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
}
