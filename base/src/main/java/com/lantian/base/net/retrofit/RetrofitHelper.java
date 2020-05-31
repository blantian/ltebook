package com.lantian.base.net.retrofit;

import com.lantian.base.common.Constants;
import com.lantian.base.net.retrofit.api.book.getBook;
import com.lantian.base.net.retrofit.api.user.Login;

/**
 * Created by Sherlock·Holmes on 2020/5/27
 */
public class RetrofitHelper {

    //登录
    private static Login loginapi;
    //获取图书
    private static getBook getbook;
    //登录
    public static Login getlogin(){
        if (loginapi ==null){
            loginapi = ApiHelper.getApi(Login.class, Constants.BASE_URL);
        }
        return loginapi;
    }
    //获取图书
    public static getBook getbook(){
        if (getbook == null) {
            getbook = ApiHelper.getApi(getBook.class,Constants.BASE_URL);
        }
        return getbook;
    }

}
