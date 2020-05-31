package com.lantian.main.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.lantian.base.activity.BaseFragment;
import com.lantian.base.common.BaseResponse;
import com.lantian.base.common.bean.GetBook;
import com.lantian.base.common.bean.LeftMenuBean;
import com.lantian.base.mongl.MongolTextView;
import com.lantian.base.utils.GetApplicationContext;
import com.lantian.main.R;
import com.lantian.main.adapter.home.LeftmenuAdapter;
import com.lantian.main.adapter.home.MonglAdapter;
import com.lantian.main.ui.home.classify.IndicatorView;
import com.lantian.main.ui.home.classify.SubjectFragment;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends BaseFragment {

    private static final String TAG =BaseFragment.class.getName();
    private HomeViewModel homeViewModel;
    private LeftmenuAdapter leftmenuAdapter;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView mTitel;
    private MonglAdapter monglAdapter;
    private MongolTextView book;

    private int currPosition = 0;
    private SubjectFragment fragment;
    private SubjectFragment[] fragments;

    private IndicatorView mIndicatorview;
    private FrameLayout mFragmentContainer;

    private List<LeftMenuBean> menuBeanList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initView(root);
        homeViewModel.getData().observe(getViewLifecycleOwner(), new Observer<List<LeftMenuBean>>() {
            @Override
            public void onChanged(List<LeftMenuBean> leftMenuBeans) {
                menuBeanList = new ArrayList<>();
                menuBeanList = leftMenuBeans;
                initAdapter(leftMenuBeans);

            }
        });
        homeViewModel.getBookData().observe(getViewLifecycleOwner(), new Observer<BaseResponse>() {
            @Override
            public void onChanged(BaseResponse baseResponse) {
                Gson gson = new GsonBuilder().create();
                LinkedTreeMap<String,LinkedTreeMap> map = (LinkedTreeMap<String, LinkedTreeMap>) baseResponse.getData();
                for (Object o:map.keySet()){
                    String json =gson.toJson(map.get(o));
                    GetBook._$5Bean getBooks = gson.fromJson(json, GetBook._$5Bean.class);
                    book.setText(getBooks.getContent());
                }
            }
        });
        return root;
    }

    /**初始化视图**/
    private void initView(View root) {
        mTitel = root.findViewById(R.id.titel);
        book = root.findViewById(R.id.book);
        mIndicatorview = root.findViewById(R.id.indicatorview);
    }


    private void initAdapter(List<LeftMenuBean> leftMenuBeans) {
        linearLayoutManager = new LinearLayoutManager(getContext());
        monglAdapter = new MonglAdapter(leftMenuBeans, getContext());
        leftmenuAdapter = new LeftmenuAdapter(R.layout.left_side_navigation_item, leftMenuBeans);
        mTitel.setLayoutManager(linearLayoutManager);
        mTitel.setAdapter(monglAdapter);
        /**item 点击事件**/
        monglAdapter.setOnItemClickListener(new MonglAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, MonglAdapter.ViewName viewName, int position) {
                if (currPosition != position) {
                    //切换fragment
                    changeFragment(position);
                    currPosition = position;
                }
                mIndicatorview.openAnimator(view);
            }
        });
        fragments = new SubjectFragment[leftMenuBeans.size()];
        //默认加载第一个fragment
        fragment = SubjectFragment.newInstance(leftMenuBeans.get(0));
        changeFragment(currPosition);

        //设置indicatorView初始位置
        monglAdapter.setOnBinding(new MonglAdapter.OnBinding() {
            @Override
            public void onBinding() {
                View child = linearLayoutManager.findViewByPosition(0);
                if (child !=null){
                    mIndicatorview.openAnimator(child);
                }
            }
        });

    }

    /**
     * 加载fragment
     *
     * @param position
     */
    private void changeFragment(int position) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (fragments[currPosition] != null) {
            transaction.hide(fragments[currPosition]);
        }
        fragment = fragments[position];
        if (fragment == null) {
            fragment = SubjectFragment.newInstance(menuBeanList.get(position));
            fragments[position] = fragment;
            transaction.add(R.id.fragmentContainer, fragment);
        } else {
            transaction.show(fragment);
        }
        transaction.commitAllowingStateLoss();
    }
}
