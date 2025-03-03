package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

// 虽然可以通过一张MySql中的表来存储营业状态的数据，但是表中就只有营业状态这个字段，所以说没有必要，所以说选择Redis的字符串来存储
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {
    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

//    /**
//     * 设置店铺的营业状态
//     *
//     * @param status
//     * @return
//     */
//    @PutMapping("/{status}")
//    @ApiOperation("设置店铺的营业状态")
//    public Result setStatus(@PathVariable Integer status) {
//        log.info("设置店铺的营业状态为：{}", status == 1 ? "营业中": "打样中");
//        redisTemplate.opsForValue().set(KEY, status);
//        return Result.success();
//    }

    /**
     * 获取店铺当前的营业状态
     *
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺当前的营业状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer)redisTemplate.opsForValue().get(KEY);
        log.info("获取店铺的营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
