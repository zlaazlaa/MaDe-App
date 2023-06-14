package com.example.myapplication.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.bean.CityBean;
import com.example.myapplication.databinding.ItemCityHotBinding;

import java.util.List;

public class CityHotAdapter extends RecyclerView.Adapter<CityHotAdapter.ItemHolder> {
    private Context context;
    private List<CityBean> cityBeanList;
    private ItemOnclickListener onclickListener;

    public CityHotAdapter(Context context, List<CityBean> cityNameList) {
        this.context = context;
        this.cityBeanList = cityNameList;
    }

    public void setOnclickListener(ItemOnclickListener onclickListener) {
        this.onclickListener = onclickListener;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCityHotBinding binding = ItemCityHotBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.binding.tvHotCity.setText(cityBeanList.get(position).getCityName());
        holder.binding.tvHotCity.setOnClickListener(v->{
            if (onclickListener != null) {
                onclickListener.itemOnclick(cityBeanList.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return cityBeanList.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        ItemCityHotBinding binding;
        public ItemHolder(@NonNull ItemCityHotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
