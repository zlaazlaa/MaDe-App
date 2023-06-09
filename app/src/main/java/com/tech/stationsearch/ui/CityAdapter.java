package com.tech.stationsearch.ui;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.stationsearch.R;
import com.tech.stationsearch.bean.CityBean;
import com.tech.stationsearch.databinding.ItemCityBinding;
import com.tech.stationsearch.util.CommonUtil;

import java.util.List;

/**
*@description 城市适配adapter
**/
public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ItemHolder> {
    private Context context;
    private List<CityBean> cityBeanList;
    private String keyword;
    private boolean isSpanText;
    private ItemOnclickListener onclickListener;


    public CityAdapter(Context context, List<CityBean> cityBeanList) {
        this.context = context;
        this.cityBeanList = cityBeanList;
    }

    public CityAdapter(Context context, List<CityBean> cityBeanList,String keyword,boolean isSpanText) {
        this.context = context;
        this.cityBeanList = cityBeanList;
        this.keyword = keyword;
        this.isSpanText = isSpanText;
    }

    public void setOnclickListener(ItemOnclickListener onclickListener) {
        this.onclickListener = onclickListener;
    }
    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCityBinding binding = ItemCityBinding.inflate(LayoutInflater.from(context), parent, false);
        return new CityAdapter.ItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        //是否为搜索场景 搜索场景需要高亮
        if (isSpanText && keyword != null) {
            holder.binding.tvCity.setTextColor(Color.BLACK);
            int keywordColor = Color.parseColor("#029def");
            SpannableString searchText = CommonUtil.matcherSearchText(keywordColor, cityBeanList.get(position).getCityName(), keyword);
            holder.binding.tvCity.setText(searchText);
        } else {
            holder.binding.tvCity.setText(cityBeanList.get(position).getCityName());
        }
        holder.itemView.setOnClickListener(v->{
            if (onclickListener != null) {
                onclickListener.itemOnclick(cityBeanList.get(holder.getAdapterPosition()).getCityName());
            }
        });
    }

    /**刷新搜索数据**/
    public void reloadDataSpan(List<CityBean> cityBeanList, String keyword){
        this.cityBeanList = cityBeanList;
        this.keyword = keyword;
        this.isSpanText = true;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return cityBeanList.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        ItemCityBinding binding;
        public ItemHolder(@NonNull ItemCityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
