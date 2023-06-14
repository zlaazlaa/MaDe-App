package com.example.myapplication.long2.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.long2.ActivityJumper
import com.example.myapplication.long2.FavoriteOperator
import com.example.myapplication.databinding.ItemFavoriteBinding
import com.example.myapplication.long2.model.City
import com.example.myapplication.long2.model.Favorite

class FavoriteItemRecycleViewAdapter(
    val city: City,
    val activityJumper: ActivityJumper,
    var dataSet:List<Favorite>,
    private val operator: FavoriteOperator,
) :RecyclerView.Adapter<FavoriteItemRecycleViewAdapter.ViewHolder>(){
    var helper:ItemTouchHelper? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFavoriteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]
        item.busLine?.let {
            holder.favoriteInfoUnder.text = "${it.startStopName} 前往 ${it.endStopName}"
        }

        item.busStop?.let {
            holder.favoriteInfoUnder.text = it.busLines.joinToString(" ") { l->l.name }
        }

        holder.favoriteName.text = item.name

        holder.root.setOnClickListener{view->
            if(view != holder.dragHandle&& view!=holder.deleteButton){
                item.let {
                    if(it.isBusLine())
                        activityJumper.jumpToBusLineDetail(item.city.cityStr,item.city.cityName,item.name,item.busLine!!.startStopName!!)
                    else if(it.isBusStop())
                        activityJumper.jumpToBusStopDetail(item.city.cityStr, item.city.cityName, item.name)
                }
            }
        }

        holder.deleteButton.setOnClickListener{ _->
            item.let {
                operator.deleteFavorite(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class ViewHolder(binding:ItemFavoriteBinding):RecyclerView.ViewHolder(binding.root) {
        val favoriteName = binding.stopOrLine.itemName
        val dragHandle = binding.stopOrLine.handleButton
        val favoriteInfoUnder = binding.stopOrLine.infoUnder
        val favoriteInfoUnder2 = binding.stopOrLine.infoUnder2
        val root = binding.root
        val deleteButton = binding.stopOrLine.favoriteButtonInStopOrLineItem

        init {
            dragHandle.setOnTouchListener { _, event ->
                return@setOnTouchListener if(MotionEvent.ACTION_DOWN == event.action) {
                    helper?.startDrag(this)
                    true
                }
                else if(MotionEvent.ACTION_UP == event.action) {
                    operator.postFavorite()
                    true
                }
                else false
            }
        }
    }


}