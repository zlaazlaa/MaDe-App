package com.example.myapplication.long.ui

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.long.FavoriteOperator
import com.example.myapplication.long.ui.adapter.FavoriteItemRecycleViewAdapter

class FavoriteItemTouchHelper(
    private val itemMover: FavoriteOperator
) : ItemTouchHelper.Callback(){
    var canDrag = false
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
            itemMover.moveFavorite(adapter.city ,fromPosition, toPosition) //viewHolder实现,livedata变化后自动通知ui
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
        super.clearView(recyclerView, viewHolder)
        canDrag = false
    }
}