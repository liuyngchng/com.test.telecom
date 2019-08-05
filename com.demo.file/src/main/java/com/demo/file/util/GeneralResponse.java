package com.demo.file.util;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Created by richard on 05/08/2019.
 */
public class GeneralResponse {
    private transient HttpResponseStatus status = HttpResponseStatus.OK;

    private String message = "SUCCESS";
    private Object data;

    public GeneralResponse(Object data) {
        this.data = data;
    }

    public GeneralResponse(HttpResponseStatus status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public void setStatus(HttpResponseStatus status) {
        this.status = status;
    }
}