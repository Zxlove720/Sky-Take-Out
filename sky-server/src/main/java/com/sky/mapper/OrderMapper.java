package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {

    /**
     * 在订单表中插入一个订单
     *
     * @param order
     */
    // 其实按理来说该Order对象已经高度封装，可以直接使用@Insert注解，但是Service层需要使用返回的id主键，所以说必须使用XML配置文件
    void insert(Orders order);
}
