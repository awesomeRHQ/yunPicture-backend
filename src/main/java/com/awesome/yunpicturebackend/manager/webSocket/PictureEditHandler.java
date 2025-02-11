package com.awesome.yunpicturebackend.manager.webSocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.awesome.yunpicturebackend.manager.webSocket.disruptor.PictureEditEventProducer;
import com.awesome.yunpicturebackend.manager.webSocket.model.PictureEditActionEnum;
import com.awesome.yunpicturebackend.manager.webSocket.model.PictureEditMessageTypeEnum;
import com.awesome.yunpicturebackend.manager.webSocket.model.PictureEditRequestMessage;
import com.awesome.yunpicturebackend.manager.webSocket.model.PictureEditResponseMessage;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图片编辑消息处理器
 * WebSocket 处理器
 * 在连接成功、连接关闭、接收到客户端消息时进行处理
 */
@Component
@Slf4j
public class PictureEditHandler extends TextWebSocketHandler {

    @Resource
    private UserService userService;

    @Resource
    private PictureEditEventProducer pictureEditEventProducer;

    // 每张图片的编辑状态，key: pictureId, value: 当前 “正在编辑” 的用户 ID
    private final Map<Long, Long> currentEditingUserMap = new ConcurrentHashMap<>();

    // 保存所有连接的会话，key: pictureId, value: 用户会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    /**
     * 连接后执行的逻辑
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 1.保存会话到集合中
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        User loginUser = (User) session.getAttributes().get("loginUser");
        pictureSessions.putIfAbsent(pictureId,ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);
        // 2.构造响应信息
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("%s加入编辑",loginUser.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(loginUser));
        // 3.广播信息给同一图片的用户
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    /**
     * 关闭连接时执行的逻辑
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        Long pictureId = (Long) attributes.get("pictureId");
        User loginUser = (User) attributes.get("loginUser");
        // 1.移除当前用户的编辑状态
        handleExitEditMessage(null,session,loginUser,pictureId);
        // 2.移除当前用户的会话
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (sessionSet != null) {
            // 判断session集合中是否有当前用户
            if (sessionSet.contains(session)) {
                sessionSet.remove(session);
                // 如果session集合为空，则删除当前图片的会话节点
                if (sessionSet.isEmpty()) {
                    pictureSessions.remove(pictureId);
                }
            }
        }
        // 3.构造响应信息
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
        String message = String.format("%s退出编辑图片",loginUser.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(loginUser));
        // 4.广播编辑信息
        broadcastToPicture(pictureId,pictureEditResponseMessage);
    }

    /**
     * 消息处理器
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 将消息解析为 PictureEditMessage
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.valueOf(type);

        // 从 Session 属性中获取公共参数
        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user");
        Long pictureId = (Long) attributes.get("pictureId");

        // 调用对应的消息处理方法
        // 发布到Disruptor中
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage,session,user,pictureId);
    }

    /**
     * 处理用户退出编辑的消息
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     * @throws Exception
     */
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        // 1.判断当前用户是否正在编辑
        Long editUserId = currentEditingUserMap.get(pictureId);
        if (editUserId != null && editUserId.equals(user.getId())) {
            // 2.移除编辑列表
            currentEditingUserMap.remove(pictureId);
            // 3.构造响应信息
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String message = String.format("%s退出编辑图片",user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            // 4.广播编辑信息
            broadcastToPicture(pictureId,pictureEditResponseMessage);
        }
    }

    /**
     * 处理用户编辑动作的消息
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     * @throws Exception
     */
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        // 1.获取基本信息
        Long editUserId = currentEditingUserMap.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (pictureEditActionEnum == null) {
            return;
        }
        // 2.确认用户是当前编辑人
        if (editUserId != null && editUserId.equals(user.getId())) {
            // 3.构造响应信息
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            String message = String.format("%s执行%s",user.getUserName(),pictureEditActionEnum.getValue());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setEditAction(editAction);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            // 4.将编辑信息广播给除当前用户外图片的其他用户
            broadcastToPicture(pictureId,pictureEditResponseMessage,session);
        }
    }

    /**
     * 处理用户进入编辑的消息
     * @param pictureEditRequestMessage 图片编辑请求信息
     * @param session 当前回话
     * @param user 当前用户
     * @param pictureId 当前图片Id
     */
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        // 1.判断没有用户编辑才能进入编辑
        if (!currentEditingUserMap.containsKey(pictureId)) {
            // 2.保存当前编辑用户
            currentEditingUserMap.put(pictureId,user.getId());
            // 3.构造响应信息
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("%s开始编辑图片",user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            // 4.广播编辑信息
            broadcastToPicture(pictureId,pictureEditResponseMessage);
        }
    }

    /**
     * 广播消息
     * @param pictureId 图片Id
     * @param pictureEditResponseMessage 图片编辑响应信息
     * @param excludeSession 需要排除的会话
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws Exception {
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessionSet)) {
            // 创建 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            //region 配置序列化：将 Long 类型转为 String，解决丢失精度问题
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance); // 支持 long 基本类型
            objectMapper.registerModule(module);
            //endregion
            // 序列化为 JSON 字符串
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);
            // 遍历会话集合，逐个发送信息
            for (WebSocketSession session : sessionSet) {
                // 排除掉的 session 不发送
                if (excludeSession != null && excludeSession.equals(session)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    /**
     * 给全部会话都广播
     * @param pictureId
     * @param pictureEditResponseMessage
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }

}
