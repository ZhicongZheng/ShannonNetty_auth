package com.shannon.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 序列化的消息
 * @author zzc
 */
@Data
@Accessors(chain = true)
public class SocketMsg {

    private long id;
    private int type;
    private String gatewayId;
    private String content;


}
