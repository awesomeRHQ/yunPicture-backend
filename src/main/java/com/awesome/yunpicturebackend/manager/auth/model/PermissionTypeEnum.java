package com.awesome.yunpicturebackend.manager.auth.model;

import cn.hutool.core.util.ObjUtil;
import com.awesome.yunpicturebackend.model.enums.TimePeriodEnum;
import lombok.Getter;

@Getter
public enum PermissionTypeEnum {

    MANAGE("MANAGE", "MANAGE", "拥有全部权限，包括增删改查和浏览"),
    EDIT("EDIT", "EDIT", "拥有部分权限，增改查和浏览"),
    READ("READ", "READ", "只拥有查询和浏览的权限");


    private final String text;

    private final String value;

    private final String description;

    /**
     * @param text 文本
     * @param value 值
     * @param description 描述
     */
    PermissionTypeEnum(String text, String value, String description) {
        this.text = text;
        this.value = value;
        this.description = description;
    }

    /**
     * 根据 value 获取枚举
     */
    public static PermissionTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PermissionTypeEnum spaceLevelEnum : PermissionTypeEnum.values()) {
            if (spaceLevelEnum.value.equals(value)) {
                return spaceLevelEnum;
            }
        }
        return null;
    }
    
}
