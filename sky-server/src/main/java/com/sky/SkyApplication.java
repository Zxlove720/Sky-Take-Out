package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
// 开启注解方式的事务管理
@EnableTransactionManagement
@Slf4j

// TODO cache缓存这里也算是本项目的亮点，日后可以回顾
// SpringCache是Spring中的一个框架，实现了基于注解的缓存功能，只需要简单的注解，就可以实现复杂的缓存
// 并且SpringCache只是提供了一层抽象，底层可以切换不同的缓存实现，最常用的是Redis
// @EnableCaching是开启缓存注解功能，通常添加在启动类上
@EnableCaching
// 在SpringBoot项目中，使用SpringCache只需要在项目中导入相关缓存技术的依赖，并且在启动类上添加@EnableCache注解
// 假如在本项目中用Redis作为缓存技术实现，只需要导入Spring data Redis的maven坐标即可，这就是Spring的便捷之处
// TODO 当开发完成之后，为了开发规范，建议为每一个文档注释补全
public class SkyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        log.info("server started");
        log.info("Hide On Bush");
    }
}
