package com.shannon.server.handler;

import com.shannon.common.enums.MsgType;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.ECCUtil;
import com.shannon.server.util.NettySocketHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.KeyPair;

/**
 * Socket服务器事件处理器
 */
@Slf4j
@Component
public class ShannonHeartServerHandler extends SimpleChannelInboundHandler<String> {

    private static final ByteBuf PONG = Unpooled.unreleasableBuffer(
            Unpooled.copiedBuffer(new SocketMsg().setId(1).setType(MsgType.PONG_VALUE).setContent("pong").toString()
                    +System.getProperty("line.separator"), CharsetUtil.UTF_8));

    /**
     * 取消绑定
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("{} 通道退出",ctx.channel().remoteAddress());
        NettySocketHolder.remove((NioSocketChannel) ctx.channel());
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
                ctx.writeAndFlush(PONG).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    /**
     * 从通道中读取消息
     */
    /*@Override
    protected void channelRead0(ChannelHandlerContext ctx, SocketMsg socketMsg) {

        switch (socketMsg.getType()){
            case MsgType.PING_VALUE:
                log.info("收到客户端的心跳");
            case MsgType.AUTH_VALUE:
                log.info("收到客户端秘钥协商消息");
                KeyPair keyPair = ECCUtil.initKey();
                ByteBuf PublicKeyStr = Unpooled.unreleasableBuffer(
                        Unpooled.copiedBuffer(new SocketMsg().setId(1).setType(MsgType.AUTH_BACK_VALUE)
                                .setContent(ECCUtil.getPublicKeyStr(keyPair)).toString(), CharsetUtil.UTF_8));
                ctx.writeAndFlush(PublicKeyStr);
        }

        NettySocketHolder.put(socketMsg.getId(), (NioSocketChannel) ctx.channel());
    }*/

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("收到客户端消息：{}", msg);
    }
}
