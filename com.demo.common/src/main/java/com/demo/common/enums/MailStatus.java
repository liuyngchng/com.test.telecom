package com.demo.common.enums;

/**
 * Mail state
 * @author Richard Liu
 * @since 2019.07.18
 */
public enum MailStatus {
    NOT_DELIVERED(0, "未投递"),
    DELIVERED(1, "已投递"),
    READ(2, "已读"),

    ;

    private int value;

    private String desc;
    MailStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static MailStatus getByName(int value) {
        for (MailStatus sig : MailStatus.values()) {
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
