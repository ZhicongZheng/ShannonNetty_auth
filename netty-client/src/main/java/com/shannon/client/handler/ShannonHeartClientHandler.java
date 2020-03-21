package com.shannon.client.handler;

import com.shannon.common.enums.MsgType;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.ECCUtil;
import com.shannon.client.util.SpringBeanFactory;
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
public class ShannonHeartClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                log.info("客户端已经10秒没收到消息了");
                //向服务端发送消息
                SocketMsg heartBeat = SpringBeanFactory.getBean("heartBeat", SocketMsg.class);
                ctx.writeAndFlush(heartBeat).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }

        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 当被通知该 channel 是活动的时候就发送信息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String publicKey = ECCUtil.getPublicKeyStr(ECCUtil.initKey());
        SocketMsg msg = new SocketMsg()
                .setId(100).setType(MsgType.DH_SENDPUBKEY).setContent(publicKey);
        ctx.writeAndFlush(Unpooled.copiedBuffer((CharSequence) msg, CharsetUtil.UTF_8));
    }

    /**
     *  每当从服务端接收到新数据时，都会使用收到的消息调用此方法 channelRead0(),在此示例中，接收消息的类型是ByteBuf。
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        //从服务端收到消息时被调用
        log.info("客户端收到消息={}", byteBuf.toString(CharsetUtil.UTF_8));
    }
}
