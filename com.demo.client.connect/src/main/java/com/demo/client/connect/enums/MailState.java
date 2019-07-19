package com.demo.client.connect.enums;

/**
 * Mail state
 * @author Richard Liu
 * @since 2019.07.18
 */
public enum MailState {
    NOT_DELIVERED(0, "未投递"),
    DELIVERED(1, "已投递"),

    ;

    private int value;

    private String desc;
    MailState(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static MailState getByName(int value) {
        for (MailState sig : MailState.values()) {
            if (sig.value == value) {
                return sig;
            }
        }
        return null;
    }

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }
}
