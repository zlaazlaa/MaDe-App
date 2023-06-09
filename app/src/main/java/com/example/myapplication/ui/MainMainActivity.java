package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.example.myapplication.bean.CityBean;
import com.example.myapplication.bean.CityGroup;
import com.example.myapplication.databinding.ActivityMainMainBinding;
import com.example.myapplication.util.FileUtil;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainMainActivity extends AppCompatActivity implements ItemOnclickListener {

    private ActivityMainMainBinding binding;
    private List<String> stringList;
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
        List<String> cityHotList = getHotCityList();
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
    *@description 获取城市数据并分组排序
    *@return java.util.List<com.example.myapplication.bean.CityGroup>
    **/
    private List<CityGroup> getCityGroupList(){
        //获取城市json数据
        cityBeans = JSONObject.parseArray(FileUtil.readJsonStr(this), CityBean.class);//获取所有城市对象
        stringList = cityBeans.stream().map(CityBean::getCityName).collect(Collectors.toList());//提取所有城市对象的名称属性
        //按照首字母排序
        Comparator<CityBean> beanComparator = Comparator.comparing(CityBean::getCityStr, Collator.getInstance(Locale.ENGLISH));
        //使用定制排序Comparator
        List<CityBean> allCityList = cityBeans.stream().sorted(beanComparator).collect(Collectors.toList());
        List<CityGroup> cityGroupList = new ArrayList<>();
        String lastTitle = null;
        CityGroup cityGroup;
        List<CityBean> cityBeanList = null;
        //按照首字母分组
        for (int i = 0; i < allCityList.size(); i++) {
            CityBean cityBean = allCityList.get(i);
            if (lastTitle == null || !cityBean.getCityStr().startsWith(lastTitle)) {
                lastTitle = cityBean.getCityStr().substring(0, 1);//获取首字母
                cityGroup = new CityGroup();
                cityBeanList = new ArrayList<>();
                cityGroup.setTitle(lastTitle.toUpperCase());//给cityGroup对象设置首字母属性
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
    private List<String> getHotCityList(){
        List<String> cityBeans = new ArrayList<>();
        cityBeans.add(stringList.get(2));
        cityBeans.add(stringList.get(0));
        cityBeans.add(stringList.get(53));
        cityBeans.add(stringList.get(1));
        cityBeans.add(stringList.get(3));
        cityBeans.add(stringList.get(375));
        cityBeans.add(stringList.get(190));
        cityBeans.add(stringList.get(161));
        cityBeans.add(stringList.get(449));
        return cityBeans;
    }

    /**
    *@params [str]
    *@description 各处点击城市点击事件 在这里统一处理
    *@return void
    **/
    @Override
    public void itemOnclick(String str) {
        Intent intent = new Intent(this, CityActivity.class);//设置intent实现到城市详情页面跳转
        intent.putExtra("city", str);//通过putExtra方式传输城市数据
        startActivity(intent);
    }
}