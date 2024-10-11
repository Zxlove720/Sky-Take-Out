package com.sky.properties;

// 这是一个阿里云的配置类，也是为了避免硬编码的问题

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// @Component注解是将该类加入IOC容器管理
@Component
// @ConfigurationProperties注解的意思是该类的属性将从配置文件中前缀为“sky.alioss”的属性开始读取
@ConfigurationProperties(prefix = "sky.alioss")
@Data
public class AliOssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

}
