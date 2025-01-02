package com.awesome.yunpicturebackend.aop;

import cn.hutool.core.util.StrUtil;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.model.dto.picture.PictureLoadMoreRequest;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.entity.UserSearchLog;
import com.awesome.yunpicturebackend.model.enums.BrowserEnum;
import com.awesome.yunpicturebackend.service.UserSearchLogService;
import com.awesome.yunpicturebackend.service.UserService;
import com.awesome.yunpicturebackend.util.ClientInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 用户关键字查询时记录用户查询行为的切面
 */
@Aspect
@Component
@Slf4j
public class SearchWordInterceptor {

    @Resource
    private UserSearchLogService userSearchLogService;

    @Resource
    private UserService userService;

    @Around("execution(* com.awesome.yunpicturebackend.service.PictureService.listRecommendPictureVOBatch(..))")
    public Object doInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        // 1.验证方法参数
        // 获取切点方法的参数列表
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            log.error("SearchWordInterceptor：搜索关键词信息获取失败");
            result = joinPoint.proceed();
        }else {
            PictureLoadMoreRequest r = (PictureLoadMoreRequest) args[0];
            String searchText = r.getSearchText();
            if (StrUtil.isBlank(searchText)) {
                return joinPoint.proceed();
            }
            // 2.获取当前请求的HttpServletRequest request对象
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            // 3.获取当前用户的角色
            User loginUser = userService.getLoginUser(request);
            // 4.记录用户搜索日志信息
            UserSearchLog userSearchLog = new UserSearchLog();
            userSearchLog.setSearch_word(searchText);
            // 4.1获取用户地址和浏览器信息
            userSearchLog.setIp_address(ClientInfoUtil.getIpAddress(request));
            userSearchLog.setDevice_info(ClientInfoUtil.getBrowserByUserAgent(request.getHeader("User-Agent")));
            userSearchLog.setUser_id(loginUser.getId());
            userSearchLog.setCreate_time(new Date());
            userSearchLogService.save(userSearchLog);
            boolean isSuccessful = true;
            try{
                // 执行目标方法
                result = joinPoint.proceed();
            } catch (Exception e) {
                isSuccessful = false;
                userSearchLog.setErr_msg(e.getMessage());
            }
            // 5.更新日志执行结果数据
            userSearchLog.setIs_successful(isSuccessful);
            userSearchLogService.updateById(userSearchLog);
        }
        return result;

    }

}
