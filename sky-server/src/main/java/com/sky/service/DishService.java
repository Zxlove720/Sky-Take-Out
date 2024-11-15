package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查找菜品
     *
     * @param id
     */
    DishVO getById(Long id);

    /**
     * 根据菜品分类id查询该分类下的菜品及其口味
     *
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);

    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);

    /**
     * 删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 修改菜品
     *
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
}
