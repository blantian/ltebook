package com.lantian.base.net.retrofit.exception;

/**
 * Created by Sherlock·Holmes on 2020/5/27
 */
public class ApiException extends RuntimeException {

    private String mErrorCode;

    public ApiException(String errorCode, String errorMessage) {
        super(errorMessage);
        mErrorCode = errorCode;
    }

    /**
     * 判断是否是token失效
     *
     * @return 失效返回true, 否则返回false;
     */
//    public boolean isTokenExpried() {
//        return mErrorCode == ErroCode.TOKEN_EXPRIED;
//    }
}
