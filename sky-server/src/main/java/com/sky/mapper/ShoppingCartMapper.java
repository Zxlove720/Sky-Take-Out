package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 条件查询
     *
     * @param shoppingCart
     * @return
     */
    // 这将是一个条件查询，动态SQL；根据传递的dish_id或setmeal_id查询，如果传递了其中之一，则返回，
    // 就可以知道当前用户的购物车中是否存在当前的请求添加的套餐或菜品
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 更新商品数量
     *
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 添加购物车数据
     *
     * @param shoppingCart
     */
    // 因为要添加到购物车中的商品都在Service层中被封装完整了的，所以说无需动态SQL，只需要使用@Insert注解即可
    @Insert("insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            "values (#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

}
