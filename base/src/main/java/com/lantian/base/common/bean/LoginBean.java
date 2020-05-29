package com.lantian.base.common.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by Sherlock·Holmes on 2020/5/27
 */
public class LoginBean {

    @JSONField(name = "userPhoneNum")
    private String userPhoneNum;
    @JSONField(name = "userPassWord")
    private String userPassWord;

    public LoginBean(String userPhoneNum,String userPassWord){
        super();
        this.userPhoneNum = userPhoneNum;
        this.userPassWord = userPassWord;
    }

    public String getUserPhoneNum() {
        return userPhoneNum;
    }

    public void setUserPhoneNum(String userPhoneNum) {
        this.userPhoneNum = userPhoneNum;
    }

    public String getUserPassWord() {
        return userPassWord;
    }

    public void setUserPassWord(String userPassWord) {
        this.userPassWord = userPassWord;
    }
}
