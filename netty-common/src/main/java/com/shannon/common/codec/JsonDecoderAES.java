package com.shannon.common.codec;

import com.alibaba.fastjson.JSON;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.util.EncryptOrDecryptUtil;
import com.shannon.common.util.NettySocketHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 使用AES解密的json解码器
 * @author zzc
 */
@Slf4j
public class JsonDecoderAES extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        SocketMsg msg;
        try {
            int len = in.readableBytes();
            byte[] bytes = new byte[len];
            in.readBytes(bytes);
            String key = NettySocketHolder.get(ctx.channel()).getKey();
            String cipherText = new String(bytes,StandardCharsets.UTF_8);
            log.info("接收到密文为:{}",cipherText);
            long start = System.currentTimeMillis();
            String json = EncryptOrDecryptUtil.doAES(cipherText, key, Cipher.DECRYPT_MODE);
            if (!StringUtils.isEmpty(json)){
                log.info("解密耗时：{}，解密出的明文为:{}",System.currentTimeMillis()-start,json);
                msg = JSON.parseObject(json).toJavaObject(SocketMsg.class);
                out.add(msg);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("解码错误"+e);
        }


    }
}
