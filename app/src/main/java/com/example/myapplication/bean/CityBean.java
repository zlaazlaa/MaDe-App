package com.example.myapplication.bean;

/**
*@description 城市实体类
**/
public class CityBean {
    private Integer id;
    private String cityName;
    private String cityStr;

    public CityBean(Integer id, String cityName, String cityStr) {
        this.id = id;
        this.cityName = cityName;
        this.cityStr = cityStr;
    }

    public CityBean() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityStr() {
        return cityStr;
    }

    public void setCityStr(String cityStr) {
        this.cityStr = cityStr;
    }

    @Override
    public String toString() {
        return "CityBean{" +
                "id=" + id +
                ", cityName='" + cityName + '\'' +
                ", cityStr='" + cityStr + '\'' +
                '}';
    }
}
