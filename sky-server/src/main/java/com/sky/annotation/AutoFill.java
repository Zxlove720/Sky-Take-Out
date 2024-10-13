package com.sky.annotation;

// 公共字段自动填充：在insert和update的时候，后端需要给实体类补充属性，这些代码都是几乎一样的，所以说一直使用set方法会使得十分冗杂
// 可以通过AOP切面编程的思想，通过功能增强的思路，完成公共字段自动填充的功能

// AOP切面编程实现自动填充的实现步骤：
// 1.自定义注解AutoFill，用于标识需要进行公共字段填充的方法
// 2.自定义切面类AutoFillAspect，统一拦截加入了AutoFill注解的方法，并且通过反射为公共字段赋值
// 3.在Mapper中，需要公共字段自动填充的方法上加入AutoFill注解

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行功能字段自动填充处理
 */
// 指定该注解的作用目标是Method方法
@Target(ElementType.METHOD)
// 指定该注解的作用时间为运行时
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    // 指定数据库操作类型：UPDATE INSERT
    // 只有update和insert类型的操作需要用这个注解
    OperationType value();
}
