package com.shannon.common.model;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 客户端网关实体
 * @author zzc
 */
@Data
@Accessors(chain = true)
public class Gateway {


    private String gwId;

    private String gwName;

    private String status;

    private Channel channel;

    private String key;

    private String pubKey;
}
