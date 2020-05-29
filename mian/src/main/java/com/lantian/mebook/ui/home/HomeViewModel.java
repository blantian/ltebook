package com.lantian.mebook.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lantian.base.common.bean.LeftMenuBean;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<List<LeftMenuBean>> leftMenuBeanMutableLiveData;
    private List<LeftMenuBean> leftMenuBeans = new ArrayList<>();
    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
        leftMenuBeanMutableLiveData = new MutableLiveData<>();
        LeftMenuBean leftMenuBean = new LeftMenuBean();
        for (int i =0;i<20;i++){
            leftMenuBean.setName("ᠨᠢᠭᠡ");
            leftMenuBean.setId(i);
            leftMenuBeans.add(leftMenuBean);
        }
        Log.e("size",String.valueOf(leftMenuBeans.size()));
        leftMenuBeanMutableLiveData.setValue(leftMenuBeans);
    }

    public LiveData<String> getText() {
        return mText;
    }
    public LiveData<List<LeftMenuBean>> getData(){
        return leftMenuBeanMutableLiveData;
    }


}