package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.maps.MapsInitializer;
import com.example.myapplication.bean.CityBean;
import com.example.myapplication.bean.CityGroup;
import com.example.myapplication.databinding.ActivityMainMainBinding;
import com.example.myapplication.long2.ui.CityActivity;
import com.example.myapplication.long2.ui.FavoriteActivity;
import com.example.myapplication.ui.CityGroupAdapter;
import com.example.myapplication.ui.CityHotAdapter;
import com.example.myapplication.ui.ItemOnclickListener;
import com.example.myapplication.ui.SearchDialogFragment;
import com.example.myapplication.util.FileUtil;
import com.google.android.material.button.MaterialButton;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainMainActivity extends AppCompatActivity implements ItemOnclickListener {

    private ActivityMainMainBinding binding;
    //    private List<String> stringList;
    private List<CityBean> cityBeans;
    private MaterialButton materialButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        materialButton = binding.jumpToFav;
        materialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(MainMainActivity.this, FavoriteActivity.class);
               intent.putExtra("user", "mqy");
               startActivity(intent);
            }
        });
        init();
        privacyCompliance();
    }

    private void privacyCompliance() {
        MapsInitializer.updatePrivacyShow(MainMainActivity.this,true,true);
        SpannableStringBuilder spannable = new SpannableStringBuilder("\"亲，感谢您对XXX一直以来的信任！我们依据最新的监管要求更新了XXX《隐私权政策》，特向您说明如下\n1.为向您提供交易相关基本功能，我们会收集、使用必要的信息；\n2.基于您的明示授权，我们可能会获取您的位置（为您提供附近的商品、店铺及优惠资讯等）等信息，您有权拒绝或取消授权；\n3.我们会采取业界先进的安全措施保护您的信息安全；\n4.未经您同意，我们不会从第三方处获取、共享或向提供您的信息；\n");
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 35, 42, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        new AlertDialog.Builder(this)
                .setTitle("温馨提示(隐私合规示例)")
                .setMessage(spannable)
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MapsInitializer.updatePrivacyAgree(MainMainActivity.this,true);
                    }
                })
                .setNegativeButton("不同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MapsInitializer.updatePrivacyAgree(MainMainActivity.this,false);
                    }
                })
                .show();
    }

    private void init() {
        //从json文件里面读取城市信息并按首字母分组排序
        List<CityGroup> cityGroupList = getCityGroupList();
        //拿热门城市数据
        List<CityBean> cityHotList = getHotCityList();
        CityHotAdapter cityHotAdapter = new CityHotAdapter(this, cityHotList);
        binding.listHot.setAdapter(cityHotAdapter);
        binding.listHot.setNestedScrollingEnabled(false);
        cityHotAdapter.setOnclickListener(this);
        CityGroupAdapter cityGroupAdapter = new CityGroupAdapter(this, cityGroupList);
        binding.listGroup.setAdapter(cityGroupAdapter);
        binding.listGroup.setNestedScrollingEnabled(false);
        cityGroupAdapter.setOnclickListener(this);
        //搜索框点击事件
        binding.layoutSearch.setOnClickListener(v -> {
            SearchDialogFragment dialogFragment = new SearchDialogFragment(cityBeans, this);
            dialogFragment.show(getSupportFragmentManager(), "show");
        });
    }


    /**
     * @return java.util.List<com.tech.stationsearch.bean.CityGroup>
     * @params []
     * @date 2023/6/7 1:29
     * @description 获取城市数据并分组排序
     **/
    private List<CityGroup> getCityGroupList() {
        //获取城市json数据
        cityBeans = JSONObject.parseArray(FileUtil.readJsonStr(this), CityBean.class);
//        stringList = cityBeans.stream().map(CityBean::getCityName).collect(Collectors.toList());
        //按照首字母排序
        Comparator<CityBean> beanComparator = Comparator.comparing(CityBean::getCityStr, Collator.getInstance(Locale.ENGLISH));
        List<CityBean> allCityList = cityBeans.stream().sorted(beanComparator).collect(Collectors.toList());
        List<CityGroup> cityGroupList = new ArrayList<>();
        String lastTitle = null;
        CityGroup cityGroup;
        List<CityBean> cityBeanList = null;
        //按照首字母分组
        for (int i = 0; i < allCityList.size(); i++) {
            CityBean cityBean = allCityList.get(i);
            if (lastTitle == null || !cityBean.getCityStr().startsWith(lastTitle)) {
                lastTitle = cityBean.getCityStr().substring(0, 1);
                cityGroup = new CityGroup();
                cityBeanList = new ArrayList<>();
                cityGroup.setTitle(lastTitle.toUpperCase());
                cityGroup.setCityList(cityBeanList);
                cityBeanList.add(cityBean);
                cityGroupList.add(cityGroup);
            } else {
                cityBeanList.add(cityBean);
            }
        }
        return cityGroupList;
    }

    /**
     * 获取热门城市
     **/
    private List<CityBean> getHotCityList() {
        List<CityBean> hotCityBean = new ArrayList<>();
        hotCityBean.add(cityBeans.get(2));
        hotCityBean.add(cityBeans.get(0));
        hotCityBean.add(cityBeans.get(53));
        hotCityBean.add(cityBeans.get(1));
        hotCityBean.add(cityBeans.get(3));
        hotCityBean.add(cityBeans.get(375));
        hotCityBean.add(cityBeans.get(190));
        hotCityBean.add(cityBeans.get(161));
        hotCityBean.add(cityBeans.get(449));
        return hotCityBean;
    }

    /**
     * @return void
     * @params [str]
     * @date 2023/6/7 1:30
     * @description 各处点击城市点击事件 在这里统一处理
     **/
    @Override
    public void itemOnclick(CityBean cityBean) {
        Intent intent = new Intent(this, CityActivity.class);
        intent.putExtra("city_name", cityBean.getCityName());
        intent.putExtra("city_str", cityBean.getCityStr());
        intent.putExtra("user", "mqy");
        startActivity(intent);
    }
}