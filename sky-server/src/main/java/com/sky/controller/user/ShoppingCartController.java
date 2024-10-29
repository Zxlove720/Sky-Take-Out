package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 购物车逻辑

// 用户可以将菜品或者套餐添加到购物车中，于菜品而言，若设置了口味信息，那么必须要选择口味后才可以加入购物车；对套餐来说，就可以直接
// 加入购物车；并且在购物车中可以修改菜品或套餐的数量，还可以一键清空购物车

// 购物车实现
// 购物车是基于数据库表实现的：用户的购物车数据需要保存到数据库中，数据库中要存储商品的信息和用户的信息，来确保购物车对于用户而言的唯一性

// 实现细节：
// 1.购物车是关联用户的，所以说在购物车表中需要记录对应的用户信息
// 2.用户可以选择套餐或者菜品；但是一次添加只存在一种可能性，套餐和菜品只能二选一
// 3.对于同一份菜品，若添加了多份，无需添加多条记录，只需要增加数量即可
@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "用户端购物车接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车......");
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查看购物车内容
     *
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车内容")
    public Result<List<ShoppingCart>> list() {
        return Result.success(shoppingCartService.showShoppingCart());
    }

    /**
     * 清空购物车
     *
     * @return
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result clean() {
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }
}
