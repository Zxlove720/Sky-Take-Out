package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 *
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    // 需要在修改数据时，删除redis中的缓存，保证数据库和redis缓存的一致性 TODO redis规范化问题
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 在每次管理端修改数据后，清除redis中的数据
     *
     * @param pattern
     */
    // 这是一个抽取出来的统一的删除redis缓存的方法，需要给方法传递删除的模式（比如说一个键）
    public void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        // redis 中的删除方法，可以传递Collection对象，会删除所有Collection中的键
        redisTemplate.delete(keys);
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询");
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id查询菜品，并回显到前端
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dishVo = dishService.getById(id);
        return Result.success(dishVo);
    }

    /**
     * 根据分类id查询该分类下所有菜品
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

    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        // 新增了菜品，所以说需要清理缓存的数据
        // 拼接键值（键是dish_ + 当前菜品对应的分类id）
        String key = "dish_" + dishDTO.getCategoryId();
        // 调用清除缓存的方法
        cleanCache(key);
        return Result.success();
    }

    /**
     * 删除菜品
     * 要求：
     * 可以批量删除
     * 起售中的、关联了套餐的菜品无法删除
     * 删除菜品之后，其关联的口味数据也需要删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result deleteDish(@RequestParam List<Long> ids) {
        log.info("菜品正在删除：{}", ids);
        dishService.deleteBatch(ids);
        // 删除了菜品之后，需要清除其缓存数据
        // 因为前端传递的请求只有需要删除的菜品的id的集合，但是redis中缓存的是dish_ + "菜品对应的分类id"，所以说不方便拼接
        // 键，所以说为了方便，每当删除菜品时，直接将redis中缓存的菜品全部删除（也就是删除redis中全部dish_开头的键）
        cleanCache("*dish_*");
        return Result.success();
    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        // 当菜品修改之后，需要清除其redis中的缓存
        // 本来可以使用dish_ + 分类id拼接成一个精确匹配，但是使用get方法也需要去查询数据库，假如说是在高峰期，是得不偿失的，所以说
        // 直接使用模糊匹配，删除所有键
//        String key = "dish_" + dishDTO.getCategoryId();
//        cleanCache(key);
        cleanCache("*dish_*");
        return Result.success();
    }

    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);

        // 同样如此，前端请求的数据没有分类id，所以说当更改了一个菜品的售卖状态，那么直接将所有的菜品缓存清除
        cleanCache("dish_*");

        return Result.success();
    }
}
