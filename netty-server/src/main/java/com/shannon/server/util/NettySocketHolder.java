package com.shannon.server.util;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用ConcurrentHashMap实现的并发客户端管理器
 * @author zzc
 */
public class NettySocketHolder {

    private static Map<String, Channel> MAP = new ConcurrentHashMap<>(16);

    public static void put(String id, Channel channel) {
        MAP.put(id, channel);
    }

    public static Channel get(String id) {
        return MAP.get(id);
    }

    public static Map<String, Channel> getMap() {
        return MAP;
    }

    public static void remove(Channel nioSocketChannel) {
        MAP.entrySet().stream().filter(entry -> entry.getValue() == nioSocketChannel).forEach(entry -> MAP.remove(entry.getKey()));
    }
}
