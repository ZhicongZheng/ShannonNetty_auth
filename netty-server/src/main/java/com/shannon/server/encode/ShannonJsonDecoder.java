package com.shannon.server.encode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.json.JsonObjectDecoder;

import java.util.List;

public class ShannonJsonDecoder extends JsonObjectDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        super.decode(channelHandlerContext,byteBuf,list);
        list.forEach(o -> {

        });
    }

}
