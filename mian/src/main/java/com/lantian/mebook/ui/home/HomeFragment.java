package com.lantian.mebook.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lantian.base.common.bean.LeftMenuBean;
import com.lantian.mebook.adapter.home.ContentAdapter;
import com.lantian.mebook.adapter.home.LeftmenuAdapter;
import com.lantian.mian.R;

import java.util.List;
public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private RecyclerView mLeftSideNavigation;
    private RecyclerView mContent;
    private LeftmenuAdapter leftmenuAdapter;
    private ContentAdapter contentAdapter;
    private List<LeftMenuBean> leftMenuBeanList;
    private GridLayoutManager gridLayoutManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initView(root);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        homeViewModel.getData().observe(getViewLifecycleOwner(), new Observer<List<LeftMenuBean>>() {
            @Override
            public void onChanged(List<LeftMenuBean> leftMenuBeans) {
                initAdapter(leftMenuBeans);
            }
        });
        return root;
    }

    private void initView(View root) {
        mLeftSideNavigation = root.findViewById(R.id.left_side_navigation);
        mContent = root.findViewById(R.id.content);
    }

    private void initAdapter(List<LeftMenuBean> leftMenuBeans) {
        gridLayoutManager = new GridLayoutManager(getContext(),1);
        leftmenuAdapter = new LeftmenuAdapter(R.layout.left_side_navigation_item,leftMenuBeans);
        mLeftSideNavigation.setLayoutManager(gridLayoutManager);
        mLeftSideNavigation.setAdapter(leftmenuAdapter);
    }
}
