package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkSpaceService workSpaceService;

    /**
     * 抽取方法：根据begin————end时间区间创建日期集合
     *
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> createTimeList (LocalDate begin, LocalDate end) {
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
        return dateList;
    }

    /**
     * 根据时间区间查询营业额数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 通过抽取的方法创建时间区间的日期集合
        List<LocalDate> dateList = createTimeList(begin, end);
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
        // 通过抽取的方法创建时间区间的日期集合
        List<LocalDate> dateList = createTimeList(begin, end);
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

    /**
     * 根据时间区间查询订单数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        // 通过抽取的方法创建时间区间的日期集合
        List<LocalDate> dateList = createTimeList(begin, end);

        // 总订单集合
        List<Integer> totalOrdersList = new ArrayList<>();
        // 有效订单集合 valid   adj.有效的
        List<Integer> validOrdersList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 查询总订单
            Integer totalOrders = getOrdersCount(beginTime, endTime, null);
            // 查询有效订单
            Integer validOrders = getOrdersCount(beginTime, endTime, Orders.COMPLETED);
            // 进行前端逻辑处理，将null变为0
            totalOrders = totalOrders == null ? 0 : totalOrders;
            // TODO 细心！细心！细心！不要再犯这种傻逼错误
            validOrders = validOrders == null ? 0 : validOrders;
            // 将其加入对应的集合
            totalOrdersList.add(totalOrders);
            validOrdersList.add(validOrders);
        }

        // 计算总订单数量
        Integer totalOrdersCount = 0;
        for (Integer order : totalOrdersList) {
            totalOrdersCount += order;
        }
        // 计算总有效订单数量
        Integer validOrdersCount = 0;
        for (Integer order : validOrdersList) {
            validOrdersCount += order;
        }
        // 计算完单率
        // 如果没有订单，完单率就是0
        Double orderCompletionRate = 0.0;
        if (totalOrdersCount != 0) {
            // 只有存在订单，才计算完单率
         orderCompletionRate = validOrdersCount.doubleValue() / totalOrdersCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(totalOrdersList, ","))
                .validOrderCountList(StringUtils.join(validOrdersList, ","))
                .totalOrderCount(totalOrdersCount)
                .validOrderCount(validOrdersCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 根据时间区间和订单状态查询订单数据
     *
     * @param beginTime
     * @param endTime
     * @param status
     * @return
     */
    private Integer getOrdersCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        // 将时间和状态封装为map进行查询
        // 在SQL中先根据status查询，若status不对，那么就可以不用比对后面的属性，提高效率
        Map<Object, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("begin", beginTime);
        map.put("end", endTime);
        return orderMapper.statisticsOrders(map);
    }

    /**
     * 根据时间区间统计畅销top10商品
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10Statistics(LocalDate begin, LocalDate end) {
        // 因为不需要统计每一天的销量，只需要统计在这段时间之内的销量，所以说不需要遍历每一天的销量，可以直接使用begin和end
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 查询销量前10的商品，并封装在GoodsSalesDTO中（商品名和销量）
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime, endTime);
        // 这里的逻辑是：需要在数据库中查询已经完成了的订单，查询这些订单的明细，查询出订单明细中的商品名和对应的数量；
        // 传递时间区间后正好进行条件查询，查询这个区间之内的订单。最后按照商品名进行分组，对应数量进行倒序排序（销量高的在前）；
        // 分页查询（limit关键字）前10条数据，就正好可以查询到销量前10的商品了。查询的别名正好和goodsSalesDTO类中属性相同，MyBatis可以直接映射

        // 处理goodsSalesDTOList中数据
        // 开启流，通过map方法传入一个函数式接口（lambda表达式（lambda表达式可以替换为方法引用））处理集合中的每一个GoodsSalesDTO对象
        // 得到每一个GoodsSalesDTO对象的name或number将其收集为集合，然后再用StringUtils中的join方法将其遍历为一个字符串封装到VO中返回
        String nameList = StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getName)
                .collect(Collectors.toList()), ",");

        String numberList = StringUtils.join(goodsSalesDTOList.stream().map(goodsSalesDTO -> goodsSalesDTO.getNumber())
                .collect(Collectors.toList()), ",");

        // 封装成对应的VO返回
        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出运营数据报表
     *
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 需要导出近30的报表，所以说起始日期是当前时间 - 30天，结束时间是当前时间 - 1天
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        // 查询运营数据，提供给Excel模板文件
        // 因为通过Apache POI只能简单的查询数据到Excel中，无法修改Excel的布局、字体、单元格等，所以说需要事先提供Excel模板文件
        BusinessDataVO businessData = workSpaceService.getBusinessData
                (LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

        // 开启模板文件的输入流
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            // 基于提供好的模板文件创建一个新的Excel表格对象
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            // 获取Excel中的一个新的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            // 给这张表封装时间
            sheet.getRow(1).getCell(1).setCellValue(begin + "至" + end);
            // 接下来需要根据定义的模板文件，具体为单元格封装数据
            // 获取第四行
            XSSFRow row = sheet.getRow(3);
            // 获取需要封装数据的单元格，并对其封装数据
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            // 获取第五行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            // 按照模板文件，接下来需要封装明细数据，明细数据从第8行开始，有30行、6列，可以用循环完成
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                // 准备明细数据
                // 这里的明细是每一天的，所以说传递的时间也是每一天的
                businessData = workSpaceService.getBusinessData(LocalDateTime.of(date,LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(7 + i);
                // 为每一个单元格封装数据
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            // 通过输出流，将文件下载到客户端浏览器中
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            // 关闭资源
            out.flush();
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
