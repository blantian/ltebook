package com.lantian.mebook;

import com.lantian.base.BaseApplication;

/**
 * Created by Sherlock·Holmes on 2020/5/28
 */
 public class MainApplication extends BaseApplication {

     private static MainApplication mainApplication;

     public static MainApplication getInstance(){
         return mainApplication;
     }

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;

    }
}
