package com.sky.config;

/* Java中可以操作Redis，常见的Redis的Java客户端：Jedis、Lettuce、Spring Data Redis，主要使用Spring Data Redis
*  Spring Data Redis提供了一个高度封装的类：RedisTemplate；对相关API进行归类封装，将同一类型的操作封装为Operation接口
*  ValueOperations：String数据操作
*  SetOperations：set类型数据操作
*  ZSetOperations：zset类型数据操作
*  HashOperations：hash类型数据操作
*  ListOperation：list类型数据操作
* */

// 环境准备
// 1.在pom.xml文件中导入Spring Data Redis的maven坐标
// 2.在.yml中配置Redis数据源：注意使用外部配置引用的方法配置
// 3.编写配置类，创建RedisTemplate对象
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建redis模板对象...");
        // 创建RedisTemplate对象
        RedisTemplate redisTemplate = new RedisTemplate();
        // 设置redis工厂连接对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 设置redis key序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }
    // 为什么要自己创建RedisTemplate类
    // Spring boot框架的确会自动装配RedisTemplate对象，但是默认的key的序列化器为JdkSerializationRedSerializer，会导致
    // 存储到Redis中的数据和原始数据有差别，所以说要手动设置StringRedisSerializer序列化器

}
