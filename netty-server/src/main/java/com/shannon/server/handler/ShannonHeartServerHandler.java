package com.shannon.server.handler;

import com.shannon.common.enums.MsgType;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.ECSignUtil;
import com.shannon.server.util.AESKeyMap;
import com.shannon.server.util.NettySocketHolder;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.KeyPair;

/**
 * Socket服务器事件处理器
 */
@Slf4j
@Component
public class ShannonHeartServerHandler extends SimpleChannelInboundHandler<SocketMsg> {

    private static final SocketMsg PING = new SocketMsg().setId(1).setType(MsgType.PING_VALUE).setContent("ping");
    private static final SocketMsg PONG = new SocketMsg().setId(1).setType(MsgType.PONG_VALUE).setContent("pong");

    /**
     * 取消绑定
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("{} 通道退出",ctx.channel().remoteAddress());
        NettySocketHolder.remove((NioSocketChannel) ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("{} 通道连接",ctx.channel().remoteAddress());
    }

    /**
     * 用户事件处理器
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                log.info("向客户端发送心跳...");
                //向客户端发送消息
                ctx.writeAndFlush(PING).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);;
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    /**
     * 从通道中读取消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocketMsg socketMsg) {
        switch (socketMsg.getType()){
            case MsgType.PING_VALUE:
                log.info("收到客户端的心跳");
                ctx.writeAndFlush(PONG);
                break;
            case MsgType.AUTH_VALUE:
                log.info("收到客户端秘钥协商消息,向客户端发送公钥");
                KeyPair keyPair = ECSignUtil.initKey();
                SocketMsg PublicKeyStr = new SocketMsg().setId(1).setType(MsgType.AUTH_BACK_VALUE)
                        .setContent(ECSignUtil.getPublicKeyStr(keyPair));
                log.info("服务端生成秘钥");
                String key = socketMsg.getContent()+PublicKeyStr;
                AESKeyMap.put(ctx.channel(),key);
                ctx.writeAndFlush(PublicKeyStr);
                break;
            case MsgType.AUTH_CHECK_VALUE:
                log.info("服务端验证秘钥");
                String k = AESKeyMap.get(ctx.channel());
                if (!socketMsg.getContent().equals("test")){
                    log.info("非法客户端，关闭连接");
                    ctx.channel().close();
                }
                break;
            default:break;
        }
        NettySocketHolder.put(socketMsg.getId(), (NioSocketChannel) ctx.channel());

    }
}
