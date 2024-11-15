package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 修改员工密码DTO
 * 将前端传递的员工id、旧密码和新密码封装为DTO
 */
@Data
public class PasswordEditDTO implements Serializable {

    //员工id
    private Long empId;

    //旧密码
    private String oldPassword;

    //新密码
    private String newPassword;

}
