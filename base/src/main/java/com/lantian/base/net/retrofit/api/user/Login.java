package com.lantian.base.net.retrofit.api.user;

import com.google.gson.internal.LinkedTreeMap;
import com.lantian.base.common.BaseResponse;


import org.junit.experimental.theories.FromDataPoints;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HTTP;
import retrofit2.http.POST;

/**
 * Created by Sherlock·Holmes on 2020/5/27
 */
public interface Login {

    @FormUrlEncoded
    @POST("loginPost/")
    Observable<BaseResponse<LinkedTreeMap>> loginSystem(@Field("userPhoneNum") String userPhoneNum
            , @Field("userPassWord") String userPassWord);
}
