package com.example.myapplication.long2.ui

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.long2.FavoriteOperator
import com.example.myapplication.long2.ui.adapter.FavoriteItemRecycleViewAdapter

class FavoriteItemTouchHelper(
    private val favoriteOperator: FavoriteOperator
) : ItemTouchHelper.Callback(){
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = 0 //don't handle swipe action

        return makeMovementFlags(dragFlags,swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val adapter = recyclerView.adapter
        return if(adapter is FavoriteItemRecycleViewAdapter) {
            val fromPosition = viewHolder.bindingAdapterPosition
            val toPosition = target.bindingAdapterPosition
            favoriteOperator.moveFavorite(adapter.city ,fromPosition, toPosition) //viewHolder实现,livedata变化后自动通知ui
            true
        } else false

    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        //do nothing
    }

    override fun isLongPressDragEnabled(): Boolean {
//        return canDrag
        return false
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        favoriteOperator.postFavorite()
        super.clearView(recyclerView, viewHolder)
    }
}