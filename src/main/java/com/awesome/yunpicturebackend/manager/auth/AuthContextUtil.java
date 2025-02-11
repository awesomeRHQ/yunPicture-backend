package com.awesome.yunpicturebackend.manager.auth;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.manager.auth.model.AuthContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class AuthContextUtil {

    @Value("${server.servlet.context-path}")
    private String CONTEXT_PATH;

    public AuthContext getAuthContext(HttpServletRequest request) {
        // 1.参数校验
        ThrowUtil.throwIf(request == null, ResponseCode.OPERATION_ERROR);
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        AuthContext authContext = new AuthContext();
        // 2.解析请求uri
        String uri = request.getRequestURI();
        // 根据uri获取当前模块 => api/module/...
        // 操作字符串，将"api/"去除
        String pathUri = uri.replace(CONTEXT_PATH + "/", ""); //  module/op/...
        // 获取 去除"api/"后，直到第一个"/"前的部分字符串
        String moduleName = StrUtil.subBefore(pathUri, "/", false);
        // 根据pathUri获取当前操作 => module/op/...
        String opUri = pathUri.replace(moduleName + "/", ""); //  op/...
        String opName = StrUtil.subBefore(opUri, "/", false);
        // 4.获取模块请求信息中的关键鉴别信息：pictureId，userId，spaceId
        // 兼容 get 和 post 操作
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = ServletUtil.getBody(request);
            authContext = JSONUtil.toBean(body, AuthContext.class);
        } else {
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            authContext = BeanUtil.toBean(paramMap, AuthContext.class);
        }
        // 根据请求路径区分 id 字段的含义
        Long id = authContext.getId();
        if (ObjUtil.isNotNull(id)) {
            switch (moduleName) {
                case "user":
                    authContext.setUserId(id);
                    break;
                case "picture":
                    authContext.setPictureId(id);
                    break;
                case "spaceUser":
                    authContext.setSpaceUserId(id);
                    break;
                case "space":
                    authContext.setSpaceId(id);
                    break;
                default:
            }
        }
        authContext.setModuleName(moduleName);
        authContext.setOpName(opName);
        return authContext;
    }
}

