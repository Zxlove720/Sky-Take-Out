package com.sky.controller.user;

// 用户通过小程序使用的时候，必须使用微信授权登录之后才可以点餐，微信登录需要获取用户的相关信息，如用户的昵称、头像等

// 基于微信登录和传统的比对用户名和密码完全不一样，需要用到微信自己提供的登录服务
// 若用户第一次使用小程序点餐，那么该用户就是一个新用户，仍然需要将这个用户存储到数据库中，进行自动注册

// 根据微信登录的流程，想要完成微信登录，最终就是要获得微信用户的openid：
// 1.先要在小程序端获取授权码，然后向后端发送请求，这个请求需要携带授权码
// 2.然后服务端在收到小程序端的授权码后，就可以去请求微信接口服务
// 3.最后后端向小程序返回openid和token等数据

// 请求路径：/user/user/login
// 注：第一个user代表用户端，第二个user代表用户模块


import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Api(tags = "客户端用户相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("微信用户登录：{}", userLoginDTO.getCode());

        // 微信登录
        User user = userService.wechatLogin(userLoginDTO);

        // 为微信用户生成jwt令牌
        // 创建一个Map对象，其中存储了jwt令牌中需要包含的声明
        Map<String, Object> claims = new HashMap<>();
        // 需要用户的id
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        // 生成一个jwt令牌，其中需要传递签名的jwt密钥、jwt的有效期、包含jwt声明的map
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        // 通过id、openid、token创建一个VO返回
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }
}
