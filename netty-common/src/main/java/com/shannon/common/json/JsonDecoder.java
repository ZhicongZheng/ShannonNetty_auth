package com.shannon.common.json;

import com.alibaba.fastjson.JSON;
import com.shannon.common.model.SocketMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

public class JsonDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        SocketMsg msg = null;
        try {
            int len = in.readableBytes();
            byte[] bytes = new byte[len];
            in.readBytes(bytes);
            msg = JSON.parseObject(new String(bytes)).toJavaObject(SocketMsg.class);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("解码错误"+e);
        }

        out.add(msg) ;
    }

}
