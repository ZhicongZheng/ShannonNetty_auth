package com.shannon.server.handler;

import com.shannon.common.enums.MsgType;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.NettySocketHolder;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Socket心跳事件处理器
 * @author zzc
 */
@Slf4j
@Component
public class ServerHeartHandler extends ChannelInboundHandlerAdapter {

    private static final SocketMsg<String> PING = new SocketMsg<String>().setId(1).setType(MsgType.PING_VALUE).setContent("ping");
    private static final SocketMsg<String> PONG = new SocketMsg<String>().setId(1).setType(MsgType.PONG_VALUE).setContent("pong");
    /**失败计数器：未收到客户端发送的ping请求*/
    private int unRecPingTimes = 0;
    /**定义客户端没有收到服务端的pong消息的最大次数*/
    private static final int MAX_UN_REC_PING_TIMES = 3;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("客户端【{}】断开连接",ctx.channel().remoteAddress());
        NettySocketHolder.remove(ctx.channel());
    }

    /**
     * 用户事件处理器
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                if(unRecPingTimes >= MAX_UN_REC_PING_TIMES){
                    // 连续超过N次未收到客户端的ping消息，那么关闭该通道，等待client重连
                    log.info("网关3次心跳没有应答，判断下线，关闭通道");
                    ctx.channel().close();
                    NettySocketHolder.remove(ctx.channel());
                }else{
                    // 失败计数器加1
                    unRecPingTimes++;
                }
                log.info("向客户端发送心跳...");
                ctx.writeAndFlush(PING).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }

        super.userEventTriggered(ctx, evt);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        SocketMsg msg = (SocketMsg) message;
        switch (msg.getType()){
            case MsgType.PING_VALUE:
                log.info("收到客户端的心跳,此时连接的客户端有{}个",NettySocketHolder.getMap().size());
                ctx.writeAndFlush(PONG);
                break;
            case MsgType.PONG_VALUE:
                if (unRecPingTimes>0){
                    unRecPingTimes--;
                }
                break;
            default:ctx.fireChannelRead(message);break;
        }
        ReferenceCountUtil.release(msg);
    }
}
