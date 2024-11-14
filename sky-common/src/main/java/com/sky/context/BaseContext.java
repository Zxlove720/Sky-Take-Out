package com.sky.context;

/**
 *  TODO ThreadLocal这里十分重要
 *  ThreadLocal并不是一个Thread，而是Thread的局部变量
 *  ThreadLocal为每个线程单独提供一份存储空间，具有线程隔离的效果；只有在线程之内才可以获取线程中对应的值，线程外则不能访问
 *  BaseContext类中封装了Thread的常用的三个方法：设置/返回当前线程局部变量的值；移除当前线程的局部变量
 */
public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    // 设置当前线程局部变量的值
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    // 返回当前线程局部变量的值
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    // 移除当前线程局部变量的值
    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
