package com.shannon.client.handler;

import com.shannon.common.enums.MsgType;
import com.shannon.common.model.ECKeys;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.EncryptOrDecryptUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;

/**
 * Socket服务器事件处理器
 *
 */
@Slf4j
public class ShannonHeartClientHandler extends SimpleChannelInboundHandler<SocketMsg> {
    //AES加密的秘钥
    private static String key = null;
    //ECC的公钥和私钥
    private static ECKeys ecKeys =null;

    private static final SocketMsg PING =new SocketMsg().setId(1).setType(MsgType.PING_VALUE).setContent("ping");

    private static final SocketMsg PONG =new SocketMsg().setId(1).setType(MsgType.PONG_VALUE).setContent("pong");

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
    protected void channelRead0(ChannelHandlerContext ctx, SocketMsg msg) {
        //从服务端收到消息时被调用
        switch (msg.getType()){
            case MsgType.PING_VALUE:
                log.info("收到服务端的心跳");
                ctx.writeAndFlush(PONG);
                break;
            case MsgType.AUTH_BACK_VALUE:
                log.info("收到服务端公钥，开始秘钥协商:{}",msg.getContent());

                key = EncryptOrDecryptUtil.ecdhKey(ecKeys.getPriKey(),msg.getContent());
                log.info("客户端开始秘钥协商完成，开始验证秘钥正确性,秘钥为:{}",key);
                String content = EncryptOrDecryptUtil.doAES("login",key, Cipher.ENCRYPT_MODE);
                SocketMsg login = new SocketMsg()
                        .setId(1).setType(MsgType.AUTH_CHECK_VALUE).setContent(content);

                ctx.writeAndFlush(login);
                break;
            default:break;
        }
    }

    private SocketMsg sendDH(){
        ecKeys = EncryptOrDecryptUtil.getEcKeys();
        System.out.println("【客户端初始化公钥cliPubKey】" + ecKeys.getPubKey());
        System.out.println("【客户端初始化私钥cliPriKey】" + ecKeys.getPriKey());
        return new SocketMsg().setId(1).setType(MsgType.AUTH_VALUE).setContent(ecKeys.getPubKey());
    }
}
