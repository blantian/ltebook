package com.lantian.base;

import android.app.Application;
import androidx.multidex.MultiDex;

import com.lantian.base.utils.GetApplicationContext;

/**
 * Created by Sherlock·Holmes on 2020/5/23
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GetApplicationContext.init(this);
        MultiDex.install(this);
    }
}
