package com.awesome.yunpicturebackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum UserRoleEnum {

    USER("用户","user"),
    ADMIN("管理员","admin");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value){
        this.text = text;
        this.value = value;
    }

    /**
     * 根据枚举值获取枚举
     * @param value 枚举值
     * @return 枚举
     */
    public static UserRoleEnum getEnumByValue(String value){
        if (ObjectUtil.isEmpty(value)){
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()){
            if (userRoleEnum.value.equals(value)){
                return userRoleEnum;
            }
        }
        return null;
    }
}
