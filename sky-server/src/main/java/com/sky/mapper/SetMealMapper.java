package com.sky.mapper;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Insert;
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

    /**
     * 插入一个新套餐
     *
     * @param setmeal
     */
    @Insert("insert into setmeal (id, category_Id, name, price, status, description, image, create_time, update_time, " +
            "create_user, update_user) values (#{id}, #{categoryId}, #{name}, #{price}, #{status}, #{description}, " +
            "#{image}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Setmeal setmeal);
}
