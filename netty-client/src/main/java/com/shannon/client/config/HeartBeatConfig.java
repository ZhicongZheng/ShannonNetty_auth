package com.shannon.client.config;

import com.shannon.common.enums.MsgType;
import com.shannon.common.model.SocketMsg;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置客户端的id和心跳消息
 */
@Configuration
public class HeartBeatConfig {

    @Value("${channel.id}")
    private long id;

    @Bean(value = "heartBeat")
    public SocketMsg heartBeat(){
        return new SocketMsg()
                .setId(id).setType(MsgType.HEART_BEAT).setContent("pong");
    }
}
