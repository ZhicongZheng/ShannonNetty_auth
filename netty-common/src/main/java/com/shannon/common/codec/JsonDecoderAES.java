package com.shannon.common.codec;

import com.alibaba.fastjson.JSON;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.EncryptOrDecryptUtil;
import com.shannon.common.util.NettySocketHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 使用AES解密的json解码器
 * @author zzc
 */
public class JsonDecoderAES extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        SocketMsg msg = null;
        try {
            int len = in.readableBytes();
            byte[] bytes = new byte[len];
            in.readBytes(bytes);
            String key = NettySocketHolder.get(ctx.channel()).getKey();
            String msgByAes = EncryptOrDecryptUtil
                    .doAES(new String(bytes,StandardCharsets.UTF_8), key, Cipher.DECRYPT_MODE);
            msg = JSON.parseObject(msgByAes).toJavaObject(SocketMsg.class);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("解码错误"+e);
        }

        out.add(msg) ;
    }
}
