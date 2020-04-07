package com.shannon.common.util;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持并发的客户端管理器
 * @author zzc
 */
public class AesKeyMap {

    private static Map<Channel, String> MAP = new ConcurrentHashMap<>(16);

    public static void put(Channel channel,String key) {
        MAP.put(channel,key);
    }

    public static String get(Channel channel) {
        return MAP.get(channel);
    }

    public static Map<Channel, String> getMap() {
        return MAP;
    }

    public static void remove(Channel channel) {
        MAP.entrySet().stream().filter(entry -> entry.getKey() == channel).forEach(entry -> MAP.remove(entry.getKey()));
    }
}
