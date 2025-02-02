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

    @Around("@annotation(modulePermissionCheck)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, ModulePermissionCheck modulePermissionCheck) throws Throwable {
        // 1.获取当前请求的HttpServletRequest request对象
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 2.获取请求上下文
        AuthContext authContext = AuthContextUtil.getAuthContext(request);
        // 3.获取当前用户
        User loginUser = userService.getLoginUser(request);
        // 3.获取空间用户的权限列表
        String moduleName = authContext.getModuleName();
        ThrowUtil.throwIf(StrUtil.isBlank(moduleName),ResponseCode.OPERATION_ERROR,"无模块信息");
        String opName = authContext.getOpName();
        ThrowUtil.throwIf(StrUtil.isBlank(opName),ResponseCode.OPERATION_ERROR,"无操作信息");
        List<String> permissions = new ArrayList<>();
        // 若为用户模块
        switch (moduleName){
            case "picture":
                Picture picture = pictureService.getById(authContext.getId());
                Long spaceId = picture.getSpaceId();
                String spaceUserRole = spaceUserService.getSpaceUserRole(spaceId, loginUser.getId());
                permissions = moduleRolePermissionsManager.getPermissionsByRoleName(moduleName, spaceUserRole);
                break;
            default:
        }
        // 4.从权限列表中判断是否有当前操作的权限
        // 权限列表为空，表示为游客
        if (permissions.isEmpty()) {
            throw new BusinessException(ResponseCode.NO_AUTH_ERROR);
        }
        boolean hasPermission = permissions.contains(opName);
        // 5.判断是否具有权限
        if (hasPermission) {
            joinPoint.proceed();
        } else {
            throw new BusinessException(ResponseCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }

}
