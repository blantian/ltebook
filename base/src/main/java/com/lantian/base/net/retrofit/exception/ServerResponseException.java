package com.lantian.base.net.retrofit.exception;

import com.lantian.base.common.ResponseStatus;

/**
 * 服务器返回的异常
 */
public class ServerResponseException extends RuntimeException {

    private int errorCode;

    public ServerResponseException(int errorCode, String cause) {
        super(ResponseStatus.getErrorMessage(errorCode, cause), new Throwable(cause));
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
