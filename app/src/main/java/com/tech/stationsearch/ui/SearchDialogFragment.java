package com.tech.stationsearch.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.tech.stationsearch.R;
import com.tech.stationsearch.bean.CityBean;
import com.tech.stationsearch.databinding.FragmentSearchDialogBinding;

import java.util.List;
import java.util.stream.Collectors;


/**
*@description 点击搜索框弹窗fragment，双击搜索框进行输入
**/
public class SearchDialogFragment extends DialogFragment {


    private List<CityBean> cityBeans;
    private FragmentSearchDialogBinding binding;
    private CityAdapter cityAdapter;
    private ItemOnclickListener itemOnclickListener;
    private String keyword;

    //通过构造方法把数据和监听回调传过来
    public SearchDialogFragment(List<CityBean> cityBeans,ItemOnclickListener onclickListener) {
        // Required empty public constructor
        this.cityBeans = cityBeans;
        this.itemOnclickListener = onclickListener;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        //设置为全屏dialog
        setStyle(DialogFragment.STYLE_NO_TITLE,R.style.DialogFullScreen);
    }

    @Override
    public void onStart() {
        super.onStart();
        //搜索框请求焦点
        binding.editSearch.requestFocus();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //搜索输入框监听事件
        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                keyword = s.toString().trim();
                //如果有输入
                if (keyword.length()>0){
                    //搜索到的城市结果列表
                    List<CityBean> searchCity = cityBeans.stream().filter(cityBean -> cityBean.getCityName().contains(keyword)).collect(Collectors.toList());
                    //输入未搜索到结果
                    if (searchCity.isEmpty()) {
                        //隐藏隐藏有结果的搜索列表，显示空结果的搜索列表
                        binding.listSearch.setVisibility(View.INVISIBLE);
                        binding.tvNoResult.setVisibility(View.VISIBLE);
                    }
                    //输入搜索到结果
                    else {
                        binding.listSearch.setVisibility(View.VISIBLE);
                        binding.tvNoResult.setVisibility(View.INVISIBLE);
                        if (cityAdapter == null) {
                            cityAdapter = new CityAdapter(getContext(), searchCity, keyword, true);
                            //给搜索列表项设置点击事件
                            cityAdapter.setOnclickListener(itemOnclickListener);
                            binding.listSearch.setAdapter(cityAdapter);
                        } else {
                            cityAdapter.reloadDataSpan(searchCity,keyword);
                            //如果输入字段有匹配的搜索结果列表，则继续输入后刷新搜索列表
                        }
                    }
                    binding.ivClear.setVisibility(View.VISIBLE);
                    binding.searchResult.setVisibility(View.VISIBLE);
                }else {
                    binding.ivClear.setVisibility(View.INVISIBLE);
                    binding.searchResult.setVisibility(View.INVISIBLE);
                }
            }
        });
        binding.ivClear.setOnClickListener(v -> binding.editSearch.setText(null));
        //点击输入框但未输入任何字段时，点击透明遮罩时关闭弹窗
        binding.layoutEmptySearch.setOnClickListener(v -> {
            if (binding.searchResult.getVisibility() != View.VISIBLE) {
                dismiss();
            }
        });
        binding.cancel.setOnClickListener(v -> dismiss());//点击取消按钮，关闭弹窗
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSearchDialogBinding.inflate(inflater, container, false);
        Window win = getDialog().getWindow();//获取当前对话框窗口
        // 一定要设置Background，如果不设置，window属性设置无效
        win.setBackgroundDrawable( new ColorDrawable(Color.TRANSPARENT));

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.TOP;
        // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
        params.width =  ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        win.setAttributes(params);
        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        return super.onCreateDialog(savedInstanceState);
    }
}