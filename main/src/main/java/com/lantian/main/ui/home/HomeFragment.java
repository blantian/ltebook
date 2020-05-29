package com.lantian.main.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lantian.base.common.bean.LeftMenuBean;
import com.lantian.main.R;
import com.lantian.main.adapter.home.LeftmenuAdapter;

import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private LeftmenuAdapter leftmenuAdapter;
    private GridLayoutManager gridLayoutManager;
    private RecyclerView mTitel;
    private View mNavigationDivider;
    private RecyclerView mNavigationRecyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initView(root);
        homeViewModel.getData().observe(getViewLifecycleOwner(), new Observer<List<LeftMenuBean>>() {
            @Override
            public void onChanged(List<LeftMenuBean> leftMenuBeans) {
                initAdapter(leftMenuBeans);
            }
        });
        return root;
    }

    private void initView(View root) {
        mTitel = root.findViewById(R.id.titel);
    }

    private void initAdapter(List<LeftMenuBean> leftMenuBeans) {
        gridLayoutManager = new GridLayoutManager(getContext(), 1);
        leftmenuAdapter = new LeftmenuAdapter(R.layout.left_side_navigation_item,leftMenuBeans);
        mTitel.setLayoutManager(gridLayoutManager);
        mTitel.setAdapter(leftmenuAdapter);
    }
}
