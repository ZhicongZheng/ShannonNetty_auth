package com.shannon.client.initializer;

import com.shannon.client.handler.ShannonHeartClientHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 */
public class ClientChannelInit extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline p = channel.pipeline();
        //检测空闲必须放在这里 因为pipeline是分顺序加载的
        p.addLast(new IdleStateHandler(0,10, 0, TimeUnit.SECONDS));
        //解码器必须放在前面，否则发数据收不到
        p.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        p.addLast(new StringDecoder());
        p.addLast(new StringEncoder());

        //客户端自定义的hanlder
        p.addLast(new ShannonHeartClientHandler());
    }
}