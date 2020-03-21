package com.shannon.client.tcpclient;

import com.shannon.client.init.ShannonChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.TimeUnit;

/**
 * Netty客户端
 */
@Slf4j
@Component
public class ShannonNettyClient {
    private EventLoopGroup group = new NioEventLoopGroup();
    @Value("${netty.server.port}")
    private int nettyPort;
    @Value("${netty.server.host}")
    private String host;

    private SocketChannel socketChannel;

    @PostConstruct
    public void start() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        //NioSocketChannel用于创建客户端通道，而不是NioServerSocketChannel。
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ShannonChannelInitializer())
                .remoteAddress(host,nettyPort)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true);

        ChannelFuture future = bootstrap.connect().sync();
        if (future.isSuccess()) {
            log.info("启动 Netty客户端 成功");
        }
        //客户端断线重连逻辑,20秒重连一次
        future.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()) {
                log.info("连接Netty服务端成功");
            } else {
                log.info("连接失败，进行断线重连");
                future1.channel().eventLoop().schedule(() -> {
                    try {
                        start();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        log.info("连接Netty服务端异常："+e.getMessage());
                    }
                }, 20, TimeUnit.SECONDS);
            }
        });

        socketChannel = (SocketChannel) future.channel();
    }

}
