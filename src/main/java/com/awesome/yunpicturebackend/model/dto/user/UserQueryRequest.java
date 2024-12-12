package com.awesome.yunpicturebackend.model.dto.user;

import com.awesome.yunpicturebackend.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户查询请求类
 */
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户角色
     */
    private String userRole;

    private static final long serialVersionUID = -4513226680361286985L;

}
