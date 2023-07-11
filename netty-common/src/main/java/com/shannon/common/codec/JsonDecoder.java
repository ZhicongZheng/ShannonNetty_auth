package com.shannon.common.codec;

import com.alibaba.fastjson.JSON;
import com.shannon.common.model.SocketMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 默认json解码器
 * @author zzc
 */
public class JsonDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
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
