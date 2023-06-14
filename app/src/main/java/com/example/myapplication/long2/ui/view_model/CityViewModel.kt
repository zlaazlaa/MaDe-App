package com.example.myapplication.long2.ui.view_model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.long2.CwlRepository
import com.example.myapplication.long2.FavoriteOperator
import com.example.myapplication.long2.OperateType
import com.example.myapplication.long2.Searcher
import com.example.myapplication.long2.model.*

import kotlinx.coroutines.launch

class CityViewModel(application: Application):
    AndroidViewModel(application),
    FavoriteOperator, Searcher {
    private val repository: CwlRepository = CwlRepository.getInstance()
    private val _data:MutableLiveData<List<BusStop>> = MutableLiveData()
    private val _favorites:MutableLiveData<Triple<OperateType, Favorite?,MutableList<Favorite>>>
    = MutableLiveData()
    private val _lineData : MutableLiveData<List<BusLine>> = MutableLiveData()

    val data :LiveData<List<BusStop>> = _data

    val favorites :LiveData<Triple<OperateType, Favorite?,MutableList<Favorite>>> = _favorites

    var user:String? = null
    private set

    var nowCity: City? = null
    private set

    fun setUserAndCity(user:String,city: City){
       viewModelScope.launch {
           if (this@CityViewModel.nowCity != city) {
               nowCity = city
               repository.getBusStopAndLineByCityUnlimited(city).let { p->
                   p.first.let { _data.value = it }
                   p.second.let {
                        _lineData.value = it

                   }
                   Log.d("cwl", "setUserAndCity: data get")
               }
               if (this@CityViewModel.user != user) {
                   this@CityViewModel.user = user
                   val result = repository.getFavoritesUnlimited(user)
                   _favorites.value = Triple(
                       OperateType.NEW_DATA_SET,
                       result.firstOrNull(),
                       result
                   )
                   Log.d("cwl", "setUserAndCity: favorite get")
               }
           }
       }
    }

    override fun addFavorite(busLine: BusLine) {
        addFavorite(Favorite(busLine))
    }

    override fun addFavorite( busStop: BusStop) {
        addFavorite(Favorite(busStop))
    }

    private fun addFavorite(favorite: Favorite){
        val temp = _favorites.value?.third ?: ArrayList()
        temp.add(favorite)
        _favorites.value = Triple(
            OperateType.ADD,
            favorite,
            temp
        )
        postFavorite()
    }

    override fun deleteFavorite(favorite: Favorite) {
        _favorites.value?.third?.let {
            val temp = it
            temp.remove(favorite)

            _favorites.value = Triple(
                OperateType.DELETE,
                favorite,
                temp
            )
            postFavorite()
        }
    }

    override fun moveFavorite(city: City, fromPosition: Int, toPosition: Int) {
        //do nothing
    }

    override fun postFavorite(){
        user?.let { user->
            viewModelScope.launch {
                _favorites.value?.third?.let{repository.postFavorites(user,it)}
                    ?:let {
                        repository.postFavorites(user,ArrayList())
                    }
            }
        }
    }

    override fun search(name: String): List<StopOrLine> {
        val temp = ArrayList<Pair<Int, StopOrLine>>()
        _data.value?.mapNotNull {
            val itemName = it.name
            if(itemName.startsWith(name)) Pair(1,it)
            else if(itemName.contains(name)) Pair(2,it)
            else null
        }?.let {
            temp.addAll(it.map { p-> Pair(p.first, StopOrLine(p.second)) })
        }

        _lineData.value?.mapNotNull {
            val itemName = it.name
            if(itemName.startsWith(name)) Pair(1,it)
            else if(itemName.contains(name)) Pair(2,it)
            else null
        }?.let {
            temp.addAll(it.map { p-> Pair(p.first, StopOrLine(p.second)) })
        }
        return temp.sortedBy { it.first }.map { it.second }
    }
}