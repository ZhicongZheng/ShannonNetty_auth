package com.shannon.server.handler;

import com.shannon.common.enums.MsgType;
import com.shannon.common.model.EcKeys;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.EncryptOrDecryptUtil;
import com.shannon.server.util.AESKeyMap;
import com.shannon.server.util.NettySocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;

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
        switch (msg.getType()){
            case MsgType.AUTH_VALUE:
                log.info("收到客户端秘钥协商消息,{}",msg.getContent());

                EcKeys ecKeys = EncryptOrDecryptUtil.getEcKeys();
                log.info("服务端初始化公钥serPubKey【{}】",ecKeys.getPubKey());
                log.info("服务端初始化私钥serPriKey【{}】",ecKeys.getPriKey());
                SocketMsg publicKeyStr = new SocketMsg().setId(1).setType(MsgType.AUTH_BACK_VALUE)
                        .setContent(ecKeys.getPubKey());

                String key = EncryptOrDecryptUtil.ecdhKey(ecKeys.getPriKey(),msg.getContent());
                log.info("服务端协商出秘钥：{}",key);
                //将改秘钥加入秘钥库
                AESKeyMap.put(ctx.channel(),key);

                ctx.writeAndFlush(publicKeyStr);
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
            default:ctx.fireChannelRead(message);
        }
        ReferenceCountUtil.release(msg);
    }
}
