package com.awesome.yunpicturebackend.util;

/**
 * 字符串处理工具
 */
public class StringUtil {

    /**
     * 根据前缀截断并获取截断后的字符串
     * @param originStr 初始字符串
     * @param prefixStr 前缀
     * @return 截断后的字符串
     */
    public static String getTruncatedString(String originStr, String prefixStr) {
        if (originStr == null || originStr.isEmpty() ) {
            return null;
        }
        return originStr.substring(prefixStr.length());
    }
}
