package com.example.myapplication.long.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.long.ActivityJumper
import com.example.myapplication.long.FavoriteOperator
import com.example.myapplication.long.FavoriteOperator.OperateType.*
import com.example.myapplication.long.OperateType
import com.example.myapplication.databinding.ItemFavoriteContainerBinding
import com.example.myapplication.long.model.City
import com.example.myapplication.long.model.Favorite
import com.example.myapplication.long.ui.FavoriteItemTouchHelper

class FavoriteContainerItemRecycleViewAdapter(
    private val operator: FavoriteOperator,
    private val activityJumper: ActivityJumper
) : RecyclerView.Adapter<FavoriteContainerItemRecycleViewAdapter.ViewHolder>() {
    private var dataSet: MutableList<Pair<City, FavoriteItemRecycleViewAdapter>> =
        ArrayList()

    fun setDataSet(
        operateType: OperateType,
        favorite: Favorite?,
        fromIndex: Int,
        toIndex: Int,
        data: Map<City, List<Favorite>>
    ) {
        when (operateType) {
            ADD -> {
                //do nothing
            }
            DELETE -> {
                val index = dataSet.indexOfFirst { it.first == favorite?.city }
                val newSubData =data[favorite?.city]

                if(newSubData!=null && newSubData.isNotEmpty())
                {
                    val subAdapter = dataSet[index].second
                    subAdapter.dataSet = data[favorite?.city]!!
                    subAdapter.notifyItemRemoved(fromIndex)
                    subAdapter.notifyItemRangeChanged(fromIndex,subAdapter.itemCount+1)
                }
                else {
                    dataSet.removeAt(index)
                    notifyItemRemoved(index)
                    notifyItemRangeChanged(index,dataSet.size+1)
                }

            }
            NEW_DATA_SET -> {
                this.dataSet = data.map {
                    Pair(
                        it.key,
                        FavoriteItemRecycleViewAdapter(
                            it.key,activityJumper, it.value,
                            operator
                        )
                    )
                }.sortedBy { it.first }.toMutableList()
                notifyDataSetChanged()
            }
            MOVE -> {
                data[favorite?.city]?.let { getData ->
                    dataSet.find { it.first == favorite?.city }?.let {
                        it.second.dataSet = getData
                        it.second.notifyItemMoved(fromIndex, toIndex)
                    }
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFavoriteContainerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pair = dataSet[position]
        val city = pair.first
        val cityAdapter = pair.second

        holder.cityName.text = city.cityName

        with(holder.recycle) {
            layoutManager = LinearLayoutManager(context)
            adapter = cityAdapter
            adapter?.let {
                if (it is FavoriteItemRecycleViewAdapter)
                    it.helper = holder.helper
            }

        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    inner class ViewHolder(binding: ItemFavoriteContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val cityName = binding.cityNameInFavorite
        val recycle = binding.recycleFavoriteInCity
        val helper = ItemTouchHelper(FavoriteItemTouchHelper(operator))

        init {
            helper.attachToRecyclerView(recycle)
        }
    }
}