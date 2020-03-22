package com.shannon.server.util;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientStatus {


    private static Map<Channel, Boolean> MAP = new ConcurrentHashMap<>(16);

    public static void put(Channel channel,Boolean checked) {
        MAP.put(channel,checked);
    }

    public static Boolean get(Channel channel) {
        return MAP.get(channel);
    }

    public static Map<Channel, Boolean> getMAP() {
        return MAP;
    }

    public static void remove(Channel channel) {
        MAP.entrySet().stream().filter(entry -> entry.getKey() == channel).forEach(entry -> MAP.remove(entry.getKey()));
    }
}
