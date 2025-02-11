package com.awesome.yunpicturebackend.manager.webSocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

/**
 * 自定义服务器websocket配置
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    @Resource
    private PictureEditHandler pictureEditHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(pictureEditHandler,"/ws/picture/edit") // 添加处理器
                .addInterceptors(webSocketHandshakeInterceptor) // 添加拦截器
                .setAllowedOrigins("*");
    }

}
