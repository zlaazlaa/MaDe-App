package com.example.myapplication;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineQuery.SearchType;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.busline.BusLineSearch.OnBusLineSearchListener;
import com.amap.api.services.busline.BusStationResult;
import com.amap.api.services.busline.BusStationSearch.OnBusStationSearchListener;
import com.amap.api.services.core.AMapException;

import java.util.List;

import overlay.BusLineOverlay;
import util.ToastUtil;

/**
 * AMapV2地图中简单介绍公交线路搜索
 */
public class BuslineActivity extends Activity implements OnMarkerClickListener,
        InfoWindowAdapter, OnItemSelectedListener, OnBusLineSearchListener,
        OnClickListener, AMap.OnMyLocationChangeListener {
    private String BUS_NUM;
    private String CITY_ID;
    private AMap aMap;
    private MapView mapView;
    private ProgressDialog progDialog = null;// 进度框
    private EditText searchName;// 输入公交线路名称
    private Spinner selectCity;// 选择城市下拉列表
    private String[] itemCitys = { "北京-010", "郑州-0371", "上海-021" };
    private String cityCode = "";// 城市区号
    private int currentpage = 0;// 公交搜索当前页，第一页从0开始
    private BusLineResult busLineResult;// 公交线路搜索返回的结果
    private List<BusLineItem> lineItems = null;// 公交线路搜索返回的busline
    private BusLineQuery busLineQuery;// 公交线路查询的查询类

    private BusLineSearch busLineSearch;// 公交线路列表查询

    private Spinner spinnerGps;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.busline_activity);
        Intent intent = getIntent();
//        CITY_ID = intent.getStringExtra("city");
//        BUS_NUM = intent.getStringExtra("bus");
        cityCode = CITY_ID;
        /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
        //Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
//        MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(bundle);// 此方法必须重写
        init();
//        searchLine();

//        //实现定位蓝点
        MyLocationStyle locationStyle;
        locationStyle = new MyLocationStyle();//初始化定位蓝点样式
        locationStyle.interval(2000);//设置连续定位模式下的的定位间隔，值在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒
        locationStyle.showMyLocation(true);
        locationStyle.strokeColor(Color.BLUE);
        locationStyle.radiusFillColor(Color.RED);
        locationStyle.strokeWidth(20);
        locationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point));
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);
        locationStyle.showMyLocation(true);
        aMap.setMyLocationStyle(locationStyle); //设置定位蓝点的style
        aMap.getUiSettings().setMyLocationButtonEnabled(false); // 设置默认定位按钮是否显示，非必须设置

        locationStyle.anchor(0.0f, 1.0f);
        aMap.setMyLocationEnabled(true);//设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false
    }

    @Override
    public void onMyLocationChange(Location location) {
        // 定位回调监听
        if(location != null) {
            Log.e("amap", "onMyLocationChange 定位成功， lat: " + location.getLatitude() + " lon: " + location.getLongitude());
            Bundle bundle = location.getExtras();
            if(bundle != null) {
                int errorCode = bundle.getInt(MyLocationStyle.ERROR_CODE);
                String errorInfo = bundle.getString(MyLocationStyle.ERROR_INFO);
                // 定位类型，可能为GPS WIFI等，具体可以参考官网的定位SDK介绍
                int locationType = bundle.getInt(MyLocationStyle.LOCATION_TYPE);

                /*
                errorCode
                errorInfo
                locationType
                */
                Log.e("amap", "定位信息， code: " + errorCode + " errorInfo: " + errorInfo + " locationType: " + locationType );
            } else {
                Log.e("amap", "定位信息， bundle is null ");
            }
        } else {
            Log.e("amap", "定位失败");
        }
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
            aMap.setOnMyLocationChangeListener(this);
        }
    }

    /**
     * 设置marker的监听和信息窗口的监听
     */
    private void setUpMap() {
        aMap.setOnMarkerClickListener(this);
        aMap.setInfoWindowAdapter(this);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 公交线路搜索
     */
    public void searchLine() {
        currentpage = 0;// 第一页默认从0开始
        showProgressDialog();
        String search = BUS_NUM;
        if ("".equals(search)) {
            search = "641";
            searchName.setText(search);
        }
        busLineQuery = new BusLineQuery(search, SearchType.BY_LINE_NAME,
                cityCode);// 第一个参数表示公交线路名，第二个参数表示公交线路查询，第三个参数表示所在城市名或者城市区号
        busLineQuery.setPageSize(10);// 设置每页返回多少条数据
        busLineQuery.setPageNumber(currentpage);// 设置查询第几页，第一页从0开始算起
        try {
            busLineSearch = new BusLineSearch(this, busLineQuery);// 设置条件
            busLineSearch.setOnBusLineSearchListener(this);// 设置查询结果的监听
            busLineSearch.searchBusLineAsyn();// 异步查询公交线路名称
        } catch (AMapException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索:\n");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * 提供一个给默认信息窗口定制内容的方法
     */
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    /**
     * 提供一个个性化定制信息窗口的方法
     */
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /**
     * 点击marker回调函数
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;// 点击marker时把此marker显示在地图中心点
    }

    /**
     * 选择城市
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        String cityString = itemCitys[position];
        cityCode = cityString.substring(cityString.indexOf("-") + 1);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        cityCode = "010";
    }

    /**
     * 公交线路查询结果回调
     */
    @Override
    public void onBusLineSearched(BusLineResult result, int rCode) {
        dissmissProgressDialog();
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null
                    && result.getQuery().equals(busLineQuery)) {
                if (result.getQuery().getCategory() == SearchType.BY_LINE_NAME) {
                    if (result.getPageCount() > 0
                            && result.getBusLines() != null
                            && result.getBusLines().size() > 0) {
                        busLineResult = result;
                        lineItems = result.getBusLines();
                        if(lineItems != null) {
                            busLineQuery = new BusLineQuery(lineItems.get(0).getBusLineId(), SearchType.BY_LINE_ID,
                                    cityCode);// 第一个参数表示公交线路id，第二个参数表示公交线路id查询，第三个参数表示所在城市名或者城市区号
                            BusLineSearch busLineSearch = null;
                            try {
                                busLineSearch = new BusLineSearch(
                                        BuslineActivity.this, busLineQuery);
                                busLineSearch.setOnBusLineSearchListener(BuslineActivity.this);
                                busLineSearch.searchBusLineAsyn();// 异步查询公交线路id
                            } catch (AMapException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (result.getQuery().getCategory() == SearchType.BY_LINE_ID) {
//                    aMap.clear();// 清理地图上的marker
                    busLineResult = result;
                    lineItems = busLineResult.getBusLines();
                    if(lineItems != null && lineItems.size() > 0) {
                        BusLineOverlay busLineOverlay = new BusLineOverlay(this,
                                aMap, lineItems.get(0));
                        busLineOverlay.removeFromMap();
                        busLineOverlay.addToMap();
                        busLineOverlay.zoomToSpan();
                    }
                }
            } else {
                ToastUtil.show(BuslineActivity.this, "no result");
            }
        } else {
            ToastUtil.showerror(BuslineActivity.this, rCode);
        }
    }

    /**
     * 查询公交线路
     */
    @Override
    public void onClick(View v) {
//        searchLine();
    }
}
