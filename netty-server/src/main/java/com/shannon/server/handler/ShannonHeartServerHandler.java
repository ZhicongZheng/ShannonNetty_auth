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

import java.security.KeyPair;

/**
 * Socket服务器事件处理器
 */
@Slf4j
public class ShannonHeartServerHandler extends SimpleChannelInboundHandler<SocketMsg> {

    private static final ByteBuf HEART_BEAT = Unpooled.unreleasableBuffer(
            Unpooled.copiedBuffer(new SocketMsg().setId(1).setType(MsgType.HEART_BEAT).setContent("pong").toString(), CharsetUtil.UTF_8));

    /**
     * 取消绑定
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("{} 通道退出",ctx.name());
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
                log.info("服务端已经5秒没有收到信息,向客户端发送心跳");
                //向客户端发送消息
                ctx.writeAndFlush(HEART_BEAT).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    /**
     * 从通道中读取消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocketMsg socketMsg) {
        log.info("收到customProtocol={}", socketMsg);
        switch (socketMsg.getType()){
            case HEART_BEAT:
                log.info("收到客户端的心跳");
            case DH_SENDPUBKEY:
                log.info("收到客户端秘钥协商消息");
                KeyPair keyPair = ECCUtil.initKey();
                ByteBuf PublicKeyStr = Unpooled.unreleasableBuffer(
                        Unpooled.copiedBuffer(new SocketMsg().setId(1).setType(MsgType.HEART_BEAT)
                                .setContent(ECCUtil.getPublicKeyStr(keyPair)).toString(), CharsetUtil.UTF_8));
                ctx.writeAndFlush(PublicKeyStr);
        }

        NettySocketHolder.put(socketMsg.getId(), (NioSocketChannel) ctx.channel());
    }
}
