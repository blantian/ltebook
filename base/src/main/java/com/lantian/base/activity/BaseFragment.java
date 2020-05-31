package com.lantian.base.activity;

import com.lantian.base.utils.ToastUtils;
import com.trello.rxlifecycle2.components.support.RxFragment;

/**
 * Date:2020/5/31
 * Time:12:16
 * author:lantian
 */
public class BaseFragment  extends RxFragment {

    protected void showToast(String msg) {
        ToastUtils.show(msg);
    }
}
