package com.awesome.yunpicturebackend.aop;

import com.awesome.yunpicturebackend.annotation.AuthCheck;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.enums.UserRoleEnum;
import com.awesome.yunpicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 鉴权切面
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)") // 表示对使用了注解类型为AuthCheck的注解生效
    public Object doIntercept(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 1.获取当前请求的HttpServletRequest request对象
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 2.获取当前用户的角色
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum currentUserRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 3.根据注解的值进行鉴权
        String mustRole = authCheck.mustRole();
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 无权限限制，直接放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 以下需要有权限
        if (currentUserRoleEnum == null){
            throw new BusinessException(ResponseCode.NO_AUTH_ERROR);
        }
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(currentUserRoleEnum)) {
            throw new BusinessException(ResponseCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
