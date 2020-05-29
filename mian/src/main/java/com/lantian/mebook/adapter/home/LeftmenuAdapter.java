package com.lantian.mebook.adapter.home;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lantian.base.common.bean.LeftMenuBean;
import com.lantian.mian.R;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by Sherlock·Holmes on 2020/5/26
 */
public class LeftmenuAdapter extends BaseQuickAdapter<LeftMenuBean, BaseViewHolder> {

    public LeftmenuAdapter(int layoutResId, @Nullable List<LeftMenuBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, LeftMenuBean leftMenuBean) {
        baseViewHolder.setText(R.id.mongol_text,leftMenuBean.getName());
    }


    @Override
    protected void convert(@NotNull BaseViewHolder holder, LeftMenuBean item, @NotNull List<?> payloads) {
        super.convert(holder, item, payloads);
    }
}
