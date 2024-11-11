package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 在订单表中插入一个订单
     *
     * @param order
     */
    // 其实按理来说该Order对象已经高度封装，可以直接使用@Insert注解，但是Service层需要使用返回的id主键，所以说必须使用XML配置文件
    void insert(Orders order);

    /**
     * 根据订单号和用户id查询订单
     * @param orderNumber
     * @param userId
     */
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 分页查询用户历史订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     *
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 各状态的订单数量统计
     *
     * @param toBeConfirmed
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer toBeConfirmed);

    /**
     * 根据状态和时间查询问题订单
     *
     * @param pendingPayment
     * @param time
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    // 查询订单状态为处于指定状态并且下单时间已经超过规定时间的订单
    List<Orders> getByStatusAndOrderTime(Integer pendingPayment, LocalDateTime time);

    /**
     * 动态条件统计营业额
     *
     * @param map
     * @return
     */
    Double sumAmount(Map<Object, Object> map);

    /**
     * 动态条件统计订单数据
     *
     * @param map
     * @return
     */
    Integer statisticsOrders(Map<Object, Object> map);

    /**
     * 根据时间区间查询销量top10商品（菜品和套餐）
     *
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
