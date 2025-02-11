package com.awesome.yunpicturebackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.manager.auth.model.ModuleRolePermissions;
import com.awesome.yunpicturebackend.manager.auth.model.ModuleRolePermissionsConfig;
import com.awesome.yunpicturebackend.manager.auth.model.RolePermission;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ModuleRolePermissionsManager {

    public static final ModuleRolePermissionsConfig MODULE_ROLE_PERMISSIONS_CONFIG;

    static{
        String json = ResourceUtil.readUtf8Str("biz/ModuleRolePermissionsConfig.json");
        MODULE_ROLE_PERMISSIONS_CONFIG = JSONUtil.toBean(json, ModuleRolePermissionsConfig.class);
    }

    /**
     * 获取配置类的模块角色权限列表
     */
    public List<ModuleRolePermissions> getModelRolePermissions(){
        return MODULE_ROLE_PERMISSIONS_CONFIG.getModuleRolePermissions();
    }

    /**
     * 根据模块名称获取全部角色权限列表
     * @param modelName 模块名称
     * @return 当前模块的全部角色权限列表
     */
    public List<RolePermission> getRolePermissionsByModelName(String modelName){
        ThrowUtil.throwIf(modelName.isBlank(),ResponseCode.OPERATION_ERROR,"模块名称格式错误");
        List<ModuleRolePermissions> moduleRolePermissions = this.getModelRolePermissions();
        if (moduleRolePermissions.isEmpty()){
            throw new BusinessException(ResponseCode.OPERATION_ERROR,"权限列表为空");
        }
        // 遍历模块角色权限列表
        for(ModuleRolePermissions modelRolePermission : moduleRolePermissions){
            // 若找到对应模块名称,则返回角色权限列表
            if (modelName.equals(modelRolePermission.getModule())){
                return modelRolePermission.getRolePermissions();
            }
        }
        return new ArrayList<>();
    }

    /**
     * 根据模块名称和角色获取权限列表
     * @param modelName 模块名称
     * @param role 角色
     * @return 当前模块的角色权限列表
     */
    public List<String> getPermissionsByModuleAndRole(String modelName, String role){
        ThrowUtil.throwIf(StrUtil.isBlank(modelName),ResponseCode.OPERATION_ERROR,"模块名称格式错误");
        ThrowUtil.throwIf(StrUtil.isBlank(role),ResponseCode.OPERATION_ERROR,"角色格式错误");
        // 获取模块全部角色权限列表
        List<RolePermission> rolePermissions = this.getRolePermissionsByModelName(modelName);
        // 遍历角色权限列表
        for (RolePermission rolePermission : rolePermissions){
            // 若找到对应角色名称,则返回权限列表
            if (role.equals(rolePermission.getRole())){
                return rolePermission.getPermissions();
            }
        }
        return new ArrayList<>();
    }

}
