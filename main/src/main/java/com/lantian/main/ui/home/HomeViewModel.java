package com.lantian.main.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lantian.base.common.bean.LeftMenuBean;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<List<LeftMenuBean>> mutableLiveData;
    public HomeViewModel() {
        mutableLiveData = new MutableLiveData<>();
        List<LeftMenuBean> leftMenuBeans = new ArrayList<>();
        LeftMenuBean leftMenuBean = new LeftMenuBean();
        for (int i=0;i<20;i++){
            leftMenuBean.setName("ᠨᠢᠭᠡ");
            leftMenuBeans.add(leftMenuBean);
        }
        mutableLiveData.setValue(leftMenuBeans);

    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<List<LeftMenuBean>> getData(){
        return  mutableLiveData;
    }
}