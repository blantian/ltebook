package com.lantian.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.google.gson.internal.LinkedTreeMap;
import com.lantian.base.activity.BaseActivity;
import com.lantian.base.common.BaseResponse;
import com.lantian.base.common.bean.LoginBean;
import com.lantian.base.dialog.RxUtil;
import com.lantian.base.net.retrofit.ResponseObserver;
import com.lantian.base.net.retrofit.RetrofitHelper;
import com.lantian.base.net.retrofit.api.Loginapi;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class ActivityLogin extends BaseActivity implements View.OnClickListener {

    private EditText mPhone;
    private EditText mPassword;
    private Button mLogin;
    private Button mRegistered;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;

    }


    @Override
    protected void init(Bundle savedInstanceState) {
        initView();
    }


    private void initView() {
        EventBus.getDefault().register(this);
        mPhone = (EditText) findViewById(R.id.phone);
        mPassword = (EditText) findViewById(R.id.password);
        mLogin = (Button) findViewById(R.id.login);
        mRegistered = (Button) findViewById(R.id.registered);
        mLogin.setOnClickListener(this);
        mRegistered.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                login();
                break;
            case R.id.registered:

                break;
        }
    }


    private void login() {
        LoginBean loginBean = new LoginBean(mPhone.getText().toString(),mPassword.getText().toString());
        String json = JSON.toJSONString(loginBean);
        RetrofitHelper.getLoginapi()
                .loginSystem(mPhone.getText().toString(),mPassword.getText().toString())
                .compose(RxUtil.rxSchedulerHelper(this,true))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResponseObserver<BaseResponse<LinkedTreeMap>>() {
                    @Override
                    public void onSuccess(BaseResponse<LinkedTreeMap> response) {

                    }
                });
    }

    private void submit() {
        // validate
        String phoneString = mPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phoneString)) {
            showToast("phoneString不能为空");
            return;
        }

        String passwordString = mPassword.getText().toString().trim();
        if (TextUtils.isEmpty(passwordString)) {
            showToast("passwordString不能为空");
            return;
        }

        // TODO validate success, do something
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
