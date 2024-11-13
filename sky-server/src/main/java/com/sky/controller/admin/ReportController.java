package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "统计报表相关接口")
public class ReportController {
    // Apache Echarts
    // Apache Echarts是一个基于JavaScript的数据可视化图标库，提供直观、生动、可交互的数据可视化图表
    // 无论是什么形式的图形，其本质上是数据，ApacheEcharts就是对数据的可视化展示

    // 但是Apache Echarts是前端需要使用的东西，后端只需要按照和前端的约定，为其提供数据即可

    @Autowired
    private ReportService reportService;

    /**
     * 营业额数据统计
     *
     * @param begin
     * @param end
     * @return
     */
    // 查询一段时间内的营业额，前端给后端传递开始时间和结束时间，后端需要查询这段时间内的营业额，并封装到VO中、
    // 约定好VO中封装两个字符串，时间和对应的营业额，用","分隔
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额数据统计")
    // 使用@DateTimeFormat限定前端传递的时间的格式
    public Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd")
                                                       LocalDate begin,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                       LocalDate end) {
        return Result.success(reportService.getTurnoverStatistics(begin, end));
    }

    /**
     * 用户数据统计
     *
     * @param begin
     * @param end
     * @return
     */
    // 用户数据统计分为两个部分：用户总量和新增用户；用户总量很好理解————
    // 而新增用户可以理解为：假如是今天是11.11日，
    // 那么在11.11日最小时间（00:00:00）————11.11日最大时间（23:59:59）创建的用户都是这一天的新用户
    @GetMapping("/userStatistics")
    @ApiOperation("用户数据统计")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd")
                                               LocalDate begin,
                                               @DateTimeFormat(pattern = "yyyy-MM-dd")
                                               LocalDate end) {
        return Result.success(reportService.getUserStatistics(begin, end));
    }

    /**
     * 订单数据统计
     *
     * @param begin
     * @param end
     * @return
     */
    // 订单数据统计需要查询当天的所有订单和完成了的有效订单，并根据这两个数据计算出订单总数、有效订单总数、订单完成率
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单数据统计")
    public Result<OrderReportVO> ordersStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd")
                                                  LocalDate begin,
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                  LocalDate end) {
        return Result.success(reportService.getOrdersStatistics(begin, end));
    }

    /**
     * top10畅销商品统计
     *
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("top10")
    @ApiOperation("top10畅销商品统计")
    public Result<SalesTop10ReportVO> top10Statistics(@DateTimeFormat(pattern = "yyyy-MM-dd")
                                                      LocalDate begin,
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                      LocalDate end) {
        return Result.success(reportService.getSalesTop10Statistics(begin, end));
    }

    /**
     * 导出运营数据报表
     *
     * @param response
     */
    // 基于Apache POI实现导出运营数据报表
    // Apache POI是处理Microsoft Office中各类文件的开源项目，使用POI可以在Java程序中处理Office文件，但是一般只处理EXCEL文件，用来导出数据报表等
    @GetMapping("/export")
    @ApiOperation("导出运营数据报表")
    public void export(HttpServletResponse response) {
        reportService.exportBusinessData(response);
    }
}
