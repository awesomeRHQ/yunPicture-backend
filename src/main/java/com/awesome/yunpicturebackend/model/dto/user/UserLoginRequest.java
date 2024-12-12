package com.awesome.yunpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求类
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -8354817822806318827L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

}
