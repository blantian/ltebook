package com.lantian.base.net.retrofit.converter;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.lantian.base.BuildConfig;
import com.lantian.base.common.BaseResponse;
import com.lantian.base.common.ResponseStatus;
import com.lantian.base.net.retrofit.exception.ApiException;
import com.lantian.base.net.retrofit.exception.NoDataExceptionException;
import com.lantian.base.net.retrofit.exception.ServerResponseException;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, Object> {
    private final Gson gson;
    private final TypeAdapter<T> adapter;

    GsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public Object convert(ResponseBody value) throws IOException {
        try {
            String response = value.string();
            if (BuildConfig.DEBUG){
                Log.e(BuildConfig.APPLICATION_ID, response);
            }
            BaseResponse basicResponse = gson.fromJson(response,BaseResponse.class);
            Log.e("basicResponse",basicResponse.getStatus()+basicResponse.getMessage());
            if (basicResponse.getStatus() == ResponseStatus.SUCCESS ){
                 return basicResponse;
            }else if (basicResponse.getData() ==null){
                throw new NoDataExceptionException();
            }else{
                throw new ServerResponseException(basicResponse.getStatus(),basicResponse.getMessage());
            }
        } finally {
            value.close();
        }
    }
}