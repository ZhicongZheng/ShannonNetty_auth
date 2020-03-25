package com.shannon.server.handler;

import com.shannon.common.enums.MsgType;
import com.shannon.common.model.ECKeys;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.EncryptOrDecryptUtil;
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

import javax.crypto.Cipher;

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
    protected void channelRead0(ChannelHandlerContext ctx, SocketMsg msg) {
        switch (msg.getType()){
            case MsgType.PING_VALUE:
                log.info("收到客户端的心跳");
                ctx.writeAndFlush(PONG);
                break;
            case MsgType.AUTH_VALUE:
                log.info("收到客户端秘钥协商消息,{}",msg.getContent());

                ECKeys ecKeys = EncryptOrDecryptUtil.getEcKeys();
                System.out.println("【服务端初始化公钥serPubKey】" + ecKeys.getPubKey());
                System.out.println("【服务端初始化私钥serPriKey】" + ecKeys.getPriKey());
                SocketMsg PublicKeyStr = new SocketMsg().setId(1).setType(MsgType.AUTH_BACK_VALUE)
                        .setContent(ecKeys.getPubKey());

                String key = EncryptOrDecryptUtil.ecdhKey(ecKeys.getPriKey(),msg.getContent());
                log.info("服务端协商出秘钥：{}",key);
                //将改秘钥加入秘钥库
                AESKeyMap.put(ctx.channel(),key);

                ctx.writeAndFlush(PublicKeyStr);
                break;
            case MsgType.AUTH_CHECK_VALUE:
                log.info("服务端验证秘钥");
                //获取此通道对应的秘钥
                String k = AESKeyMap.get(ctx.channel());
                //确认秘钥的正确性
                String login = EncryptOrDecryptUtil.doAES(msg.getContent(),k, Cipher.DECRYPT_MODE);
                log.info("解密出客户端登录数据:{}",login);
                if (!("login").equals(login)){
                    log.info("非法客户端，关闭连接");
                    ctx.channel().close();
                }
                log.info("客户端{}验证通过，加入连接列表",ctx.name());
                NettySocketHolder.put(msg.getId(), (NioSocketChannel) ctx.channel());
                break;
            default:break;
        }


    }
}
