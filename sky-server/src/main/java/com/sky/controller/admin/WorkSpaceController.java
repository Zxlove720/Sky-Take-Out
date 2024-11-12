package com.sky.controller.admin;


import com.sky.result.Result;
import com.sky.service.WorkSpacerService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;

// 实现工作台
// 工作台是系统运营的数据看板，提供了快捷操作的入口（直接点击工作台即可），有效提高了商家管理的效率
// 工作台实现相对简单，只需要简单的在数据库中查找数据并封装返回即可
@RestController
@RequestMapping("/admin/workspace")
@Slf4j
@Api(tags = "工作台相关接口")
public class WorkSpaceController {

    @Autowired
    private WorkSpacerService workSpaceService;

    /**
     * 工作台查询今日数据
     *
     * @return
     */
    @GetMapping("/businessData")
    @ApiOperation("工作台今日数据查询")
    public Result<BusinessDataVO> businessData() {
        // 获得当天开始时间
        // with方法是LocalDateTime基于现在的时间创建一个新的时间
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        // 获得当天结束时间
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        BusinessDataVO businessDataVO = workSpaceService.getBusinessData(begin, end);
        return Result.success(businessDataVO);
    }

    /**
     * 查询订单管理数据
     *
     * @return
     */
    @GetMapping("/overviewOrders")
    @ApiOperation("查询订单管理数据")
    public Result<OrderOverViewVO> orderOverView() {
        return Result.success(workSpaceService.getOrderView());
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    @GetMapping("/overviewDishes")
    @ApiOperation("查询菜品总览")
    public Result<DishOverViewVO> dishOverView() {
        return Result.success(workSpaceService.getDishOverView());
    }

}
