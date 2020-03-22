package com.shannon.common.json;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class JsonEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        String json = "";
        try {
           json  = JSONObject.toJSONString(msg);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("编码错误"+e);
        }

        out.writeBytes(Unpooled.wrappedBuffer(json.getBytes()));
    }

}
