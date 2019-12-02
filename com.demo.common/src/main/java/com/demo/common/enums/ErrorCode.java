package com.demo.common.enums;

/**
 * Signal used to difference msg function.
 * @author Richard Liu
 * @since 2019.07.18
 */
public enum ErrorCode {
    OK(0, "OK"),
    USER_NOT_EXIST(1, "用户不存在"),
    PS_ERROR(2, "密码错误"),
    USER_PS_NULL(3, "用户名或密码为空"),
    ;

    private int code;
    private String desc;
    ErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ErrorCode getByName(int code) {
        for (ErrorCode error : ErrorCode.values()) {
            if (error.code == code) {
                return error;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
