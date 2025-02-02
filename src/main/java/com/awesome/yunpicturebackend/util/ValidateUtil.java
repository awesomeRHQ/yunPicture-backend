package com.awesome.yunpicturebackend.util;

/**
 * 数据校验工具类
 */
public class ValidateUtil {

    /**
     * 校验是否为null或非正数
     */
    public static boolean isNullOrNotPositive(Long number) {
        return number == null || number <= 0;
    }

    /**
     * 校验是否为空或为负数
     */
    public static boolean isNullOrNegative(Long number) {
        return number == null || number < 0;
    }


}
