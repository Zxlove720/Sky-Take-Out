package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@Service
public interface ReportService {

    /**
     * 根据时间区间统计营业额数据
     *
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 根据时间区间统计用户数量
     *
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 根据时间区间统计订单数据
     *
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

    /**
     * 根据时间区间统计畅销top10商品
     *
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10Statistics(LocalDate begin, LocalDate end);

    /**
     * 导出运营数据报表
     *
     * @param response
     */
    void exportBusinessData(HttpServletResponse response);
}
