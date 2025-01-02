package com.awesome.yunpicturebackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum BrowserEnum {

    /**
     * IE
     */
    INTERNET_EXPLORER("Internet Explorer","Internet Explorer"),
    /**
     * Edge
     */
    MICROSOFT_EDGE("Microsoft Edge","Microsoft Edge"),
    /**
     * Chrome
     */
    GOOGLE_CHROME("Google Chrome","Google Chrome"),
    /**
     * Firefox
     */
    MOZILLA_FIREFOX("Mozilla Firefox","Mozilla Firefox"),
    /**
     * Safari
     */
    SAFARI("Safari","Safari");

    private String text;

    private String value;

    BrowserEnum(String text, String value){
        this.text = text;
        this.value = value;
    }

    /**
     * 根据枚举值获取枚举
     * @param value 枚举值
     * @return 枚举
     */
    public static BrowserEnum getEnumByValue(String value){
        if (ObjectUtil.isEmpty(value)){
            return null;
        }
        for (BrowserEnum browserEnum : BrowserEnum.values()){
            if (browserEnum.value.equals(value)){
                return browserEnum;
            }
        }
        return null;
    }

}
