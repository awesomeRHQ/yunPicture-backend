package com.awesome.yunpicturebackend.aop;

import cn.hutool.core.util.StrUtil;
import com.awesome.yunpicturebackend.annotation.ModulePermissionCheck;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.manager.auth.AuthContextUtil;
import com.awesome.yunpicturebackend.manager.auth.ModuleRolePermissionsManager;
import com.awesome.yunpicturebackend.manager.auth.model.AuthContext;
import com.awesome.yunpicturebackend.manager.auth.model.ModuleRolePermissionsConfig;
import com.awesome.yunpicturebackend.model.entity.Picture;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.service.PictureService;
import com.awesome.yunpicturebackend.service.SpaceUserService;
import com.awesome.yunpicturebackend.service.UserService;
import javassist.expr.NewArray;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Component
@Aspect
@Slf4j
public class ModulePermissionInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private ModuleRolePermissionsManager moduleRolePermissionsManager;

    @Resource
    private AuthContextUtil authContextUtil;

    @Around("@annotation(modulePermissionCheck)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, ModulePermissionCheck modulePermissionCheck) throws Throwable {
        // 1.获取当前请求的HttpServletRequest request对象
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 2.获取当前用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ResponseCode.NOT_LOGIN_ERROR);
        }
        // 3.获取请求上下文
        AuthContext authContext = authContextUtil.getAuthContext(request);
        String moduleName = authContext.getModuleName();
        ThrowUtil.throwIf(StrUtil.isBlank(moduleName),ResponseCode.OPERATION_ERROR,"无模块信息");
        String opName = authContext.getOpName();
        ThrowUtil.throwIf(StrUtil.isBlank(opName),ResponseCode.OPERATION_ERROR,"无操作信息");
        // 4.获取用户的权限列表
        List<String> permissions = getCurrentUserPermissions(authContext,loginUser);
        // 5.从权限列表中判断是否有当前操作的权限
        // 权限列表为空，表示为无权限（游客）
        if (permissions.isEmpty()) {
            throw new BusinessException(ResponseCode.NO_AUTH_ERROR);
        }
        boolean hasPermission = permissions.contains(opName);
        if (hasPermission) {
            joinPoint.proceed();
        } else {
            throw new BusinessException(ResponseCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }

    /**
     * 获取当前用户的模块角色权限列表
     * @param authContext 权限信息上下文
     * @param loginUser 当前用户
     * @return 模块角色权限列表
     */
    private List<String> getCurrentUserPermissions(AuthContext authContext, User loginUser){
        List<String> permissions = new ArrayList<>();
        String moduleName = authContext.getModuleName();
        String opName = authContext.getOpName();
        String userRole = "";
        // 获取用户角色
        // 如果是管理操作，直接根据全局权限判断
        if (StrUtil.isNotBlank(opName) && opName.equals("manage")) {
            userRole = loginUser.getUserRole();
            if (StrUtil.isNotBlank(moduleName) && StrUtil.isNotBlank(userRole)) {
                permissions = moduleRolePermissionsManager.getPermissionsByModuleAndRole("main", userRole);
            }
        } else {
            // 不同模块有不同的用户角色
            switch (moduleName){
                case "picture":
                    // 有空间角色表Id，直接查询用户角色
                    if (authContext.getSpaceUserId() != null) {
                        userRole = spaceUserService.getSpaceUserRole(authContext.getSpaceUserId());
                    }
                    // 有空间Id，直接查询用户角色
                    else if(authContext.getSpaceId() != null) {
                        userRole = spaceUserService.getSpaceUserRole(authContext.getSpaceId(), loginUser.getId());
                    }
                    // 有图片Id，通过图片Id查询空间Id
                    else if (authContext.getPictureId() != null) {
                        Picture picture = pictureService.getById(authContext.getPictureId());
                        Long spaceId = picture.getSpaceId();
                        userRole = spaceUserService.getSpaceUserRole(spaceId, loginUser.getId());
                    } else {
                        userRole = loginUser.getUserRole();
                    }
                    break;
                case "space_user":
                    // 有空间角色表Id，直接查询用户角色
                    if (authContext.getSpaceUserId() != null) {
                        userRole = spaceUserService.getSpaceUserRole(authContext.getSpaceUserId());
                    }
                    // 有空间Id，直接查询用户角色
                    else if(authContext.getSpaceId() != null) {
                        userRole = spaceUserService.getSpaceUserRole(authContext.getSpaceId(), loginUser.getId());
                    }
                default:
            }
            if (StrUtil.isNotBlank(moduleName) && StrUtil.isNotBlank(userRole)) {
                permissions = moduleRolePermissionsManager.getPermissionsByModuleAndRole(moduleName, userRole);
            }
        }
        return permissions;
    }

}
