package com.awesome.yunpicturebackend.exception;

import com.awesome.yunpicturebackend.common.ResponseCode;

/**
 * 抛异常工具类
 */
public class ThrowUtil {

    /**
     * 条件成立则抛异常
     * @param condition 条件
     * @param runtimeException 异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException){
        if (condition){
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛出响应码相关异常
     * @param condition 条件
     * @param responseCode 响应码
     */
    public static void throwIf(boolean condition, ResponseCode responseCode){
        throwIf(condition,new BusinessException(responseCode));
    }

    /**
     * 条件成立则抛出响应码相关异常并自定义异常信息
     * @param condition 条件
     * @param responseCode 响应码
     * @param message 自定义异常信息
     */
    public static void throwIf(boolean condition, ResponseCode responseCode, String message){
        throwIf(condition, new BusinessException(responseCode,message));
    }

}
