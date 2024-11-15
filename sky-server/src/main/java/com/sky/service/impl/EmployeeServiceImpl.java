package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordEditFailedException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
@Slf4j
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
        //使用DigestUtils中的md5DigestAsHex方法，传递加密前的密码（注意：传递字节）进行MD5加密之后再比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus().equals(StatusConstant.DISABLE)) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 原始SQL语句：select * from employee limit 0, 10
        // 使用PageHelper插件帮助分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        // PageHelper查询默认返回Page，Page是继承了ArrayList的，其中存储的是查询到的对象（这里要用Employee对象）
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        // 获取总页数
        long total = page.getTotal();
        // 获取每一页的记录
        List<Employee> records = page.getResult();
        // 返回查询结果
        return new PageResult(total, records);
    }

    /**
     * 根据id查询员工信息
     *
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        // 回显到前端页面，密码即使通过MD5加密了也不安全，所以说要对回显的密码进行再次加密
        employee.setPassword("******");
        return employee;
    }

    /**
     * 新增员工
     *
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

//        // 设置当前记录的创建时间和修改时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
//        // 设置当前记录创建人的id和修改人的id（当前登录用户的id）
//        // 按理来说，Service层无法直接获取当前登录用户的id；但是可以通过ThreadLocal携带变量，
//        // 将解析JWT得到的用户id传递给Service层。因为登录、解析和新增员工操作同属于一个线程（客户端的每一个请求都是一个单独的线程）
//        // 所以在同一个线程内可以获取到对应的值。
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        // 使用mapper层操作数据库，插入一个新的员工
        employeeMapper.insert(employee);
    }

    /**
     * 编辑员工信息
     *
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        // 将DTO类转换为对应的实体类————对象拷贝（DTO只是为了前端的数据传输，其中的属性是不全的，给Mapper应该要用对应的实体类）
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
//        // 为employee类补充信息
//        // 更新时间
//        employee.setUpdateTime(LocalDateTime.now());
//        // 操作用户的id————根据ThreadLocal得到
//        employee.setUpdateUser(BaseContext.getCurrentId());

        // 调用Mapper层中方法操作数据库（在实现启用/禁用员工账号时已经实现，所以说直接可以使用）
        employeeMapper.update(employee);
    }

    /**
     * 启用/禁用员工账号
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 不能给Mapper层直接传递status和id，需要封装为Employee实体类再传递给Mapper层

        // 通过Employee中的builder方法构建一个Employee实体对象
        // 要封装为实体类再传递给Mapper的主要原因：
        // 1.更加清晰：通过使用实体类，能够直接理解是在操作一个员工对象，代码阅读性提高
        // 2.减少参数传递：减少了参数传递，更加简洁，并且在传递时不易出错
        // 3.Mybatis在设计时就是围绕对象操作进行的，更符合Mybatis的设计原则
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        // 因为启用/禁用员工账号本质上是修改员工的status属性，所以说直接调用Mapper中的update方法即可
        employeeMapper.update(employee);
    }

    /**
     * 员工修改密码
     *
     * @param passwordEditDTO
     */
    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        Long id = BaseContext.getCurrentId();
        System.out.println("-------------------");
        System.out.println(passwordEditDTO);
        log.warn("{}", id);
        Employee employee = employeeMapper.getById(id);
        String oldPassword = employee.getPassword();
        // 比对旧密码是否一致，若旧密码一致那么就可以修改为新密码
        // 因为数据库中的员工密码是加密过的，所以说比对时也需要将密码进行加密之后再比对
        if (DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes()).equals(oldPassword)) {
            // 前端请求的员工原密码和查询的对应员工密码成功比对，可以修改密码
            // 前端请求的新密码是没有加密的，需要将其加密
            employee.setPassword(DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes()));
            // 更新数据库
            employeeMapper.update(employee);
        } else {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_EDIT_FAILED);
        }
    }
}
