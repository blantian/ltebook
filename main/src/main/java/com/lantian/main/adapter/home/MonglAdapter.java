package com.lantian.main.adapter.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.lantian.base.common.bean.LeftMenuBean;
import com.lantian.base.mongl.MongolTextView;
import com.lantian.main.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Date:2020/5/29
 * Time:21:02
 * author:lantian
 */
public class MonglAdapter extends RecyclerView.Adapter<MonglAdapter.MonglHolder> {

    private final String TAG = this.getClass().getName();
    private List<LeftMenuBean> leftMenuBeans;
    private OnItemClickListener mOnItemClickListener;
    public enum ViewName {ITEM, ITEM_NAME}
    private Context context;

    public MonglAdapter(List<LeftMenuBean> leftMenuBeans, Context context){
        this.leftMenuBeans = leftMenuBeans;
        this.context =context;
        Log.e(TAG,leftMenuBeans.size() +"");
    }

    @NonNull
    @Override
    public MonglAdapter.MonglHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_side_navigation_item,parent,false);
        return new MonglHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonglAdapter.MonglHolder holder, int position) {
        holder.mongolTextView.setText(leftMenuBeans.get(position).getName());
        MonglHolder monglHolder = (MonglHolder) holder;
        monglHolder.mongolTextView.setTag(position);
        monglHolder.itemView.setTag(position);
        if (onBinding!=null){
            onBinding.onBinding();
        }
    }

    @Override
    public int getItemCount() {
        return leftMenuBeans.size();
    }

    public class MonglHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        MongolTextView mongolTextView;
        public MonglHolder(@NonNull View itemView) {
            super(itemView);
            mongolTextView = itemView.findViewById(R.id.mongl);
            mongolTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null){
                int position = (int) view.getTag();
                switch (view.getId()) {
                    case R.id.mongl:
                        mOnItemClickListener.onItemClick(view, ViewName.ITEM_NAME, position);
                        break;
                }
            }
        }

    }

    private OnBinding onBinding;
    public void setOnBinding(OnBinding onBinding) {
        this.onBinding = onBinding;
    }
    public interface OnBinding{
        void onBinding();
    }

    // 自定义点击事件
    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
    public interface OnItemClickListener {
        void onItemClick(View view, ViewName viewName, int position);
    }
}
