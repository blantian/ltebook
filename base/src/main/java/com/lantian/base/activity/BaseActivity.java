package com.lantian.base.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import com.lantian.base.utils.ActivityManagerUtil;
import com.lantian.base.utils.StatusBarUtil;
import com.lantian.base.utils.ToastUtils;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

/**
 * Created by Sherlock·Holmes on 2020/5/23
 */
public abstract class BaseActivity extends RxAppCompatActivity {
    protected ActivityManagerUtil appManager = ActivityManagerUtil.getAppManager();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appManager.addActivity(this);
        StatusBarUtil.statusBarLightMode(this);
        setContentView(getLayoutId());
        init(savedInstanceState);
    }


    protected void showToast(String msg) {
        ToastUtils.show(msg);
    }

    /**
     * 绑定布局文件
     * @return
     */
    protected abstract @LayoutRes
    int getLayoutId();

    /**
     * 初始化
     * @param savedInstanceState
     */
    protected abstract void init(Bundle savedInstanceState);

    /**
     * 页面跳转
     * @param context
     * @param cType
     * @param args
     */
    public static void instance(Context context, Class<?> cType, Bundle args){
        Intent intent = new Intent(context, cType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        if(args !=null){
            intent.putExtras(args);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appManager.finishActivity(this);
    }
}
