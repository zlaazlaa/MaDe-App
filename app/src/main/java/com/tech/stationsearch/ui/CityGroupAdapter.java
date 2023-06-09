package com.tech.stationsearch.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.stationsearch.bean.CityBean;
import com.tech.stationsearch.bean.CityGroup;
import com.tech.stationsearch.databinding.ItemCityGroupBinding;

import java.util.List;

/**
*@description 主页面按字母排序的adapter
**/
public class CityGroupAdapter extends RecyclerView.Adapter<CityGroupAdapter.ItemHolder>{
    private Context context;
    private List<CityGroup> cityGroupList;
    private ItemOnclickListener onclickListener;


    public CityGroupAdapter(Context context, List<CityGroup> cityGroupList) {
        this.context = context;
        this.cityGroupList = cityGroupList;
    }

    public void setOnclickListener(ItemOnclickListener onclickListener) {
        this.onclickListener = onclickListener;
    }
    @NonNull
    @Override
    public CityGroupAdapter.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCityGroupBinding binding = ItemCityGroupBinding.inflate(LayoutInflater.from(context), parent, false);
        return new CityGroupAdapter.ItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CityGroupAdapter.ItemHolder holder, int position) {
        CityGroup cityGroup = cityGroupList.get(position);
        //为分组设置标题（首字母）
        holder.binding.tvCityTitle.setText(cityGroup.getTitle());
        CityAdapter cityAdapter = new CityAdapter(context, cityGroup.getCityList());
        holder.binding.listCity.setAdapter(cityAdapter);
        cityAdapter.setOnclickListener(onclickListener);
    }

    @Override
    public int getItemCount() {
        return cityGroupList.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        ItemCityGroupBinding binding;
        public ItemHolder(@NonNull ItemCityGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
