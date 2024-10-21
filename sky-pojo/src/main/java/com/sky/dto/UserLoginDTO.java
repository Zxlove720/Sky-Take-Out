package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */

// 这个DTO类是接受小程序传递的授权码的类
@Data
public class UserLoginDTO implements Serializable {

    private String code;

}
