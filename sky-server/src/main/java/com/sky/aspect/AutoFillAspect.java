package com.sky.aspect;

// 这是切面类
// 用于自动填充公共字段，统一拦截加入了AutoFill注解的方法，并且通过反射为公共字段赋值
// TODO 这是该项目的难点、亮点之一

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充的处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {


    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    // 切入点表达式：在com.sky.mapper包中所有有AutoFill的注解的方法
    public void autoFillPointCut() {}
    /**
     * 前置通知
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充...");
        // 获取到当前被拦截的方法上的数据库操作类型

        // 获取方法签名对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取方法上的注解对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        // 获取数据库操作类型
        OperationType operationType = autoFill.value();

        // 获取到当前被拦截的方法的参数 -- 实体对象
        Object[] args = joinPoint.getArgs();
        // 如果当前被拦截的方法的参数（也就是对应的实体类）为null或为0，那么就直接返回，不需要进行自动填充
        if (args == null || args.length==0) {
            return;
        }

        // 得到当前方法的实体类（也就是上面Object数组的第一个元素）
        Object entity = args[0];
        // 准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        // 根据当前不同的操作类型，为对应的属性通过反射赋值
        if (operationType == OperationType.INSERT) {
            // 如果当前是INSERT操作，那么需要为4个公共字段赋值
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射为对象属性赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (operationType == OperationType.UPDATE) {
            // 当前是UPDATE操作，只需要给两个公共字段赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
