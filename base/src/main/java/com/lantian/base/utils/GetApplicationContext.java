package com.lantian.base.utils;

import android.content.Context;

/**
 * Created by Sherlock·Holmes on 2020/5/23
 */
public class GetApplicationContext {

    public static Context context;
    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        GetApplicationContext.context = context.getApplicationContext();
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (context != null){
            return context;
        }
        throw new NullPointerException("u should init first");
    }
}
