package com.example.myapplication.long2.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.long2.ActivityJumper
import com.example.myapplication.databinding.ItemStopOrLineBinding
import com.example.myapplication.long2.model.StopOrLine

class SearchItemAdapter(
    private val activityJumper: ActivityJumper
)
 : RecyclerView.Adapter<SearchItemAdapter.ViewHolder>(){
    var dataSet :List<StopOrLine> = ArrayList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    inner class ViewHolder(binding : ItemStopOrLineBinding) : RecyclerView.ViewHolder(binding.root) {
        val infoUnder  = binding.infoUnder
        val infoUnder2 = binding.infoUnder2
        val historyName = binding.itemName
        val favoriteButton = binding.favoriteButtonInStopOrLineItem
        val deleteHistoryButton = binding.handleButton
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemStopOrLineBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]
        item.busLine?.let {
            holder.historyName.text = it.name
            holder.infoUnder.text = "${it.startStopName} 开往 ${it.endStopName}"
        } ?: item.busStop?.let {
            holder.historyName.text = it.name
            holder.infoUnder.text = it.busLines.joinToString(",") { l->l.name }
        }
        holder.infoUnder2.visibility = View.GONE
        holder.favoriteButton.visibility = View.GONE
        holder.deleteHistoryButton.visibility  =View.GONE

        holder.root.setOnClickListener {
            item.busLine?.let {
                activityJumper.jumpToBusLineDetail(item.city.cityStr,item.city.cityName,item.name,it.busStopNames.random())
            }
            item.busStop?.let {
                activityJumper.jumpToBusStopDetail(item.city.cityStr, item.city.cityName, item.name)
            }
        }
    }

    override fun getItemCount(): Int = dataSet.size

}