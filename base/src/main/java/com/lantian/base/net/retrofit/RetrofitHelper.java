package com.lantian.base.net.retrofit;

import com.lantian.base.common.Constants;
import com.lantian.base.net.retrofit.api.Loginapi;

/**
 * Created by Sherlock·Holmes on 2020/5/27
 */
public class RetrofitHelper {

    private static Loginapi loginapi;
    public static Loginapi getLoginapi(){
        if (loginapi ==null){
            loginapi = ApiHelper.getApi(Loginapi.class, Constants.BASE_URL);
        }
        return loginapi;
    }
}
