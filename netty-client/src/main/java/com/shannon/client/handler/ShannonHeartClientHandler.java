package com.shannon.client.handler;

import com.shannon.common.enums.MsgType;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.ECCUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Socket服务器事件处理器
 *
 */
@Slf4j
public class ShannonHeartClientHandler extends SimpleChannelInboundHandler<String> {

    private static final ByteBuf PING = Unpooled.unreleasableBuffer(
            Unpooled.copiedBuffer(new SocketMsg().setId(1).setType(MsgType.PING_VALUE).setContent("ping").toString()
                    +System.getProperty("line.separator"), CharsetUtil.UTF_8));

    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                log.info("向服务端发送心跳...");
                ctx.writeAndFlush(PING).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }

        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 当被通知该 channel 是活动的时候就发送信息
     * 连接上客户端之后，就发送认证消息，主动与服务端协商秘钥
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(sendDH());
    }

    /**
     *  每当从服务端接收到新数据时，都会使用收到的消息调用此方法 channelRead0(),在此示例中，接收消息的类型是ByteBuf。
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) {
        //从服务端收到消息时被调用
        log.info("客户端收到消息={}", msg);
    }

    private ByteBuf sendDH(){
        String publicKey = ECCUtil.getPublicKeyStr(ECCUtil.initKey());
        return Unpooled.unreleasableBuffer(
                Unpooled.copiedBuffer(new SocketMsg().setId(1).setType(MsgType.AUTH_VALUE).setContent(publicKey).toString()
                        +System.getProperty("line.separator"), CharsetUtil.UTF_8));
    }
}
