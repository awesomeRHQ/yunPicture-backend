package com.awesome.yunpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 时间周期枚举
 */
@Getter
public enum TimePeriodEnum {

    INANHOUR("过去一小时内", "0"),
    INPASSTFHOURS("过去24小时内", "1"),
    INPASSWEEK("过去一周内", "2"),
    INPASSMONTH("过去一个月内", "3");


    private final String text;

    private final String value;



    /**
     * @param text 文本
     * @param value 值
     */
    TimePeriodEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static TimePeriodEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (TimePeriodEnum spaceLevelEnum : TimePeriodEnum.values()) {
            if (spaceLevelEnum.value.equals(value)) {
                return spaceLevelEnum;
            }
        }
        return null;
    }
}
