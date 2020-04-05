package com.shannon.server.handler;

import com.shannon.common.enums.MsgType;
import com.shannon.common.model.EcKeys;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.EncryptOrDecryptUtil;
import com.shannon.server.util.AesKeyMap;
import com.shannon.server.util.NettySocketHolder;
import com.shannon.server.util.SpringBeanFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.tiles3.SpringBeanPreparerFactory;

import javax.crypto.Cipher;
import javax.el.BeanNameResolver;

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
                    log.info("收到客户端秘钥协商消息,{}",msg.getContent());
                    EcKeys ecKeys = SpringBeanFactory.getBean("EcKeys",EcKeys.class);
                    SocketMsg publicKeyStr = new SocketMsg().setId(1).setType(MsgType.AUTH_BACK_VALUE)
                            .setContent(ecKeys.getPubKey());

                    String key = EncryptOrDecryptUtil.ecdhKey(ecKeys.getPriKey(),msg.getContent());
                    log.info("服务端协商出秘钥：{}",key);
                    //将改秘钥加入秘钥库
                    AesKeyMap.put(ctx.channel(),key);

                    ctx.writeAndFlush(publicKeyStr);
                    break;
                case MsgType.AUTH_CHECK_VALUE:
                    log.info("服务端验证秘钥");
                    //获取此通道对应的秘钥
                    String k = AesKeyMap.get(ctx.channel());
                    //确认秘钥的正确性
                    String login = EncryptOrDecryptUtil.doAES(msg.getContent(),k, Cipher.DECRYPT_MODE);
                    log.info("解密出客户端登录数据:{}",login);
                    if (!("login").equals(login)){
                        log.info("非法客户端，关闭连接");
                        ctx.channel().close();
                        AesKeyMap.remove(ctx.channel());
                    }
                    log.info("客户端{}验证通过，加入连接列表",ctx.name());
                    NettySocketHolder.put(msg.getGatewayId(), ctx.channel());
                    break;
                default:break;
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("客户端认证出错{}",e.getMessage());
            ctx.channel().close();
        }
        ctx.fireChannelRead(message);
        ReferenceCountUtil.release(msg);
    }
}
