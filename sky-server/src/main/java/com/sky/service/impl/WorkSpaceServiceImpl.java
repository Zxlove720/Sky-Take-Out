package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpacerService;
import com.sky.vo.BusinessDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkSpaceServiceImpl implements WorkSpacerService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

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
}
