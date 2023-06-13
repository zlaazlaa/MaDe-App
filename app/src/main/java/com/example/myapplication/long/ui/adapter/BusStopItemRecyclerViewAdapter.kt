package com.example.myapplication.long.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.*
import com.example.myapplication.long.FavoriteOperator.OperateType.*

import com.example.myapplication.databinding.ItemBusStopBinding
import com.example.myapplication.long.ActivityJumper
import com.example.myapplication.long.FavoriteOperator
import com.example.myapplication.long.OperateType
import com.example.myapplication.long.model.BusStop
import com.example.myapplication.long.model.Favorite
import com.example.myapplication.long.model.getFavorite

class BusStopItemRecyclerViewAdapter(
    private val favoriteOperator: FavoriteOperator,
    private val activityJumper: ActivityJumper
) : RecyclerView.Adapter<BusStopItemRecyclerViewAdapter.ViewHolder>() {

    private var isOpened : Array<Boolean> = arrayOf()
    var busStopData: List<BusStop> = ArrayList()
    set(value){
        field = value
        isOpened = Array(field.size){false}
        notifyDataSetChanged()
    }

    var favorites : List<Favorite> = ArrayList()
    private set

    fun setFavorites(operateType: OperateType, difference : Favorite?, favorites:List<Favorite>)
    {
        val before = this.favorites
        this.favorites = favorites
        when(operateType){
            ADD,DELETE -> {
                difference?.let {
                    it.busLine?.let { busLine->
                        val indexList = List(busStopData.size){i->i}.filter { i2->
                            busStopData[i2].busLines.contains(busLine)
                        }
                        indexList.forEach{i->
                            if(isOpened[i])
                                notifyItemChanged(i)
                        }
                    } ?: let { _->
                        val index = busStopData.indexOf(it.busStop)
                        if(index!=-1)
                            notifyItemChanged(index)
                    }
                }
            }
            NEW_DATA_SET -> notifyDataSetChanged()
            MOVE -> {
                //do nothing
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBusStopBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = busStopData[position]
        holder.stationNameView.text = item.name
        if (isOpened[position]){
            holder.routeNameListView.visibility = View.GONE
            with(holder.routeDetailListView) {
                visibility = View.VISIBLE
                layoutManager = LinearLayoutManager(context)
                adapter = BusLineInStopItemRecyclerViewAdapter(item.busLines,favorites,item.name,favoriteOperator,activityJumper)
            }
        }
        else {
            holder.routeDetailListView.visibility = View.GONE
            with(holder.routeNameListView)
            {
                visibility = View.VISIBLE
                text = item.busLines.joinToString(" ","","",-1,"..."){ r->r.name}
            }
        }

//        if(item.isFavorite)
        if(favorites.getFavorite(item)!=null)
            holder.favoriteButton.setImageResource(R.drawable.ic_has_subscribe)
        else
            holder.favoriteButton.setImageResource(R.drawable.ic_not_subscribe)



        holder.checkDetailBottom.setOnClickListener {
            activityJumper.jumpToBusStopDetail(item.city.cityStr, item.city.cityName, item.name)
        }

        holder.favoriteButton.setOnClickListener {
            val favorite =  favorites.getFavorite(item)
            favorite?.let {
                favoriteOperator.deleteFavorite(it)
            } ?: let {
                favoriteOperator.addFavorite(item)
            }
        }
    }

    override fun getItemCount(): Int = busStopData.size



    inner class ViewHolder(binding: ItemBusStopBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val stationNameView: TextView = binding.stationName
        val checkDetailBottom:TextView= binding.checkStationDetail
        val routeNameListView :TextView = binding.routesNameList
        val routeDetailListView:RecyclerView = binding.routesDetailList
        val favoriteButton = binding.favoriteButton

        init {
            binding.root.let {
                it.setOnClickListener { view ->
                    if(view != checkDetailBottom&& view != routeDetailListView&& view != favoriteButton)
                    {
                        onItemClicked()
                    }
                }
            }
        }

        private fun onItemClicked()
        {
            val pos = absoluteAdapterPosition
            isOpened[pos] = !isOpened[pos]
            notifyItemChanged(this.absoluteAdapterPosition)
        }

        override fun toString(): String {
            return super.toString() + " '" + stationNameView.text + "'"
        }
    }
}