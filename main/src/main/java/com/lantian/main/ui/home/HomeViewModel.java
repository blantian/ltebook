package com.lantian.main.ui.home;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import com.lantian.base.common.BaseResponse;
import com.lantian.base.common.bean.GetBook;
import com.lantian.base.common.bean.LeftMenuBean;
import com.lantian.base.dialog.RxUtil;
import com.lantian.base.net.retrofit.ResponseObserver;
import com.lantian.base.net.retrofit.RetrofitHelper;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.LogRecord;

public class HomeViewModel extends ViewModel {

    private Handler handler;
    private HomeFragment homeFragment;
    private MutableLiveData<BaseResponse> getBookMutableLiveData;
    private MutableLiveData<List<LeftMenuBean>> mutableLiveData;
    public HomeViewModel() {
        mutableLiveData = new MutableLiveData<>();
        getBookMutableLiveData = new MutableLiveData<>();
        homeFragment = new HomeFragment();
        getApiData();
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what ==1){
                    getBookMutableLiveData.postValue((BaseResponse) msg.obj);
                }
            }
        };
        List<LeftMenuBean> leftMenuBeans = new ArrayList<>();
        LeftMenuBean leftMenuBean = new LeftMenuBean();
        for (int i=0;i<20;i++){
            leftMenuBean.setName("ᠨᠢᠭᠡ");
            leftMenuBeans.add(leftMenuBean);
        }
        mutableLiveData.setValue(leftMenuBeans);

    }

    private void getApiData() {
        RetrofitHelper.getbook()
                .getBook("20200531")
                .compose(RxUtil.rxSchedulerHelper(homeFragment,false))
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResponseObserver<BaseResponse<GetBook>>() {
                    @Override
                    public void onSuccess(BaseResponse<GetBook> response) {
                        Message message = new Message();
                        message.what = 1;
                        message.obj = response;
                        handler.sendMessage(message);
                    }
                });

    }

    public LiveData<BaseResponse> getBookData() {
        return getBookMutableLiveData;
    }

    public LiveData<List<LeftMenuBean>> getData(){
        return  mutableLiveData;
    }
}