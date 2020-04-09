package com.shannon.server.handler;

import com.shannon.common.codec.JsonDecoderAES;
import com.shannon.common.codec.JsonEncoderAES;
import com.shannon.common.enums.MsgType;
import com.shannon.common.model.EcKeys;
import com.shannon.common.model.Gateway;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.EncryptOrDecryptUtil;
import com.shannon.common.util.NettySocketHolder;
import com.shannon.server.util.SpringBeanFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import java.util.concurrent.TimeUnit;

/**
 * 服务端认证处理器
 * @author zzc
 */
@Slf4j
public class ServerAuthHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("客户端【{}】连接，开始认证",ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        SocketMsg msg = (SocketMsg) message;
        try {
            switch (msg.getType()){
                case MsgType.AUTH_VALUE:
                    String clientPubKey = (String) msg.getContent();
                    String gwId = msg.getGatewayId();
                    if (StringUtils.isEmpty(clientPubKey) || StringUtils.isEmpty(gwId)){
                        log.info("网关公钥或网关ID为空，拒绝处理");
                        closeChannel(ctx);
                        return;
                    }
                    log.info("2.服务端接收到秘钥协商请求,客户端公钥为：{}",clientPubKey);
                    EcKeys ecKeys = SpringBeanFactory.getBean("EcKeys",EcKeys.class);
                    SocketMsg<String> publicKeyStr = new SocketMsg<String>()
                            .setId(1).setType(MsgType.AUTH_BACK_VALUE).setContent(ecKeys.getPubKey());

                    String key = EncryptOrDecryptUtil.ecdhKey(ecKeys.getPriKey(),clientPubKey);
                    log.info("3.服务端协商出加密秘钥：{}",key);
                    //将改秘钥加入秘钥库
                    Gateway gw = new Gateway()
                            .setGwId(msg.getGatewayId())
                            .setGwName("name")
                            .setChannel(ctx.channel())
                            .setKey(key)
                            .setPubKey(clientPubKey);
                    NettySocketHolder.put(msg.getGatewayId(), gw);
                    ctx.writeAndFlush(publicKeyStr);
                    break;
                case MsgType.AUTH_CHECK_VALUE:
                    log.info("6.服务端验证秘钥正确性");
                    Gateway gw1 = NettySocketHolder.get(msg.getGatewayId());
                    //确认秘钥的正确性
                    String login = EncryptOrDecryptUtil.doAES((String) msg.getContent(),gw1.getKey(), Cipher.DECRYPT_MODE);
                    log.info("7. 服务端解密出登录口令:{}",login);
                    if (("login").equals(login)){
                        log.info("网关【{}】验证通过，加入连接列表",msg.getGatewayId());

                        gw1.setStatus("在线");
                        NettySocketHolder.put(msg.getGatewayId(),gw1);
                        //向客户端发送认证成功的消息，这里一定要先发消息再替换handler
                        ctx.writeAndFlush(new SocketMsg<String>().setType(MsgType.LOGIN_SUCCESS_VALUE));
                        //用AES加解密替换掉默认的编解码器
                        ctx.pipeline().replace(ctx.pipeline().get("decoder"),"decoder",new JsonDecoderAES());
                        ctx.pipeline().replace(ctx.pipeline().get("encoder"),"encoder",new JsonEncoderAES());
                        ctx.pipeline().addFirst("idle",new IdleStateHandler(5,0, 0, TimeUnit.SECONDS));
                    }else {
                        log.info("非法客户端，关闭连接");
                        closeChannel(ctx);
                    }
                    break;
                default:break;
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("客户端认证出错{}",e.getMessage());
            closeChannel(ctx);
        }
        ctx.fireChannelRead(message);
        ReferenceCountUtil.release(msg);
    }

    private void closeChannel(ChannelHandlerContext ctx){
        NettySocketHolder.remove(ctx.channel());
        ctx.channel().close();
    }
}
