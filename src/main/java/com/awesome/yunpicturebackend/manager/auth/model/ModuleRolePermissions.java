package com.awesome.yunpicturebackend.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ModuleRolePermissions implements Serializable {

    /**
     * 模块名
     */
    private String module;

    /**
     * 角色权限列表
     */
    private List<RolePermission> rolePermissions;

    private static final long serialVersionUID = 1L;

}
