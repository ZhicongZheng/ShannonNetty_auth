package com.shannon.server.handler;

import com.shannon.common.enums.MsgType;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.EncryptOrDecryptUtil;
import com.shannon.common.util.NettySocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端处理网关上报消息handler
 * @author zzc
 */
@Slf4j
public class ServerReceiveHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        SocketMsg msg = (SocketMsg) message;
        if (msg.getType()== MsgType.UPLOAD_DATA_VALUE){
            log.info("服务端接收到网关上报数据");
            String pubKey = NettySocketHolder.get(msg.getGatewayId()).getPubKey();
            //验签
            if (!EncryptOrDecryptUtil.verify((String) msg.getContent(),pubKey,msg.getSign())){
                log.info("服务端验签未通过，消息疑似被篡改，丢弃消息");
                return;
            }
            log.info("服务端验签通过，消息为：{}", msg.getContent());
        }else {
            ctx.fireChannelRead(message);
        }
        ReferenceCountUtil.release(msg);
    }
}
