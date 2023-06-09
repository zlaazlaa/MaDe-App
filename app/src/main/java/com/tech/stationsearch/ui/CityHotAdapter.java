package com.tech.stationsearch.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.stationsearch.bean.CityBean;
import com.tech.stationsearch.databinding.ItemCityHotBinding;

import java.util.List;

public class CityHotAdapter extends RecyclerView.Adapter<CityHotAdapter.ItemHolder> {
    private Context context;
    private List<String> cityNameList;
    private ItemOnclickListener onclickListener;

    public CityHotAdapter(Context context, List<String> cityNameList) {
        this.context = context;
        this.cityNameList = cityNameList;
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
        holder.binding.tvHotCity.setText(cityNameList.get(position));
        holder.binding.tvHotCity.setOnClickListener(v->{
            if (onclickListener != null) {
                onclickListener.itemOnclick(cityNameList.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return cityNameList.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        ItemCityHotBinding binding;
        public ItemHolder(@NonNull ItemCityHotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
