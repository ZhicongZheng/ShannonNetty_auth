package com.shannon.common.codec;

import com.alibaba.fastjson.JSONObject;
import com.shannon.common.util.EncryptOrDecryptUtil;
import com.shannon.common.util.NettySocketHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;

/**
 * 使用AES加密的json编码器
 * @author zzc
 */
@Slf4j
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
        log.info("加密前的明文为:{}",json);
        long start = System.currentTimeMillis();
        String cipherText= EncryptOrDecryptUtil.doAES(json, key, Cipher.ENCRYPT_MODE);
        log.info("加密耗时：{}，加密后的明文为:{}",System.currentTimeMillis()-start,cipherText);
        out.writeBytes(Unpooled.wrappedBuffer(cipherText.getBytes(StandardCharsets.UTF_8)));
    }
}
