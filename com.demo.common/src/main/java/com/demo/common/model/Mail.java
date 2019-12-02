package com.demo.common.model;


import com.demo.common.enums.MailStatus;

import java.io.Serializable;
import java.util.Date;

/**
 * Mail model
 * @author Richard Liu
 * @since 2019.07.18
 */
public class Mail implements Serializable {

    public Mail() {
        this.id = new Date().getTime();
    }

    private long id;

    private String from;

    private String to;

    private String title;

    private String cc;

    private int status = MailStatus.NOT_DELIVERED.getValue();

    private String text;

    private String attachment;

    private Date createTime;

    private Date updateTime;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public void setDelivered() {
        this.status = MailStatus.DELIVERED.getValue();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
