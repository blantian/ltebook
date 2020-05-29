package com.lantian.base.net.retrofit.interceptor;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 头部拦截器
 */
public class HttpHeaderInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .header("Content-Type","text/html; charset=utf-8")
                .header("Connection","keep-alive")
                .header("Proxy-Connection","keep-alive").build();
        return chain.proceed(request);
    }
}
