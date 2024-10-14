package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper {

    /**
     * 查询对应分类关联的菜品数量
     *
     * @param id
     * @return
     */
    Integer countDishByCategory(Long id);

    /**
     * 新增菜品和对应口味
     *
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);
}
