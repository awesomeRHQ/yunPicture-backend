package com.awesome.yunpicturebackend.manager.auth.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户权限判别类
 */
@Data
public class AuthContext implements Serializable {

    private static final long serialVersionUID = 6304127097383458330L;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 操作名称
     */
    private String opName;

    /**
     * 数据Id，临时参数
     */
    private Long id;

    /**
     * 图片Id
     */
    private Long pictureId;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 空间Id
     */
    private Long spaceId;

    /**
     * 空间用户Id
     */
    private Long spaceUserId;

}
