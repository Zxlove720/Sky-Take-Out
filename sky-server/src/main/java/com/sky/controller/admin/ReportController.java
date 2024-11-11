package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return Result.success(reportService.getTurnover(begin, end));
    }
}
