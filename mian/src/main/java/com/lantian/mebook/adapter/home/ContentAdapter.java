package com.lantian.mebook.adapter.home;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lantian.base.common.bean.ContentBean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by Sherlock·Holmes on 2020/5/26
 */
public class ContentAdapter extends BaseQuickAdapter<ContentBean, BaseViewHolder> {

    public ContentAdapter(int layoutResId, @Nullable List<ContentBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, ContentBean contentBean) {

    }
}
