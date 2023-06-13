package com.example.myapplication.long

import android.util.Log
import com.example.myapplication.long.model.City
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit



private const val TIME_OUT_SECOND = 60L
private const val DOMAIN = "http://49.234.42.16/api/"
//private const val DOMAIN = "https://ljm-python.azurewebsites.net/"

class DataRequester {
    private val client =
        OkHttpClient.Builder().readTimeout(TIME_OUT_SECOND, TimeUnit.SECONDS).build()

    private suspend fun postData(url: String, jsonString: String) = withContext(Dispatchers.IO){
        Log.d("cwl", "postData: $url")
        val requestBody = jsonString.toRequestBody(
            "application/json; charset=utf-8".toMediaType()
        )
        val request = Request.Builder().url(url).post(requestBody).build()
        val result = client.newCall(request).execute().body?.string() ?: "no body return"
        Log.d("cwl", "postData: $result")
    }

    private fun fetchData(url: String): String {
        Log.d("cwl", "fetchData(${Thread.currentThread().name}): $url")
        val request: Request = Request.Builder().url(url)
            .method("GET", null).build()
        val call = client.newCall(request)
        val response = call.execute()

        return response.body?.string() ?: "[]"
    }

    suspend fun postFavorite(jsonString: String) {
        Log.d("cwl", "postFavorite: $jsonString")
        postData(DOMAIN + "save_favorites", jsonString)
    }

    fun fetchBusLinesNameByCity(city: City): String = fetchData(DOMAIN + "query_all_bus")


    fun fetchBusLineInfoByCityAndName(city: City, name: String): String {
        val result = fetchData(DOMAIN + "bus_line_info?" + "city=${city.cityStr}&bus=$name")
        Log.d("cwl", "fetchOver(${Thread.currentThread().name}): $result")
        return result
    }

    fun fetchFavoriteByUsername(userName: String): String {
        val data = fetchData(DOMAIN +"query_favorite?"+ "message=$userName")
        Log.d("cwl", "fetchOver(${Thread.currentThread().name}): ${if(data.length>10)data.substring(0..9)else data}")
        return data
    }

    //new interface
    fun fetchBusLineInfoByCityAndNameUnlimited(city: City, name: String):String{
        val data = fetchData(DOMAIN +"get_station_details?"+"city=${city.cityStr}&key_word=$name")
        Log.d("cwl", "fetchOver(${Thread.currentThread().name}): ${if(data.length>10)data.substring(0..9)else data}")
        return data
    }

    fun fetchBusStopInfoByCityAndNameUnlimited(city: City, name: String):String {
        return fetchData(DOMAIN +"get_line_by_station?"+"city=${city.cityStr}&station=$name")
    }


    companion object{
        private var INSTANCE: DataRequester? = null

        fun getInstance(): DataRequester {
            if(INSTANCE ==null) INSTANCE = DataRequester()
            return INSTANCE!!
        }
    }
}