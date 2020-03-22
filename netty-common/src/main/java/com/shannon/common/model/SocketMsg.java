package com.shannon.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 序列化的消息
 */
@Data
@Accessors(chain = true)
public class SocketMsg implements Serializable {


    private static final long serialVersionUID = 290429819350651974L;
    private long id;
    private int type;
    private String content;


}
