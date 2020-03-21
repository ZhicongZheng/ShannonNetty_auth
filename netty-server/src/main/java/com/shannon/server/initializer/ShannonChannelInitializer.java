package com.shannon.server.initializer;

import com.shannon.server.encode.ShannonDecoder;
import com.shannon.server.handler.ShannonHeartServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;

/**
 * 配置新Channel的ChannelPipeline
 */
public class ShannonChannelInitializer extends ChannelInitializer<Channel> {

    @Value("${server.READER_IDLE_TIME_SECONDS}")
    private int READER_IDLE_TIME_SECONDS;

    @Value("${server.WRITER_IDLE_TIME_SECONDS}")
    private int WRITER_IDLE_TIME_SECONDS;

    @Value("${server.ALL_IDLE_TIME_SECONDS}")
    private int ALL_IDLE_TIME_SECONDS;
    @Override
    protected void initChannel(Channel channel) {

        ChannelPipeline p = channel.pipeline();
        //检测空闲必须放在这里 因为pipeline是分顺序加载的
        p.addLast(new IdleStateHandler(READER_IDLE_TIME_SECONDS,WRITER_IDLE_TIME_SECONDS, ALL_IDLE_TIME_SECONDS, TimeUnit.SECONDS));
        //解码器必须放在前面，否则发数据收不到
        p.addLast(new ShannonDecoder());
        //客户端自定义的hanlder
        p.addLast(new ShannonHeartServerHandler());
    }
}
