package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.MyLocationStyle;

public class get_location extends AppCompatActivity implements AMap.OnMyLocationChangeListener {
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);

        mapView = findViewById(R.id.map);

        mapView.onCreate(savedInstanceState);

        //地图控制器对象
        AMap aMap = mapView.getMap();
        aMap.setOnMyLocationChangeListener(this);

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
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMyLocationChange(Location location) {
        // 定位回调监听
        if (location != null) {
            Log.e("amap", "onMyLocationChange 定位成功， lat: " + location.getLatitude() + " lon: " + location.getLongitude());
            Bundle bundle = location.getExtras();
            if (bundle != null) {
                int errorCode = bundle.getInt(MyLocationStyle.ERROR_CODE);
                String errorInfo = bundle.getString(MyLocationStyle.ERROR_INFO);
                // 定位类型，可能为GPS WIFI等，具体可以参考官网的定位SDK介绍
                int locationType = bundle.getInt(MyLocationStyle.LOCATION_TYPE);

                /*
                errorCode
                errorInfo
                locationType
                */
                Log.e("amap", "定位信息， code: " + errorCode + " errorInfo: " + errorInfo + " locationType: " + locationType);
            } else {
                Log.e("amap", "定位信息， bundle is null ");

            }

        } else {
            Log.e("amap", "定位失败");
        }
    }
}