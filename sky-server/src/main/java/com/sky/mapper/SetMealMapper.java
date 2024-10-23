package com.sky.mapper;

import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /**
     * 根据菜品分类id查询套餐
     *
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询其包含菜品列表
     *
     * @param setMealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description from setmeal_dish as sd left join dish as d " +
            "on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetMealId(Long setMealId);
}
