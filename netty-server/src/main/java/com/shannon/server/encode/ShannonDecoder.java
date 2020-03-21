package com.shannon.server.encode;

import com.shannon.common.model.SocketMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 服务端解码器
 */
public class ShannonDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        long id = byteBuf.readLong();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String content = new String(bytes);
        SocketMsg socketMsg = new SocketMsg();
        socketMsg.setId(id);
        socketMsg.setContent(content);
        list.add(socketMsg);
    }
}
