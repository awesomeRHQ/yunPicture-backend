package com.awesome.yunpicturebackend.util;

import javax.servlet.http.HttpServletRequest;

import static com.awesome.yunpicturebackend.model.enums.BrowserEnum.*;

/**
 * 客户端信息工具
 */
public class ClientInfoUtil {

    /**
     * 获取请求点ip地址
     * @param request 请求
     * @return 客户端ip
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For"); // 可能包含多个 IP 地址
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr(); // 获取直接的远程地址
        }
        // 如果是通过代理，X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取客户端请求的浏览器信息
     * @param userAgent 请求的agent信息
     * @return 浏览器名称
     */
    public static String getBrowserByUserAgent(String userAgent){
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("MSIE")) {
            return INTERNET_EXPLORER.getValue();
        } else if (userAgent.contains("Trident")) { // IE 11+
            return INTERNET_EXPLORER.getValue();
        } else if (userAgent.contains("Edge")) {
            return MICROSOFT_EDGE.getValue();
        } else if (userAgent.contains("Chrome")) {
            return GOOGLE_CHROME.getValue();
        } else if (userAgent.contains("Firefox")) {
            return MOZILLA_FIREFOX.getValue();
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            return SAFARI.getValue();
        } else {
            return "Unknown";
        }
    }

}
