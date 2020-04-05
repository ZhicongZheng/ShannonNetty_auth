package com.shannon.common.enums;

/**
 * 消息类型枚举类
 * @author zzc
 */

public enum MsgType {
    //第一次握手，客户端主动认证
    AUTH(1),
    //第二次握手，服务端认证返回
    AUTH_BACK(2),
    //第三次握手，客户端发送验证消息，验证消息准确性
    AUTH_CHECK(3),
    //ping
    PING(4),
    //pong
    PONG(5),
    //上传数据
    UPLOAD_DATA(6),
    //推送数据
    PUSH_DATA(7),
    //服务端主动刷新秘钥
    REFRESH_KEY(8);


    private final int value;

    public static final int AUTH_VALUE = 1;
    public static final int AUTH_BACK_VALUE = 2;
    public static final int AUTH_CHECK_VALUE = 3;
    public static final int PING_VALUE = 4;
    public static final int PONG_VALUE = 5;
    public static final int UPLOAD_DATA_VALUE = 6;
    public static final int PUSH_DATA_VALUE = 7;
    public static final int REFRESH_KEY_VALUE = 8;



    @Deprecated
    public static MsgType valueOf(int value) {
        return forNumber(value);
    }

    public static MsgType forNumber(int value) {
        switch (value) {
            case 1: return AUTH;
            case 2: return AUTH_BACK;
            case 3: return AUTH_CHECK;
            case 4: return PING;
            case 5: return PONG;
            case 6: return UPLOAD_DATA;
            case 7: return PUSH_DATA;
            case 8: return REFRESH_KEY;
            default: return null;
        }
    }

    MsgType(int value) {
        this.value = value;
    }

    public final int getNumber() {
        return value;
    }

}
