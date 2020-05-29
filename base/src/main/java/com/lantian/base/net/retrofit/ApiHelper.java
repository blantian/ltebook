package com.lantian.base.net.retrofit;

import retrofit2.Retrofit;

/**
 * Created by Sherlock·Holmes on 2020/5/27
 */
public class ApiHelper {
    public static <T> T getApi(Class<T> cls,String baseUrl){
        Retrofit retrofit = RetrofitUtils.getRetrofitBuilder(baseUrl).build();
        return retrofit.create(cls);
    }
}
