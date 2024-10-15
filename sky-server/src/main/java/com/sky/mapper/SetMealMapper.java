package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealMapper {

    /**
     * 查询对应分类关联的套餐数量
     *
     * @param id
     * @return
     */
    Integer countSetMealByCategory(Long id);

    /**
     * 查询菜品关联的套餐id
     *
     * @param dishIds
     * @return
     */
    List<Long> getSetMealIdByDishId(List<Long> dishIds);
}
