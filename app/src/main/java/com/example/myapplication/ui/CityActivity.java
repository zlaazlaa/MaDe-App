package com.example.myapplication.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivityCityBinding;
/**城市详情页面**/
public class CityActivity extends AppCompatActivity {

    private ActivityCityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //通过视图绑定viewBinding来实现布局与视图交互代码
        binding = ActivityCityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());//获取当前页面的布局
        String city = getIntent().getStringExtra("city");//获取传输的城市名称
        binding.cityName.setText(city);
        binding.cancel.setOnClickListener(v->{finish();});//通过调用finish方法返回主界面
    }
}