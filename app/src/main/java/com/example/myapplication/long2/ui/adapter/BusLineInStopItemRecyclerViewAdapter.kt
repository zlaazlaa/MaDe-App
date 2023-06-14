package com.example.myapplication.long2.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.myapplication.long2.ActivityJumper
import com.example.myapplication.long2.FavoriteOperator
import com.example.myapplication.R

import com.example.myapplication.databinding.ItemBusLineInStopBinding
import com.example.myapplication.long2.model.BusLine
import com.example.myapplication.long2.model.Favorite
import com.example.myapplication.long2.model.getFavorite

class BusLineInStopItemRecyclerViewAdapter(
    private val values: List<BusLine>,
    private val favorites:List<Favorite>,
    private val nowStopName: String,
    private val favoriteOperator: FavoriteOperator,
    private val activityJumper: ActivityJumper
) : RecyclerView.Adapter<BusLineInStopItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBusLineInStopBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        holder.routeNameView.text = item.name
        item.getNextStopName(nowStopName, true)?.let {
            holder.toDestinationLayout.visibility = View.VISIBLE
            holder.toDestinationNameView.text = "去往 ${item.endStopName!!}"
        } ?: let {
            holder.toDestinationLayout.visibility = View.GONE
        }
        item.getNextStopName(nowStopName, false)?.let {
            holder.toStartingLayout.visibility = View.VISIBLE
            holder.toStartingNameView.text = "去往 ${item.startStopName!!}"
        } ?: let {
            holder.toStartingLayout.visibility = View.GONE
        }

//        if (item.isFavorite)
        if(favorites.getFavorite(item) != null)
            holder.favoriteButton.setImageResource(R.drawable.ic_has_subscribe)
        else
            holder.favoriteButton.setImageResource(R.drawable.ic_not_subscribe)

        holder.favoriteButton.setOnClickListener{
            favorites.getFavorite(item)?.let {
                favoriteOperator.deleteFavorite(it)
            } ?:let {
                favoriteOperator.addFavorite(item)
            }
        }

        holder.root.setOnClickListener {
            if(it != holder.favoriteButton)
                activityJumper.jumpToBusLineDetail(item.city.cityStr,item.city.cityName,item.name,
                item.busStopNames.random()
                    )
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ItemBusLineInStopBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val routeNameView: TextView = binding.routeName
        val toDestinationNameView: TextView = binding.toDestinationName
        val toDestinationStatusView: TextView = binding.toDestinationStatus
        val toStartingNameView: TextView = binding.toStartingName
        val toStartingStatusView: TextView = binding.toStartingStatus

        val favoriteButton: ImageView = binding.subscribeButton

        val toDestinationLayout: ConstraintLayout = binding.toDestinationLayout
        val toStartingLayout: ConstraintLayout = binding.toStartingLayout

        val root = binding.root

        override fun toString(): String {
            return super.toString() + " '" + routeNameView.text + "'"
        }
    }


}