package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.example.myapplication.bean.CityBean;
import com.example.myapplication.bean.CityGroup;
import com.example.myapplication.databinding.ActivityMainMainBinding;
import com.example.myapplication.long2.ui.CityActivity;
import com.example.myapplication.util.FileUtil;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init(){
        //从json文件里面读取城市信息并按首字母分组排序
        List<CityGroup> cityGroupList = getCityGroupList();
        //拿热门城市数据
        List<CityBean> cityHotList = getHotCityList();
        CityHotAdapter cityHotAdapter = new CityHotAdapter(this,cityHotList);
        binding.listHot.setAdapter(cityHotAdapter);
        binding.listHot.setNestedScrollingEnabled(false);
        cityHotAdapter.setOnclickListener(this);
        CityGroupAdapter cityGroupAdapter = new CityGroupAdapter(this,cityGroupList);
        binding.listGroup.setAdapter(cityGroupAdapter);
        binding.listGroup.setNestedScrollingEnabled(false);
        cityGroupAdapter.setOnclickListener(this);
        //搜索框点击事件
        binding.layoutSearch.setOnClickListener(v -> {
            SearchDialogFragment dialogFragment = new SearchDialogFragment(cityBeans, this);
            dialogFragment.show(getSupportFragmentManager(),"show");
        });
    }


    /**
     *@params []
     *@date 2023/6/7 1:29
     *@description 获取城市数据并分组排序
     *@return java.util.List<com.tech.stationsearch.bean.CityGroup>
     **/
    private List<CityGroup> getCityGroupList(){
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

    /**获取热门城市**/
    private List<CityBean> getHotCityList(){
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
     *@params [str]
     *@date 2023/6/7 1:30
     *@description 各处点击城市点击事件 在这里统一处理
     *@return void
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