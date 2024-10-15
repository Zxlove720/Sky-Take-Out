package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

@Service
@Slf4j
public class DishServiceImp implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        // 将DTO转换为对应的实体对象
        BeanUtils.copyProperties(dishDTO, dish);

        // 向菜品表中插入1条数据
        dishMapper.insert(dish);

        // 获取insert语句生成的主键值
        // 这个是dish的主键id，也就是dish_id，dish_flavor中需要这个属性
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 判断当前添加的菜品口味
        // 如果口味不是null或者空（说明有口味），那么就在口味表中插入添加菜品的口味
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(new Consumer<DishFlavor>() {
                @Override
                public void accept(DishFlavor dishFlavor) {
                    dishFlavor.setDishId(dishId);
                }
            });
            // 向口味表中插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

}
