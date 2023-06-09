package com.example.myapplication.bean;

import java.util.List;

/**
*@date 2023/6/7 21:30
*@description 城市分组实体类
**/
public class CityGroup {
    /**首字母**/
    private String title;
    private List<CityBean> cityList;

    public CityGroup(String title, List<CityBean> cityList) {
        this.title = title;
        this.cityList = cityList;
    }

    public CityGroup() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<CityBean> getCityList() {
        return cityList;
    }

    public void setCityList(List<CityBean> cityList) {
        this.cityList = cityList;
    }

    @Override
    public String toString() {
        return "CityGroup{" +
                "title='" + title + '\'' +
                ", cityList=" + cityList +
                '}';
    }
}
