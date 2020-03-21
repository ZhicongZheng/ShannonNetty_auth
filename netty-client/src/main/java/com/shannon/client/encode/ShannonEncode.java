package com.shannon.client.encode;

import com.shannon.common.model.SocketMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 客户端编码器
 */
public class ShannonEncode extends MessageToByteEncoder<SocketMsg> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, SocketMsg socketMsg, ByteBuf byteBuf) throws Exception {
        byteBuf.writeLong(socketMsg.getId()) ;
        byteBuf.writeBytes(socketMsg.getContent().getBytes()) ;
    }
}
