package com.demo.client.connect.enums;

/**
 * Signal used to difference msg function.
 * @author Richard Liu
 * @since 2019.07.18
 */
public enum Signal {
    LOGIN("login", "登录"),
    PULL_MSG("pull_msg", "拉取消息"),
    SEND_MSG("send_msg", "发送消息"),

    ;

    private String name;

    private String desc;
    Signal(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public static Signal getByName(String name) {
        for (Signal sig : Signal.values()) {
            if (sig.name.equals(name)) {
                return sig;
            }
        }
        return null;
    }

}
