package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetMealMapper setMealMapper;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // 将ShoppingCartDTO对象封装为对应的ShoppingCart对象
        // ShoppingCartDTO将传递这次添加购物车请求的内容（是套餐还是菜品；如果是菜品还会再传递一个菜品的口味信息）
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        // 因为每个用户都有一个自己的购物车，所以说需要给当前购物车设置为当前用户的id
        shoppingCart.setUserId(BaseContext.getCurrentId());
        // 判断当前要添加到购物车中的商品是否已经在购物车中了
        // 这里是根据这次请求添加的内容，查看购物车表中是否已经存在了当前请求添加的内容
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList != null && !shoppingCartList.isEmpty()) {
            // 当这个集合不为空时，则说明当前想要添加的菜品或者套餐已经在该用户的购物车中存在，直接数量加1即可
            shoppingCart = shoppingCartList.get(0);
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(shoppingCart);
        } else {
            // 如果不存在，那么直接在购物车中插入，先插入1个

            // 判断当前添加到购物车中的是菜品还是套餐
            // 因为加入到购物车中的只能是菜品或者套餐，并且一般而言是菜品的概率更大，所以说可以先假定添加的是菜品
            // 然后获取到菜品的id，如果添加的不是菜品，那么菜品id就会是null，就证明是添加的是套餐

            // 先假定添加的是菜品，获取菜品的id
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {
                // 若菜品id不是null，那么这次添加一定是菜品

                // 根据id查询这个菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                // 若菜品id是null，那么这次添加一定是套餐
                Setmeal setmeal = setMealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }

            // 不论是套餐还是菜品，都需要补充以下属性
            // 设置添加的量，每次都只能添加一个
            shoppingCart.setNumber(1);
            // 设置添加时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }
}
