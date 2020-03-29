package com.shannon.client.initializer;

import com.shannon.client.handler.ShannonHeartClientHandler;
import com.shannon.common.json.JsonDecoder;
import com.shannon.common.json.JsonEncoder;
import com.shannon.common.model.SocketMsg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author zzc
 */
public class ClientChannelInit extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        //检测空闲必须放在这里 因为pipeline是分顺序加载的
        p.addLast(new IdleStateHandler(0,10, 0, TimeUnit.SECONDS));
        //解码器必须放在前面，否则发数据收不到
        //进行长度字段解码，这里也会对数据进行粘包和拆包处理
        p.addLast(new LengthFieldBasedFrameDecoder(8192, 0, 4, 0, 4));
        //LengthFieldPrepender是一个编码器，主要是在响应字节数据前面添加字节长度字段
        p.addLast(new LengthFieldPrepender(4));
        p.addLast(new JsonDecoder()) ;
        p.addLast(new JsonEncoder());

        //客户端自定义的hanlder
        p.addLast(new ShannonHeartClientHandler());
    }
}
