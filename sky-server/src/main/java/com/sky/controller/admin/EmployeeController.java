package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
// @Api注解是用于类上，表示对类的说明的注解
@Api(tags = "员工管理相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO 前端请求用户名和密码，通过@RequestBody封装到DTO中
     * @return Result<EmployeeLoginVO>
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录")
    //ApiOperation 注解用在方法上，对方法进行说明
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 员工退出
     * 前端直接操作的，不需要请求后端数据，直接返回即可
     * @return Result
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出")
    public Result logout() {
        return Result.success();
    }

    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO 前端请求员工姓名（支持模糊匹配）、分页查询的页码、每一页显示的记录条数
     * @return Result<PageResult>
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        // 仍然使用DTO进行封装，DTO中的属性和前端传递的数据一一对应
        // 因为前端传递的数据不是json格式的，而是Query格式的数据，所以说无需使用@RequestBody参数来指定参数，可以直接封装
        log.info("员工分页查询，参数为：{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id查询员工信息
     *
     * @param id 员工id
     * @return Result<Employee> 封装根据id查询的员工信息返回 用作前端回显
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工信息")
    public Result<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 新增员工
     * @param employeeDTO 前端传递系列员工信息，封装到DTO中
     * @return Result
     */
    @PostMapping
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 编辑员工信息
     *
     * @param employeeDTO 前端传递系列员工信息，封装到DTO中
     * @return Result
     */
    @PutMapping
    @ApiOperation("编辑员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑员工信息：{}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }

    /**
     * 启用/禁用员工账号
     *
     * @param status 员工状态，前端是传递的是员工当前的状态，用status封装Employee对象
     * @param id 员工id，根据id来封装Employee对象，便于在数据库中修改
     * @return Result
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用/禁用员工账号")
    // 前端的请求路径携带当前员工的状态值：若员工状态为禁用，那么要将其变为启用；若状态为启用，则变为禁用
    // 前端请求路径还传递了id，通过id定位该操作是针对哪个用户
    public Result startOrStop(@PathVariable Integer status, Long id) {
        if (status.equals(StatusConstant.ENABLE)) {
            log.info("禁用员工账号：{}, {}", status, id);
        } else {
            log.info("启用员工账号：{}，{}", status, id);
        }
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 员工修改密码
     *
     * @param passwordEditDTO 前端传递需要修改密码的员工id、旧密码和新密码，封装到密码修改的特定DTO中
     * @return Result
     */
    @PutMapping("/editPassword")
    @ApiOperation("员工修改密码")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        employeeService.editPassword(passwordEditDTO);
        return Result.success();
    }
}
