package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
@Slf4j
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    private SetMealMapper setMealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 根据菜品分类id查询套餐
     *
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setMealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据套餐id查询其包含的菜品列表
     *
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setMealMapper.getDishItemBySetMealId(id);
    }

    /**
     * 新增套餐
     *
     * @param setmealDTO
     */
    @Override
    public void save(SetmealDTO setmealDTO) {
        // 将SetmealDTO对象封装为对应的setmealDTO对象
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 因为有AOP编程，所以说不需要再为setmeal对象补充属性了
        // 直接调用Mapper中的方法添加新的套餐在数据库中
        setMealMapper.insert(setmeal);

        // 需要将套餐和对应的菜品建立联系，所以说需要将套餐的id和其对应的菜品id添加到setmeal_dish表中
        // 获取这次请求添加的套餐的id
        Long setmealId = setmeal.getId();
        log.info("setmealID:{}", setmealId);
        // setmealDTO中的setmealdishes是一个arraylist集合，其中存储的是SetmealDish对象，
        // 对象中有菜品的信息，需要为其补充菜品所属的套餐id
        // 获取当前套餐下存储的setmealDish的信息
        List<SetmealDish> setmealDishs = setmealDTO.getSetmealDishes();
        // 为当前套餐下存储的所有setmealDish关联当前套餐的id
        setmealDishs.forEach(new Consumer<SetmealDish>() {
            @Override
            public void accept(SetmealDish setmealDish) {
                setmealDish.setSetmealId(setmealId);
            }
        });
        // 批量保存套餐和菜品的关联关系
        setmealDishMapper.insertBatch(setmealDishs);
    }
}
