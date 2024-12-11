package com.awesome.yunpicturebackend.common.utils;

import com.awesome.yunpicturebackend.common.BaseResponse;
import com.awesome.yunpicturebackend.common.ResponseCode;

/**
 * 返回工具类
 */
public class ResultUtil {

    /**
     * 成功
     * @param data 响应数据
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<T>(200,data,"ok");
    }

    /**
     * 成功
     * @param data 响应数据
     * @param message 自定义响应信息
     */
    public static <T> BaseResponse<T> success(T data,String message){
        return new BaseResponse<T>(200, data, message);
    }

    /**
     * 错误
     * @param code 错误码
     * @param message 错误信息
     */
    public static <T> BaseResponse<T> error(int code, String message){
        return new BaseResponse<T>(code, null, message);
    }

    /**
     * 错误
     * @param responseCode 错误码
     */
    public static <T> BaseResponse<T> error(ResponseCode responseCode){
        return new BaseResponse<T>(responseCode, null, responseCode.getMessage());
    }

    /**
     * 错误
     * @param responseCode 错误码
     * @param data 数据
     */
    public static <T> BaseResponse<T> error(ResponseCode responseCode, T data){
        return new BaseResponse<T>(responseCode, data, responseCode.getMessage());
    }

    /**
     * 错误
     * @param responseCode 错误码
     * @param message 自定义错误信息
     */
    public static <T> BaseResponse<T> error(ResponseCode responseCode, String message){
        return new BaseResponse<T>(responseCode, null, message);
    }

    /**
     * 错误
     * @param responseCode 错误码
     * @param data 数据
     * @param message 自定义错误信息
     */
    public static <T> BaseResponse<T> error(ResponseCode responseCode, T data, String message){
        return new BaseResponse<T>(responseCode, data, message);
    }

}
