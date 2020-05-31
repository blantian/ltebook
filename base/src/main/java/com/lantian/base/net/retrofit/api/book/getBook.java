package com.lantian.base.net.retrofit.api.book;

import com.google.gson.internal.LinkedTreeMap;
import com.lantian.base.common.BaseResponse;
import com.lantian.base.common.bean.GetBook;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Date:2020/5/31
 * Time:11:59
 * author:lantian
 */
public interface getBook {
    @FormUrlEncoded
    @POST("MGetBook/")
    Observable<BaseResponse<GetBook>> getBook(@Field("ebookName") String userPhoneNum);
}
