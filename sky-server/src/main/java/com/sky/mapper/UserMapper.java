package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     *
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入用户数据
     *
     * @param user
     */
    void insert(User user);

    /**
     * 根据用户id查找用户
     *
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{id}")
    User getById(Long userId);

    /**
     * 根据时间区间查询用户数量
     *
     * @param map
     * @return
     */
    Integer countUsersByTime(Map<Object, Object> map);
}
