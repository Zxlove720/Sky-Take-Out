package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
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
    @Autowired
    private DishMapper dishMapper;

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

    /**
     * 根据id查询套餐
     *
     * @param id
     * @return
     */
    @Override
    public Setmeal getById(Long id) {
        return setMealMapper.getById(id);
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 使用PageHelper插件，便捷实现分页查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        // 调用Mapper中的方法进行分页查询
        Page<SetmealVO> pages = setMealMapper.pageQuery(setmealPageQueryDTO);
        // 获取分页查询的总页数
        Long total = pages.getTotal();
        // 获取分页查询的结果
        List<SetmealVO> result = pages.getResult();
        // 封装成PageResult对象返回Controller层响应
        return new PageResult(total, result);
    }

    /**
     * 批量删除套餐
     *
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        // 判断当前套餐的id，是否为起售，若该套餐起售了，则无法删除
        for (Long id : ids) {
            Setmeal setmeal = setMealMapper.getById(id);
            if (id.equals(StatusConstant.DISABLE)) {
                // 假如当前套餐是起售状态，那么无法删除，直接抛出异常
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
            // 假如当前套餐没有起售，那么可以删除
            // 先删除套餐表中的套餐
            setMealMapper.delete(id);
            // 再删除套餐关联的菜品信息
            setmealDishMapper.deleteBySetmealID(id);
        }
    }

    /**
     * 更新套餐信息
     *
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {

        // 将SetmealDTO对象封装为对应的Setmeal对象
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 修改setmeal表，修改套餐信息
        setMealMapper.update(setmeal);
        // 获取当前套餐id
        Long setmealId = setmealDTO.getId();
        // 删除原来的套餐和菜品的关联，操作setmeal_dish表中的delete方法
        setmealDishMapper.deleteBySetmealID(setmealId);
        // 获得当前的setmeal下所有的菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(new Consumer<SetmealDish>() {
            @Override
            public void accept(SetmealDish setmealDish) {
                // 将当前菜品和当前套餐产生关联
                System.out.println(setmealId);
                setmealDish.setSetmealId(setmealId);
            }
        });
        // 重新插入套餐和菜品的关系
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 套餐起售停售
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 起售套餐的时候，必须先判断其中是否有已经停售的菜品，若存在已经停售的菜品，那么抛出异常并提示
        if (status.equals(StatusConstant.ENABLE)) {
            // 查询当前id对应的套餐下包含的菜品
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if (dishList != null && !dishList.isEmpty()) {
                // 判断当前套餐下菜品不为空才进行下一步操作
                dishList.forEach(new Consumer<Dish>() {
                    @Override
                    public void accept(Dish dish) {
                        if (dish.getStatus().equals(StatusConstant.DISABLE)) {
                            // 假如该套餐下的某个菜品是停售状态，那么该套餐无法起售，抛出异常
                            throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                        }
                    }
                });
            }
        }
        Setmeal setmeal = Setmeal.builder()
        .id(id)
        .status(status)
        .build();
        setMealMapper.update(setmeal);
    }
}
