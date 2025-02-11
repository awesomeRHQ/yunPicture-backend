package com.awesome.yunpicturebackend.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RolePermission implements Serializable {

    /**
     * 角色键
     */
    private String role;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 描述
     */
    private String description;

    private static final long serialVersionUID = 1L;
}
