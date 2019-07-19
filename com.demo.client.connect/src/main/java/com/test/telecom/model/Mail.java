package com.demo.telecom.model;

import com.demo.telecom.enums.MailState;

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

    private String cc;

    private Date date;

    private int state = MailState.NOT_DELIVERED.getValue();

    private String text;

    private String attachment;



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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
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
        this.state = MailState.DELIVERED.getValue();
    }
}
