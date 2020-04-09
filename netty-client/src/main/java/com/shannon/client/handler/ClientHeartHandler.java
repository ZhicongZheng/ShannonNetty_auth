package com.shannon.client.handler;

import com.shannon.client.tcpclient.ShannonNettyClient;
import com.shannon.common.codec.JsonDecoder;
import com.shannon.common.codec.JsonDecoderAES;
import com.shannon.common.codec.JsonEncoder;
import com.shannon.common.codec.JsonEncoderAES;
import com.shannon.common.enums.MsgType;
import com.shannon.common.model.EcKeys;
import com.shannon.common.model.Gateway;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.EncryptOrDecryptUtil;
import com.shannon.common.util.NettySocketHolder;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import java.util.concurrent.TimeUnit;

/**
 * Socket服务器事件处理器
 *
 * @author zzc
 */
@Slf4j
public class ClientHeartHandler extends SimpleChannelInboundHandler<SocketMsg<String>> {
    /** ECC的公钥和私钥*/
    private static EcKeys ecKeys =null;
    @Value("gatewayId")
    private String gatewayId;
    @Autowired
    private ShannonNettyClient nettyClient;

    private static final SocketMsg<String> PING =new SocketMsg<String>().setGatewayId("1").setType(MsgType.PING_VALUE).setContent("ping");

    private static final SocketMsg<String> PONG =new SocketMsg<String>().setGatewayId("1").setType(MsgType.PONG_VALUE).setContent("pong");

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                //log.info("向服务端发送心跳...");
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
        ctx.writeAndFlush(clientInit());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //如果运行过程中服务端挂了,执行重连机制
        EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(() -> {
            try {
                nettyClient.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 10L, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    /**
     *  每当从服务端接收到新数据时，都会使用收到的消息调用此方法 channelRead0()
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocketMsg msg) throws InterruptedException {
        //从服务端收到消息时被调用
        switch (msg.getType()){
            case MsgType.PING_VALUE:
                //log.info("收到服务端的心跳");
                ctx.writeAndFlush(PONG);
                break;
            case MsgType.AUTH_BACK_VALUE:
                log.info("4.客户端开始秘钥协商，收到服务端公钥为:{}",msg.getContent());
                String key = EncryptOrDecryptUtil.ecdhKey(ecKeys.getPriKey(), (String) msg.getContent());
                Gateway gw = new Gateway()
                        .setGwId("1")
                        .setKey(key)
                        .setChannel(ctx.channel());
                NettySocketHolder.put("1",gw);
                log.info("5. 客户端秘钥协商完成，开始验证秘钥正确性,加密秘钥为:{}",key);
                String content = EncryptOrDecryptUtil.doAES("login",key, Cipher.ENCRYPT_MODE);
                SocketMsg<String> login = new SocketMsg<String>()
                        .setGatewayId("1").setType(MsgType.AUTH_CHECK_VALUE).setContent(content);
                ctx.writeAndFlush(login);
                break;
            case MsgType.LOGIN_SUCCESS_VALUE:
                log.info("8. 网关登陆成功，后续数据将使用AES进行加解密");
                //用AES加解密替换掉默认的编解码器
                ctx.pipeline().replace(ctx.pipeline().get("decoder"),"decoder",new JsonDecoderAES());
                ctx.pipeline().replace(ctx.pipeline().get("encoder"),"encoder",new JsonEncoderAES());
                //登录成功之后，才加入心跳机制
                ctx.pipeline().addFirst("idle",new IdleStateHandler(0,5, 0, TimeUnit.SECONDS));
                for (int i=0;i<10;i++){
                    String data = "测试加密数据";
                    SocketMsg<String> sendData = new SocketMsg<String>()
                            .setGatewayId("1")
                            .setType(MsgType.UPLOAD_DATA_VALUE)
                            .setContent(data)
                            .setSign(EncryptOrDecryptUtil.sign(data,ecKeys.getPriKey()));
                    ctx.writeAndFlush(sendData);
                    TimeUnit.SECONDS.sleep(5);
                }

                break;
            case MsgType.REFRESH_KEY_VALUE:
                log.info("服务端主动刷新秘钥");
                ctx.pipeline().replace(ctx.pipeline().get("decoder"),"decoder",new JsonDecoder());
                ctx.pipeline().replace(ctx.pipeline().get("encoder"),"encoder",new JsonEncoder());
                ctx.pipeline().remove("idle");
                ctx.writeAndFlush(clientInit());
                break;
            default:break;
        }
    }

    private SocketMsg<String> clientInit(){
        ecKeys = EncryptOrDecryptUtil.getEcKeys();
        log.info("【客户端初始化公钥cliPubKey】" + ecKeys.getPubKey());
        log.info("【客户端初始化私钥cliPriKey】" + ecKeys.getPriKey());
        log.info("1.客户端发起秘钥协商请求");
        return new SocketMsg<String>().setGatewayId("1").setType(MsgType.AUTH_VALUE).setContent(ecKeys.getPubKey());
    }

}
