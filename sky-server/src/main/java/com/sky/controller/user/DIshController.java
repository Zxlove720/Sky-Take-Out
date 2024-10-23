package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 需要提供一个客户端的菜品DishController
// 主要用于查询菜品（如果有口味还需要查询口味）；查询套餐；套餐中包含的菜品

// 实现思路：
// 首先需要查询分类
// 然后根据分类id查询菜品、根据分类id查询套餐；然后根据套餐id查询包含的菜品

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "客户端-菜品浏览接口")
public class DIshController {
    @Autowired
    private DishService dishService;

    /**
     * 根据菜品分类id查询对于的菜品及其口味
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据菜品分类查询对应菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        // 查询在售状态中的菜品
        dish.setStatus(StatusConstant.ENABLE);
        List<DishVO> list = dishService.listWithFlavor(dish);
        return Result.success(list);
    }
}
