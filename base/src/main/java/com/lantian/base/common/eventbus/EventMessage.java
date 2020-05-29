package com.lantian.base.common.eventbus;

/**
 * Date:2020/5/28
 * Time:0:14
 * author:lantian
 */
public class EventMessage {

    private String message;
    private int code;

    public EventMessage(int code,String message){
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
