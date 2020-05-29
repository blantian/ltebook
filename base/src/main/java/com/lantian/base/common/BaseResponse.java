package com.lantian.base.common;

/**
 * Date:2020/5/24
 * Time:23:29
 * author:lantian
 */
public class BaseResponse<T> {

    private int status;
    private String message;
    private T data;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
