package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    // 需要服务端请求的微信服务的接口地址
    public static final String WECHAT_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wechatLogin(UserLoginDTO userLoginDTO) {
        // 通过封装的getOpenid函数获得用户的openid
        String openid = getOpenid(userLoginDTO.getCode());
        // 判断是否成功获得openid，若openid为空则登录失败，抛出异常
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        // 判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);
        // 如果是新用户，那么自动完成注册，将其保存到数据库
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        // 返回该用户对象
        return user;
    }

    /**
     * 调用微信的接口服务，获取微信用户的openid
     *
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        // 这个hashMap是为了存储向微信服务器发送请求时需要的参数，就是请求参数，封装成map
        Map<String, String> map = new HashMap<>();
        // 封装小程序的appid
        map.put("appid", weChatProperties.getAppid());
        // 封装小程序对应的密钥
        map.put("secret", weChatProperties.getSecret());
        // 封装小程序的临时登录凭证
        map.put("js_code", code);
        // 封装授权类型，这里的授权类型是：表示通过使用授权码（临时登录凭证）获取openid
        map.put("grant_type", "authorization_code");
        // 这是微信接口返回的json格式数据，其中包含了openid
        String json = HttpClientUtil.doGet(WECHAT_LOGIN, map);
        // 用JSON处理工具将字符串形式的json数据转换为JsonObject对象，便于操作
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
