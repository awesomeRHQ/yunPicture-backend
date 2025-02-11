package com.awesome.yunpicturebackend.util;

import java.util.List;

/**
 * 数据校验工具类
 */
public class ValidateUtil {

    /**
     * 校验number是否为null或非正数
     */
    public static boolean isNullOrNotPositive(Long number) {
        return number == null || number <= 0;
    }

    /**
     * 校验number是否为空或为负数
     */
    public static boolean isNullOrNegative(Long number) {
        return number == null || number < 0;
    }

    /**
     * 校验list是否为空或为负数
     */
    public static boolean isNullOrEmpty(List list) {
        return list == null || list.isEmpty();
    }


}
