package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetMealMapper setmealMapper;

    /**
     * 根据时间区间查询营业数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        /*
          营业额：当日已完成订单的总金额
          有效订单：当日已完成订单的数量
          订单完成率：有效订单数 / 总订单数
          平均客单价：营业额 / 有效订单数
          新增用户：当日新增用户的数量
         */

        // 封装为map便于查询数据库
        Map<Object, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);

        // 查询总订单数目
        Integer totalOrderCount = orderMapper.statisticsOrders(map);
        // 查询营业额
        map.put("status", Orders.COMPLETED);
        Double turnover = orderMapper.sumAmount(map);
        turnover = turnover == null ? 0.0 : turnover;
        // 查询有效订单数
        Integer validOrderCount = orderMapper.statisticsOrders(map);
        validOrderCount = validOrderCount == null ? 0 : validOrderCount;

        // 计算完单率和平均客单价
        Double unitPrice = 0.0;
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0 && validOrderCount != 0) {
            // 完单率
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            // 客单价
            unitPrice = turnover / validOrderCount;
        }

        // 新增用户数
        Integer newUsers = userMapper.countUsersByTime(map);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    /**
     * 查询订单管理数据
     *
     * @return
     */
    @Override
    public OrderOverViewVO getOrderView() {
        Map<Object, Object> map = new HashMap<>();
        map.put("begin", LocalDateTime.now().with(LocalTime.MIN));

        // 待接单订单
        map.put("status", Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = orderMapper.statisticsOrders(map);

        // 待派送订单
        map.put("status", Orders.CONFIRMED);
        Integer deliveryOrders = orderMapper.statisticsOrders(map);

        // 已完成订单
        map.put("status", Orders.COMPLETED);
        Integer completedOrders = orderMapper.statisticsOrders(map);

        // 已取消订单
        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.statisticsOrders(map);

        // 全部订单
        map.put("status", null);
        Integer allOrders = orderMapper.statisticsOrders(map);

        // 封装为VO对象返回
        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveryOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    @Override
    public DishOverViewVO getDishOverView() {
        // 菜品总览需要查询已起售的菜品数量和已停售的菜品数量
        Map<Object, Object> map = new HashMap<>();
        // 查询已起售的菜品
        map.put("status", StatusConstant.ENABLE);
        Integer sold = dishMapper.countByStatus(map);
        // 查询未起售的菜品
        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = dishMapper.countByStatus(map);

        // 封装成对应的VO返回
        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    @Override
    public SetmealOverViewVO getSetmealOverView() {
        // 套餐总览和菜品总览类似，需要查询起售的套餐和未起售的套餐
        Map<Object, Object> map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByStatus(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByStatus(map);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
