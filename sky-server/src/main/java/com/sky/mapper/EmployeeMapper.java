package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeePageQueryDTO;
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

    /**
     * 分页查询
     * 因为涉及到动态SQL，所以说使用.xml配置文件的方式进行分页查询
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用/禁用员工账号
     * @param employee
     */
    // 因为是更新员工，所以说必须根据传递的参数进行动态SQL，使用.xml文件配置文件
    void update(Employee employee);

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    // 因为该语句无需动态SQL，相对简单，所以说直接使用注解实现SQL
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
}
