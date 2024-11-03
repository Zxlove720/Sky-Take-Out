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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 需要提供一个客户端的菜品DishController
// 主要用于查询菜品（如果有口味还需要查询口味）；查询套餐；套餐中包含的菜品

// 实现思路：
// 首先需要查询分类
// 然后根据分类id查询菜品、根据分类id查询套餐；然后根据套餐id查询包含的菜品


// redis缓存菜品，减少数据库的查询
// 在数据库中查找属于IO，假如有很多用户同时访问，那么查找效率将会变低，所以说要将菜品数据缓存在redis中，这样才方便调用
// 构造redis的逻辑：key就是分类的id，value是当前id下对应的菜品
@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "客户端-菜品浏览接口")
public class DIshController {

    @Autowired
    private DishService dishService;

    // 自动注入redis操作工具
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 根据菜品分类id查询对于的菜品及其口味
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据菜品分类查询对应菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        // 构造redis中使用的key，规则：dish_分类id代表不同的菜品分类
        String key = "dish_" + categoryId;
        // 查询redis中是否查找当前key对应的菜品数据，将菜品数据封装为list集合返回
        List<DishVO> valueList = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (valueList != null && !valueList.isEmpty()) {
            // 若存在这个值，那么直接返回，无需查询数据库
            return Result.success(valueList);
        }
        // 若redis中不存在，那么查询数据库，并将其缓存在redis中
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        // 查询在售状态中的菜品
        dish.setStatus(StatusConstant.ENABLE);
        List<DishVO> list = dishService.listWithFlavor(dish);
        // 将其缓存在redis中
        redisTemplate.opsForValue().set(key, list);
        return Result.success(list);
    }
    // 为了保持数据库中的数据和redis中缓存的数据保持一致，修改管理端的DishController的相关方法，每当数据发生变化，在管理端就要清除其缓存

}
