package com.sky.mapper;

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
}
