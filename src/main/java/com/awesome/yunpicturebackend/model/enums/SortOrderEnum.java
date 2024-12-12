package com.awesome.yunpicturebackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum SortOrderEnum {

    ASC("顺序","asc"),
    DESC("倒序","desc");


    private final String text;

    private final String value;

    SortOrderEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据枚举值获取枚举
     * @param value 枚举值
     * @return 枚举
     */
    public static SortOrderEnum getEnumByValue(String value){
        if (ObjectUtil.isEmpty(value)){
            return null;
        }
        for (SortOrderEnum sortOrderEnum : SortOrderEnum.values()){
            if (sortOrderEnum.value.equals(value)){
                return sortOrderEnum;
            }
        }
        return null;
    }

}
