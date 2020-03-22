package com.shannon.common.enums;

public enum MsgType {
    //验证
    AUTH(1),
    //ping
    PING(2),
    //pong
    PONG(3),
    //上传数据
    UPLOAD_DATA(4),
    //推送数据
    PUSH_DATA(5),
    //验证返回
    AUTH_BACK(6);

    private final int value;

    public static final int AUTH_VALUE = 1;

    public static final int PING_VALUE = 2;

    public static final int PONG_VALUE = 3;

    public static final int UPLOAD_DATA_VALUE = 4;

    public static final int PUSH_DATA_VALUE = 5;

    public static final int AUTH_BACK_VALUE = 6;

    @Deprecated
    public static MsgType valueOf(int value) {
        return forNumber(value);
    }

    public static MsgType forNumber(int value) {
        switch (value) {
            case 1: return AUTH;
            case 2: return PING;
            case 3: return PONG;
            case 4: return UPLOAD_DATA;
            case 5: return PUSH_DATA;
            case 11: return AUTH_BACK;
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
