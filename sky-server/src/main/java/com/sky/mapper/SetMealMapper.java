package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealMapper {

    /**
     * 根据id查询套餐
     *
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id}")
    SetmealVO getById(Long id);

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
    // TODO重大BUG，因为要使用自动自增的id给当前的setmeal对象，所以说不能直接使用@Insert注解，而必须使用xml配置SQL
    // useGeneratedKeys="true"：表示在插入数据后，要使用数据库生成的键值（通常是自增主键）
    // keyProperty="id"：指定将数据库生成的键值赋值给传入参数对象的id属性，这样才能将自增的id给setmeal对象
    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);


}
