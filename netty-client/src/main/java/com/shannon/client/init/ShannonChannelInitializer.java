package com.shannon.client.init;

import com.shannon.client.encode.ShannonEncode;
import com.shannon.client.handler.ShannonHeartClientHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;

/**
 */
public class ShannonChannelInitializer extends ChannelInitializer<Channel> {

    @Value("${client.READER_IDLE_TIME_SECONDS}")
    private int READER_IDLE_TIME_SECONDS;

    @Value("${client.WRITER_IDLE_TIME_SECONDS}")
    private int WRITER_IDLE_TIME_SECONDS;

    @Value("${client.ALL_IDLE_TIME_SECONDS}")
    private int ALL_IDLE_TIME_SECONDS;

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline p = channel.pipeline();
        //检测空闲必须放在这里 因为pipeline是分顺序加载的
        p.addLast(new IdleStateHandler(READER_IDLE_TIME_SECONDS,WRITER_IDLE_TIME_SECONDS, ALL_IDLE_TIME_SECONDS, TimeUnit.SECONDS));
        //解码器必须放在前面，否则发数据收不到
        p.addLast(new ShannonEncode());

        //客户端自定义的hanlder
        p.addLast(new ShannonHeartClientHandler());
    }
}
