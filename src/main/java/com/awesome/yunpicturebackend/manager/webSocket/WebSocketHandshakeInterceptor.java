package com.awesome.yunpicturebackend.manager.webSocket;

import cn.hutool.core.util.StrUtil;
import com.awesome.yunpicturebackend.manager.auth.ModuleRolePermissionsManager;
import com.awesome.yunpicturebackend.manager.auth.model.PermissionTypeEnum;
import com.awesome.yunpicturebackend.model.entity.Picture;
import com.awesome.yunpicturebackend.model.entity.Space;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.enums.space.SpaceTypeEnum;
import com.awesome.yunpicturebackend.service.PictureService;
import com.awesome.yunpicturebackend.service.SpaceService;
import com.awesome.yunpicturebackend.service.SpaceUserService;
import com.awesome.yunpicturebackend.service.UserService;
import com.awesome.yunpicturebackend.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * WebSocket握手请求拦截器
 * 进行WebSocket连接前要进行权限校验，如果用户没有团队空间的编辑权限，则不握手。
 */
@Component
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private ModuleRolePermissionsManager moduleRolePermissionsManager;

    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response,@NotNull WebSocketHandler wsHandler,@NotNull Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest){
            // 1.获取请求request
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 2.获取请求参数
            String pictureId = servletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)){
                log.error("缺少图片参数，拒绝握手");
                return false;
            }
            User loginUser = userService.getLoginUser(servletRequest);
            if (loginUser == null){
                log.error("未获取到当前用户，拒绝握手");
                return false;
            }
            Picture picture = pictureService.getById(pictureId);
            if (picture == null){
                log.error("未获取到图片，拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space = null;
            // 若空间Id有效
            if (ValidateUtil.isNullOrNotPositive(spaceId)){
                log.error("未获取到空间，拒绝握手");
                return false;
            }
            //查询空间信息
            space = spaceService.getById(spaceId);
            if (space == null){
                log.error("未获取到空间，拒绝握手");
                return false;
            }
            // 若不为团队空间，则不进行握手
            if (!SpaceTypeEnum.TEAM.getValue().equals(space.getSpaceType())){
                log.error("空间不是团队空间，拒绝握手");
                return false;
            }
            // 3.判断用户是否有编辑权限
            // 获取当前用户的空间角色
            String spaceUserRole = spaceUserService.getSpaceUserRole(spaceId, loginUser.getId());
            List<String> permissions = moduleRolePermissionsManager.getPermissionsByModuleAndRole("picture", spaceUserRole);
            // 如果权限列表中没有管理权限和编辑权限
            if (!permissions.contains(PermissionTypeEnum.MANAGE.getValue()) && !permissions.contains(PermissionTypeEnum.EDIT.getValue())){
                log.error("当前用户没有权限，拒绝握手");
                return false;
            }
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", pictureId);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
