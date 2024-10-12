package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理SQL异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        // 报错信息：Duplicate entry 'zhangsan' for key 'employee.idx_username'
        // 得到报错信息
        String message = ex.getMessage();
        // 策略：若报错信息包含"Duplicate entry"，那么说明是重复添加问题，那么针对其进行异常处理
        if (message.contains("Duplicate entry")) {
            // 按照空格对报错信息进行切片，为了得到哪个值重复添加了
            String[] split = message.split(" ");
            // 得到重复添加的用户名
            String username = split[2];
            // 拼接报错信息，仍然使用常量类中的属性来避免硬编码系列问题
            String errorMessage = username + MessageConstant.ALREADY_EXISTS;
            // 返回封装后的结果
            return Result.error(errorMessage);
        } else {
            // 返回未知错误
            // TODO 日后需要添加更多的SQL异常
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }


}
