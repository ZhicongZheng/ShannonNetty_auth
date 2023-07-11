package com.shannon.common.util;

import com.shannon.common.model.Gateway;
import io.netty.channel.Channel;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用ConcurrentHashMap实现的并发客户端管理器
 * @author zzc
 */
public class NettySocketHolder {

    private static Map<String, Gateway> MAP = new ConcurrentHashMap<>(16);

    public static void put(String id, Gateway gw) {
        MAP.put(id, gw);
    }

    public static Gateway get(String id) {
        return MAP.get(id);
    }

    public static Gateway get(Channel channel){
       Optional<Map.Entry<String, Gateway>> optional = MAP.entrySet().stream().filter(entry -> entry.getValue().getChannel()==channel).findFirst();
        return optional.map(Map.Entry::getValue).orElse(null);
    }

    public static Map<String, Gateway> getMap() {
        return MAP;
    }

    public static void remove(Channel channel) {
        MAP.entrySet().stream().filter(entry -> entry.getValue().getChannel()==channel).forEach(entry -> MAP.remove(entry.getKey()));
    }
}
