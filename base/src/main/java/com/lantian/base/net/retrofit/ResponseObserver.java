package com.lantian.base.net.retrofit;


import com.google.gson.JsonParseException;
import com.lantian.base.net.retrofit.exception.NoDataExceptionException;
import com.lantian.base.net.retrofit.exception.ServerResponseException;
import com.lantian.base.utils.LogUtils;
import com.lantian.base.utils.ToastUtils;

import org.json.JSONException;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.ParseException;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import retrofit2.adapter.rxjava2.HttpException;


/**
 * Date:2020/5/24
 * Time:23:38
 * author:lantian
 *
 * 服务器响应异常处理
 */
public abstract class ResponseObserver<T> implements Observer<T> {

    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onNext(T response) {
        onSuccess(response);
    }

    /**
     * 处理请求异常
     * @param e
     */
    @Override
    public void onError(@NonNull Throwable e) {
        LogUtils.e("Retrofit", e.getMessage());
        //   HTTP错误
        if (e instanceof HttpException) {
            onException(ExceptionReason.BAD_NETWORK);
            //   连接错误
        } else if (e instanceof ConnectException
                || e instanceof UnknownHostException) {
            onException(ExceptionReason.CONNECT_ERROR);
            //  连接超时
        } else if (e instanceof InterruptedIOException) {
            onException(ExceptionReason.CONNECT_TIMEOUT);
            //  解析错误
        } else if (e instanceof JsonParseException
                || e instanceof JSONException
                || e instanceof ParseException) {
            onException(ExceptionReason.PARSE_ERROR);
        } else if (e instanceof ServerResponseException) {
            onFail(e.getMessage());
        } else if (e instanceof NoDataExceptionException) {
            onSuccess(null);
        } else {
            onException(ExceptionReason.UNKNOWN_ERROR);
        }
        onFinish();
    }

    @Override
    public void onComplete() {

    }

    abstract public void onSuccess(T response);

    /**
     * 服务器返回数据，但响应码不为1000
     */
    public void onFail(String message) {
        ToastUtils.show(message);
    }

    public void onFinish() {
    }

    /**
     * 请求异常
     *
     * @param reason
     */
    public void onException(ExceptionReason reason) {
        switch (reason) {
            case CONNECT_ERROR:
                ToastUtils.show("连接错误");
                break;
            case CONNECT_TIMEOUT:
                ToastUtils.show("连接超时");
                break;
            case BAD_NETWORK:
                ToastUtils.show("网络问题");
                break;
            case PARSE_ERROR:
                ToastUtils.show("解析数据失败");
                break;
            case UNKNOWN_ERROR:
                ToastUtils.show("未知错误");
            default:
                break;
        }
    }


    /**
     * 请求网络失败原因
     */
    public enum ExceptionReason {
        /**
         * 解析数据失败
         */
        PARSE_ERROR,
        /**
         * 网络问题
         */
        BAD_NETWORK,
        /**
         * 连接错误
         */
        CONNECT_ERROR,
        /**
         * 连接超时
         */
        CONNECT_TIMEOUT,
        /**
         * 未知错误
         */
        UNKNOWN_ERROR,
    }
}
