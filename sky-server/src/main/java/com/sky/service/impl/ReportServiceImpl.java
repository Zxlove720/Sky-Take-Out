package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 根据时间区间查询营业额数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 创建一个集合，用于存储从begin-end时间内的所有日期，用于查找对应的营业额
        List<LocalDate> dateList = new ArrayList<>();
        // 先加入起始日期
        dateList.add(begin);
        // 若还没有加入到最后一个日期，就一直加入
        while (!begin.equals(end)) {
            // 每次都将begin日期后延1天，直到begin = end
            begin = begin.plusDays(1);
            // 加入集合
            dateList.add(begin);
        }

        // 创建营业额集合，用于存储每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        // 遍历日期集合，按照每一天进行逻辑处理
        for (LocalDate date : dateList) {
            // 因为表中的订单的时间是LocalDateTime类型的，所以说要将日期集合中的LocalDate封装为LocalDateTime
            // 当天的营业额是大于当天的最小时间（00:00:00），小于当天的最大时间的（23:59:59），所以说可以将日期集合中的元素对应的
            // 那天的beginTime设置为当天最小时间；endTime设置为当天的最大时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            // 用Map来封装查询的条件
            Map<Object, Object> map = new HashMap<>();
            // 要统计当天的营业额，只统计已经完成了的订单
            map.put("status", Orders.COMPLETED);
            // 封装查询的时间（时间为当天）
            map.put("begin", beginTime);
            map.put("end", endTime);
            Double turnover = orderMapper.sumAmount(map);
            // 判断当天是否有营业额，若没有营业额则turnover为空，但是这不符合前端展示的逻辑，需要对其检查，若没有营业额，那么营业额是0.0
            turnover = turnover == null ? 0.0 : turnover;
            // 将当前date对应的营业额加入turnover集合
            turnoverList.add(turnover);
        }
        // 处理数据返回
        // 使用StringUtils进行数据封装，封装为前端需要的格式返回。StringUtils是Apache的
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 根据时间区间查询用户数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 创建日期集合
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(! begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 新增用户集合
        List<Integer> newUserList = new ArrayList<>();
        // 总用户集合
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            // 总用户，只要在目标时间之前创建的用户都算是当前时间的总用户
            // 建议先查询总用户数，因为查询条件更加简单
            Integer totalUser = getUserCount(null, endTime);
            // 新增用户可以理解为：假如是今天是11.11日，
            // 那么在11.11日最小时间（00:00:00）————11.11日最大时间（23:59:59）创建的用户都是这一天的新用户
            Integer newUser = getUserCount(beginTime, endTime);
            // 进行前端逻辑处理，将null变为0
            totalUser = totalUser == null ? 0 : totalUser;
            newUser = newUser == null ? 0 : newUser;
            // 加入对应集合
            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }
        // 封装数据返回
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 根据时间区间统计用户数量
     *
     * @param beginTime
     * @param endTime
     * @return
     */
    private Integer getUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        // 将开始时间和结束时间封装为map再在数据库中进行查询
        Map<Object, Object> map = new HashMap<>();
        map.put("begin", beginTime);
        map.put("end", endTime);
        return userMapper.countUsersByTime(map);
    }


}
