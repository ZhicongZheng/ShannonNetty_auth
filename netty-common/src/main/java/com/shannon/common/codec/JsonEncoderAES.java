package com.shannon.common.codec;

import com.alibaba.fastjson.JSONObject;
import com.shannon.common.util.EncryptOrDecryptUtil;
import com.shannon.common.util.NettySocketHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;

/**
 * 使用AES加密的json编码器
 * @author zzc
 */
public class JsonEncoderAES extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        String json = "";
        try {
            json  = JSONObject.toJSONString(msg);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("编码错误"+e);
        }
        String key = NettySocketHolder.get(ctx.channel()).getKey();
        out.writeBytes(Unpooled.wrappedBuffer(EncryptOrDecryptUtil
                .doAES(json, key, Cipher.ENCRYPT_MODE).getBytes(StandardCharsets.UTF_8)));
    }
}
