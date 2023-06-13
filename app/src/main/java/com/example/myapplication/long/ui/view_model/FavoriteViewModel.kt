package com.example.myapplication.long.ui.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.long.CwlRepository
import com.example.myapplication.long.FavoriteOperator
import com.example.myapplication.long.OperateType
import com.example.myapplication.long.model.BusLine
import com.example.myapplication.long.model.BusStop
import com.example.myapplication.long.model.City
import com.example.myapplication.long.model.Favorite
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application):AndroidViewModel(application) ,
    FavoriteOperator
    {
    private val repository = CwlRepository.getInstance()
    private var _favorites:MutableLiveData<
            Triple<
                    OperateType,
                    Triple<Favorite?,Int,Int>,
                    Map<City,List<Favorite>>>> = MutableLiveData()
    val favorite:LiveData<
            Triple<
                    OperateType,
                    Triple<Favorite?,Int,Int>,
                    Map<City,List<Favorite>>>> = _favorites

    var user:String?=null
    set(value){
       field = value
       field?.let {
           viewModelScope.launch {
               val temp = repository.getFavoritesUnlimited(it).groupBy{it.city}
               _favorites.value = Triple(
                   OperateType.NEW_DATA_SET,
                   Triple(null,-1,-1),
                   temp
               )
               _favorites.value?.third?.let {
                   val list = ArrayList<BusLine>()
                   for(entry in it)
                   {

                       entry.value.forEach { f->
                          if(f.isBusLine())
                              list.add(f.busLine!!)
                       }
                   }
               }
           }
       }
    }

    override fun addFavorite(busLine: BusLine) {
        //do nothing
    }

    override fun addFavorite(busStop: BusStop) {
        //do nothing
    }


    override fun deleteFavorite(favorite: Favorite)
    {
        _favorites.value?.third?.let {
            it[favorite.city]?.toMutableList()?.let {  list->
                val index = list.indexOf(favorite)
                list.remove(favorite)
                val temp = it.toMutableMap()
                temp[favorite.city] = list
                _favorites.value = Triple(
                    OperateType.DELETE,
                    Triple(favorite,index,-1),
                    temp
                )

                postFavorite()
            }
        }
    }

    override fun moveFavorite(city: City, fromPosition: Int, toPosition: Int) {
        if(fromPosition != toPosition) {
            _favorites.value?.third?.let {
                it[city]?.toMutableList()?.let { list->
                    val tempFavorite = list.removeAt(fromPosition)
                    list.add(toPosition,tempFavorite)
                    val temp = it.toMutableMap()
                    temp[city] = list
                    _favorites.value = Triple(
                        OperateType.MOVE,
                        Triple(temp[city]!!.first(),fromPosition,toPosition),
                        temp
                    )
                }
            }
        }
    }


    override fun postFavorite(){
        user?.let { user->
            viewModelScope.launch {
                _favorites.value?.third?.let{
                    repository.postFavorites(user,it.flatMap {e-> e.value })
                }
                    ?:let {
                        repository.postFavorites(user,ArrayList())
                    }
            }
        }
    }
}