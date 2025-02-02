package com.awesome.yunpicturebackend.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ModuleRolePermissionsConfig implements Serializable {

    /**
     * 模块权限列表
     */
    private List<ModuleRolePermissions> moduleRolePermissions;

    private static final long serialVersionUID = 1L;

}
