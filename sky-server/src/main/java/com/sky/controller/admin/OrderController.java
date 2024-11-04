package com.sky.controller.admin;


import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 这个Controller是商家端（管理端）用于处理客户提交的订单的
// 包括查看订单、统计各个状态的订单、接单、拒单、取消、派送、完成、检测订单合理性等常见的外卖业务功能
@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "管理端订单管理接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 订单分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单分页查询")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        // 封装PageResult响应
        return Result.success(pageResult);
    }

    /**
     * 各状态的订单数量统计
     *
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("各状态的订单数量统计")
    // statistic   v.统计
    public Result<OrderStatisticsVO> statistics() {
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查看订单详情
     *
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查看订单详情")
    public Result<OrderVO> details(@PathVariable Long id) {
        OrderVO orderVO = orderService.selectDetails(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     *
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     * @return
     * @throws Exception
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

}
