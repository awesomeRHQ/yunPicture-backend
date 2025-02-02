package com.awesome.yunpicturebackend.model.vo.space;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SpaceInfo implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间类型：0-私有 1-团队
     */
    private String spaceType;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private String spaceLevel;

    /**
     * 创建用户Id
     */
    private Long createUserId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date editTime;
}
