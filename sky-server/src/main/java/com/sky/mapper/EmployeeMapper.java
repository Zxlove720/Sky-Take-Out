package com.sky.mapper;

import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     * @param employee
     */

    // 因为后端将Employee类封装完整了，其属性和数据库表中的字段能够一一对应，不存在动态SQL情况，所以说可以直接使用注解编写SQL
    @Insert("insert into employee (name, username, password, phone, sex, id_number, status, " +
            "create_time, update_time, create_user, update_user) values (#{name}, #{username}, #{password}, " +
            "#{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    // 注意：1.values中的值必须和insert中值一一对应 2.表中字段是用下划线分割的，但是类中属性是驼峰命名，要在配置文件中开启驼峰命名
    void insert(Employee employee);



}
