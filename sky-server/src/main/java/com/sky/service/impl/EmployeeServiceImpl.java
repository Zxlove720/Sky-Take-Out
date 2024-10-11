package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        //使用DigestUtils中的md5DigestAsHex方法，传递加密前的密码（注意：传递字节）进行MD5加密之后再比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        // 此处传递的是DTO对象，为了方便封装前端提交的数据，但是传递给持久层（Mapper）还是建议使用实体类
        // 所以说需要将EmployeeDTO对象转换为Employee对象（这两者不能直接进行类型转换）

        /* Employee中的属性实际上是对EmployeeDTO属性的扩展，可以通过set/get对Employee对象的属性进行设置，
           但二者的属性高度相似，这么写十分冗杂；所以说建议使用对象属性拷贝 */

        // 对象属性拷贝
        // ---- 将EmployeeDTO中和Employee相同的属性拷贝给Employee对象
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee); // copyProperties方法：第一个参数是源对象，第二个参数是目标对象
        // Employee的属性比EmployeeDTO的属性更多，所以说拷贝之后需要手动设置Employee对象的属性

        // 设置账号的状态，插入新员工默认是正常状态
        employee.setStatus(StatusConstant.ENABLE);  // 为了避免硬编码系列问题，需要使用StatusConstant常量类中的属性来代表状态码

        // 设置员工密码，员工默认密码123456，但是前端不会传递密码，需要后端补充
        // 同样，在后端补充密码的时候，也要用常量类中的字段来避免硬编码的系列问题，并且密码还需要用MD5加密(DigestUtils是spring框架的包)
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 设置当前记录的创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 设置当前记录创建人的id和修改人的id
        // TODO 目前先随便硬编码一个id，但是后期需要更改为当前登录用户的id
        employee.setCreateUser(10L);
        employee.setUpdateUser(10L);

        // 使用mapper层操作数据库，插入一个新的员工
        employeeMapper.insert(employee);

    }

}
