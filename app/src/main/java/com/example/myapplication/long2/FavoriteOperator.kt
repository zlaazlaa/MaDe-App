package com.example.myapplication.long2

import com.example.myapplication.long2.model.BusLine
import com.example.myapplication.long2.model.BusStop
import com.example.myapplication.long2.model.City
import com.example.myapplication.long2.model.Favorite

typealias OperateType = FavoriteOperator.OperateType

interface FavoriteOperator{
    fun addFavorite(busLine: BusLine)
    fun addFavorite(busStop: BusStop)
    fun deleteFavorite(favorite: Favorite)
    fun postFavorite()
    fun moveFavorite(city: City, fromPosition:Int, toPosition:Int)

    enum class OperateType {
        ADD,DELETE,NEW_DATA_SET,MOVE
    }
}