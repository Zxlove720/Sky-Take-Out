package com.sky.task;


import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


// SpringTask是Spring框架提供的任务调度工具，可以按照设置的时间自动执行某个代码逻辑
// 定位：定时任务框架
// 作用：定时自动执行某段Java代码

// 为什么需要使用SpringTask
// --需要对订单状态进行定时处理，假如有实际上已经完成了，但是用户没有点击完成的订单，就需要每天定时对这些订单修改状态或进行清理
// 只要是需要定时处理并且使用了Spring框架的场景都可以使用SpringTask

// cron表达式
// cron表达式的本质上是一个字符串，通过cron表达式可以定义任务的触发时间
// 构成规则：分为6个或者7个域，由空格分隔开，每个域都代表一个含义

// 每个域的含义分别是：秒、分钟、小时、日、月、周(这个周是指星期几)、年（年是可选的域，并非必须）
// *代表所有这个域的所有时间     ?代表这个域不知道具体时间
// 并且日和周的值不同时设置，其中一个若设置了，那么另一个使用"?"表示

// 通配符的含义：
// * 表示所有值   ? 表示未说明的值，即不关心它的值   - 表示一个指定的范围   , 表示附加一个可能值
// / 该符号之前表示开始时间，符号后表示每次递增的值，就可以用来表示多少时间进行一次

// 例子
// */5 * * * * ?表示每5s执行一次
// 0 */1 * * * ?表示每分钟执行一次
// 0 0 5-15 * * ?表示每天5-15点的整点执行
// 0 0-5 14 * * ?表示每天14点的0-5分钟，每分钟执行......
// 具体cron具体分析，若无法写出或无法看懂，那可以去https://cron.qqe2.com/在线cron表达式生成器生成cron或解析cron

// 如何使用SpringTask?
// 1.需要导入maven坐标：spring-context；这个框架是spring下的一个很小的框架
// 2.在启动类上添加注解@EnableScheduling开启任务调度
// 3.自定义定时任务类，编写cron表达式


/**
 * 基于SpringTask自定义定时任务，实现订单状态的定时处理
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    // 存在问题
    // 1.若用户下单后一直未支付，那么订单一直处于“待支付”状态
    // 2.用户实际上已经收到货，但管理端或者用户端没有完成该订单，那么订单将一直处于“派送中”状态

    // 处理逻辑
    // 1.通过SpringTask定时任务，每分钟检查是否已经存在支付超时的订单（下单后超过15min未支付则可以视为支付超时订单），
    // 如果存在则修改订单状态为“已取消”
    // 2.每天凌晨的打烊时间检查一次是否存在“派送中”的订单，如果存在则修改订单状态为“已完成”

    /**
     * 处理支付超时的订单
     */
    @Scheduled(cron = "0 * * * * ?") // 个人理解将其理解为每一个第0s，那么也就是每分钟
    public void processTimeoutOrder() {
        log.info("处理支付超时的订单：{}", new Date());
        // 设置时间为前15min，这个时间之前的订单若还处于未支付状态，那么就认为是超时订单，需要处理
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, time);
        // 因为是每天自动进行查询处理，所以说即使没有查询到结果（一般没有结果就是因为没有这种订单）也不需要抛出异常
        if (ordersList != null && !ordersList.isEmpty()) {
            // 但是要判断这种“问题订单”非空才进行下一步处理
            for (Orders orders : ordersList) {
                // 遍历订单表，处理其中的所有订单
                // 将订单状态变为取消
                orders.setStatus(Orders.CANCELLED);
                // 封装取消原因
                orders.setCancelReason(MessageConstant.PAY_ORDER_TIMEOUT);
                // 封装取消时间
                orders.setCancelTime(LocalDateTime.now());
                // 更新数据库
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直处于“派送中”的实际已经完成的订单
     */
    @Scheduled(cron = "0 0 1 * * ?") // 在每月每天的1时的0分0秒，也就是在每一天的1时
    public void processDeliveryOrder() {
        log.info("处理派送中的订单：{}", new Date());
        // 这么写的意思是：需要在打烊后，查找出那些在打烊前2小时提交的，仍然处于派送中的订单，按道理来说这些订单是已经完成了的，但是
        // 用户没有及时完成，所以说需要自动完成订单
        LocalDateTime time = LocalDateTime.now().plusHours(-2);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, time);

        if(ordersList != null && !ordersList.isEmpty()){
            ordersList.forEach(order -> {
                // 完成订单
                order.setStatus(Orders.COMPLETED);
                // 更新数据库
                orderMapper.update(order);
            });
        }
    }
}
