package com.lantian.base.net.retrofit;

import com.lantian.base.common.Constants;
import com.lantian.base.net.retrofit.converter.MyGsonConverterFactory;
import com.lantian.base.net.retrofit.interceptor.HttpCacheInterceptor;
import com.lantian.base.net.retrofit.interceptor.HttpHeaderInterceptor;
import com.lantian.base.net.retrofit.interceptor.LoggingInterceptor;
import com.lantian.base.utils.GetApplicationContext;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RetrofitUtils {

    public static OkHttpClient.Builder getOkHttpClientBuilder(){
        File cacheFile = new File(GetApplicationContext.getContext().getExternalCacheDir(), "net_cache");
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 100);
        return new OkHttpClient.Builder()
                .readTimeout(Constants.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(Constants.DEFAULT_TIMEOUT,TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(new LoggingInterceptor())
                .addInterceptor(new HttpHeaderInterceptor())
                .addNetworkInterceptor(new HttpCacheInterceptor())
                .cache(cache);
    }

    public static Retrofit.Builder getRetrofitBuilder(String baseUrl){
        OkHttpClient okHttpClient = getOkHttpClientBuilder().build();
        return  new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(MyGsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl);
    }

}
