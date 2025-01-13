package com.awesome.yunpicturebackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 审核状态枚举
 */
@Getter
public enum ReviewStatusEnum {

    REVIEWING("审核中",0),
    APPROVE("同意",1),
    REFUSE("不同意",2);

    private final String text;

    private final Integer value;

    ReviewStatusEnum(String text, Integer value){
        this.text = text;
        this.value = value;
    }

    /**
     * 根据枚举值获取枚举
     * @param value 枚举值
     * @return 枚举
     */
    public static ReviewStatusEnum getEnumByValue(Integer value){
        if (ObjectUtil.isEmpty(value)){
            return null;
        }
        for (ReviewStatusEnum reviewStatusEnum : ReviewStatusEnum.values()){
            if (reviewStatusEnum.value.equals(value)){
                return reviewStatusEnum;
            }
        }
        return null;
    }

}
