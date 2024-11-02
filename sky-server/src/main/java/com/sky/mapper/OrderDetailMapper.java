package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 根据订单明细，在订单明细表中批量插入数据
     *
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     * 根据订单id查询订单明细
     *
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{order_id}")
    List<OrderDetail> getByOrderId(Long orderId);
}
