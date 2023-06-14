package com.example.myapplication.long2

import android.util.Log
import com.example.myapplication.long2.model.BusLine
import com.example.myapplication.long2.model.BusStop
import com.example.myapplication.long2.model.City
import com.example.myapplication.long2.model.Favorite
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import org.json.JSONArray
import org.json.JSONObject

private const val FIELD_BUS_LINE_NAME_TEST = "bus_name"
private const val FIELD_BUS_LINE_NAME = "name"
private const val FIELD_ALL_BUS_LINE_STATION = "all_station"
private const val FIELD_BUS_LINE_ID = "id"

private const val FIELD_BUS_STOP_NAME = "name"
private const val FIELD_BUS_STOP_ID = "id"
private const val FIELD_BUS_STOP_SEQUENCE = "sequence"
private const val FIELD_BUS_STOP_LOCATION = "location"

private const val FIELD_FAVORITE_CITY_NAME = "city_name"
private const val FIELD_FAVORITE_CITY_STR = "city_str"
private const val FIELD_FAVORITE_TYPE = "favorite_type"
private const val FIELD_FAVORITE_NAME = "name"
private const val FIELD_FAVORITE_USER_NAME = "user"
private const val FIELD_FAVORITE_POST_CITY = "city"
private const val FIELD_FAVORITE_POST_INDEX = "index"
private const val FIELD_FAVORITE_POST_FAVORITES = "favorites"

class CwlRepository {
    private var _busStopAndLineMap: HashMap<City, Pair<List<BusStop>, List<BusLine>>> = HashMap()

    private val requester = DataRequester.getInstance()


     fun getBusStopByCityAndName(city: City,stopName: String):BusStop{
        return _busStopAndLineMap[city]!!.first.find { it.name == stopName }!!
    }
    //没有使用，此处代表调用的是有限次api
    suspend fun getBusStopAndLineByCity(city: City): Pair<List<BusStop>, List<BusLine>> =
        withContext(Dispatchers.Default) {
            val temp = _busStopAndLineMap[city]
            if (temp == null) {
                val temp2: Pair<List<BusStop>, List<BusLine>> = getBusStopsAndLinesData(city)
                _busStopAndLineMap[city] = temp2
                temp2
            } else
                temp
        }

    //对外方法，获取站点和线路数据
    suspend fun getBusStopAndLineByCityUnlimited(city: City): Pair<List<BusStop>, List<BusLine>> =
        withContext(Dispatchers.Default) {
            val temp = _busStopAndLineMap[city]
            if (temp == null) {
                val temp2: Pair<List<BusStop>, List<BusLine>> =
                    getBusStopsAndLinesDataUnlimited(city)
                _busStopAndLineMap[city] = temp2

                temp2
            } else
                temp
        }

    //没用使用 有限次
    private suspend fun getBusStopsAndLinesData(city: City): Pair<List<BusStop>, List<BusLine>> {
        //通过城市获取线路名
        //TODO 当前是测试数据 query_all_bus
        val jsonArray = JSONArray(requester.fetchBusLinesNameByCity(city))


        val mapNameBusLine = HashMap<String, BusLine>() //name-busLine map
        val mapBusStopNameLines =
            HashMap<String, ArrayList<BusLine>>() //name-line_list map

        val channel = Channel<Pair<String, String>?>()

        runBlocking {


            val jobs = ArrayList<Job>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val busLineName = obj.getString(FIELD_BUS_LINE_NAME_TEST)
                jobs.add(
                    launch(Dispatchers.IO) {
                        produceAllStationJson(channel, city, busLineName)
                    }
                )
            }

            launch {
                jobs.forEachIndexed { i, j ->
                    Log.d("cwl", "getBusStopsAndLinesData: before $i")
                    j.join()
                    Log.d("cwl", "getBusStopsAndLinesData: after $i")
                }
                channel.close()
                Log.d("cwl", "getBusStopsAndLinesData: channel closed")
            }

            Log.d("cwl", "getBusStopsAndLinesData:before consume")
            channel.consumeEach { receive ->
                receive?.let {
                    Log.d("cwl", "consume(${Thread.currentThread().name}):data")
                    val busLineName = receive.first
                    val json = receive.second
                    val obj = JSONObject(json)
                    val allStation = JSONArray(obj.getString(FIELD_ALL_BUS_LINE_STATION))
                    val receivedRelations = List(allStation.length()) { i ->
                        val objStation = allStation.getJSONObject(i)

                        StopAndSequence(
                            objStation.getString(FIELD_BUS_STOP_NAME),
                            objStation.getInt(FIELD_BUS_STOP_SEQUENCE)
                        )
                    }
                    val busLine = BusLine(
                        busLineName,
                        city,
                        receivedRelations.sortedBy { r -> r.sequence }.map { r -> r.stopName }
                    )
                    for (relation in receivedRelations) {
                        val list = mapBusStopNameLines[relation.stopName] ?: ArrayList()
                        list.add(busLine)
                        mapBusStopNameLines[relation.stopName] = list
                    }
                    mapNameBusLine.putIfAbsent(
                        busLineName,
                        busLine
                    )
                } ?: let {
                    Log.d("cwl", "consume(${Thread.currentThread().name}):null  ")
                }
            }


        }
        Log.d("cwl", "getBusStopsAndLinesData: consume over ")

        return Pair(
            mapBusStopNameLines.map {
                BusStop(
                    it.key,
                    city,
                    it.value
                )
            },
            mapNameBusLine.map { p -> p.value }
        )
    }

    private suspend fun produceAllStationJson(
        channel: SendChannel<Pair<String, String>?>,
        city: City,
        busLineName: String
    ) {
        val json = requester.fetchBusLineInfoByCityAndName(city, busLineName)
        val name = Thread.currentThread().name
        Log.d("cwl", "($name): try send ${json.substring(0..10)}")
        if (json.startsWith('{')) {
            Log.d("cwl", "($name): before send ${json.substring(0..10)}")
            channel.send(Pair(busLineName, json))
            Log.d("cwl", "($name): has send ${json.substring(0..10)}")
        } else {
            Log.d("cwl", "($name): before send null")
            channel.send(null)
            Log.d("cwl", "($name): has send null  ")
        }
    }



    suspend fun getBusStopsWithLines(busLines: List<BusLine>): List<BusStop> {
        val cityLines = busLines.groupBy { it.city }
        val result = ArrayList<BusStop>()
        for (entry in cityLines) {
            val stops = getBusStopsAndLinesData(entry.key).first
            result.addAll(
                stops.filter {
                    it.busLines.any { busLine ->
                        entry.value.contains(busLine)
                    }
                }
            )
        }
        return result
    }

    private suspend fun getBusStopsAndLinesDataUnlimited(city: City): Pair<List<BusStop>, List<BusLine>> {
        //TODO 当前用的是测试数据
        val jsonArray = JSONArray(requester.fetchBusLinesNameByCity(city))

        val mapStopNameWithLines = HashMap<String, ArrayList<BusLine>>()
        val listLines = ArrayList<BusLine>()

        val busLineNameAndJsonChannel = Channel<Pair<String, String>>(10)
        runBlocking {
            val jobs = ArrayList<Job>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val busLineName = obj.getString(FIELD_BUS_LINE_NAME_TEST) //TODO test data field
                val cityStr = obj.getString(FIELD_FAVORITE_CITY_STR) //TODO test data field
                if (cityStr != city.cityStr) continue
                jobs.add(
                    launch(Dispatchers.IO) {
                        produceBusLineJsonUnlimited(busLineNameAndJsonChannel, city, busLineName)
                    })
            }

            launch {
                jobs.forEach {
                    it.join()
                }
                busLineNameAndJsonChannel.close()
            }

            busLineNameAndJsonChannel.consumeEach { receive ->
                val busLineName = receive.first
                val jsonStopNames = JSONArray(
                    receive.second
                )
                val stopNames =
                    Array(jsonStopNames.length()) { jsonStopNames.getString(it) }.toList()
                val busLine = BusLine(busLineName, city, stopNames)
                for (stopName in stopNames) {
                    val lines = mapStopNameWithLines.getOrDefault(stopName, ArrayList())
                    lines.add(busLine)
                    mapStopNameWithLines[stopName] = lines
                }
                listLines.add(busLine)
            }
        }


        return Pair(
            mapStopNameWithLines.map {
                BusStop(it.key, city, it.value.distinct())
            },
            listLines
        )
    }

    private suspend fun produceBusLineJsonUnlimited(
        sendChannel: SendChannel<Pair<String, String>>,
        city: City,
        busLineName: String
    ) {
        val json = requester.fetchBusLineInfoByCityAndNameUnlimited(
            city, busLineName
        )
        val send = Pair(busLineName, json)
        sendChannel.send(send)
    }

    suspend fun postFavorites(userName: String, favorites: List<Favorite>) {
        val obj = JSONObject()
        val arr = JSONArray()
        obj.put(FIELD_FAVORITE_USER_NAME, userName)
        for ((index, favorite) in favorites.withIndex()) {
            val arrItem = JSONObject()
            arrItem.put(FIELD_FAVORITE_TYPE, favorite.typeCode)
            arrItem.put(FIELD_FAVORITE_NAME, favorite.name)
            arrItem.put(FIELD_FAVORITE_POST_CITY, favorite.city.cityStr)
            arrItem.put(FIELD_FAVORITE_POST_INDEX, index)
            arr.put(arrItem)
        }
        obj.put(FIELD_FAVORITE_POST_FAVORITES, arr)
        val json = obj.toString()
        requester.postFavorite(json)
    }

    suspend fun getFavorites(userName: String): List<Favorite> {
        val jsonArray = JSONArray(
            withContext(Dispatchers.Default) {
                requester.fetchFavoriteByUsername(userName)
            }
        )

        return Array(jsonArray.length()) {
            val obj = jsonArray.getJSONObject(it)
            val name = obj.getString(FIELD_FAVORITE_NAME)
            val typeCode = obj.getInt(FIELD_FAVORITE_TYPE)

            val cityName = obj.getString(FIELD_FAVORITE_CITY_NAME)
            val cityStr = obj.getString(FIELD_FAVORITE_CITY_STR)
            val city = City(cityStr = cityStr, cityName = cityName)

            when (typeCode) {
                Favorite.TYPE_CODE_BUS_STOP -> {
                    getBusStopAndLineByCity(city).first.find { busStop ->
                        busStop.name == name
                    }?.let { busStop ->
                        return@Array Favorite(busStop)
                    }
                }
                Favorite.TYPE_CODE_BUS_LINE -> {
                    getBusStopAndLineByCity(city).second.find { busLine ->
                        busLine.name == name
                    }?.let { busLine ->
                        return@Array Favorite(busLine)
                    }
                }
                else -> {
                    Log.w("cwl", "getFavorites: unknown favorite type \"$typeCode\"")
                    null
                }
            }
        }.toList().filterNotNull()
    }

    suspend fun getFavoritesUnlimited(userName: String): MutableList<Favorite> {
        val jsonArray = JSONArray(
            withContext(Dispatchers.IO) {
                requester.fetchFavoriteByUsername(userName)
            }
        )

        return Array(jsonArray.length()) {
            val obj = jsonArray.getJSONObject(it)
            val name = obj.getString(FIELD_FAVORITE_NAME)
            val typeCode = obj.getInt(FIELD_FAVORITE_TYPE)

            val cityName = obj.getString(FIELD_FAVORITE_CITY_NAME)
            val cityStr = obj.getString(FIELD_FAVORITE_CITY_STR)
            val city = City(cityStr = cityStr, cityName = cityName)

            when (typeCode) {
                Favorite.TYPE_CODE_BUS_STOP -> {
                    getBusStopAndLineByCityUnlimited(city).first.find { busStop ->
                        busStop.name == name
                    }?.let { busStop ->
                        return@Array Favorite(busStop)
                    }
                }
                Favorite.TYPE_CODE_BUS_LINE -> {
                    getBusStopAndLineByCityUnlimited(city).second.find { busLine ->
                        busLine.name == name
                    }?.let { busLine ->
                        return@Array Favorite(busLine)
                    }
                }
                else -> {
                    Log.w("cwl", "getFavoritesUnlimited: unknown favorite type \"$typeCode\"")
                    null
                }
            }
        }.filterNotNull().toMutableList()
    }


    private inner class StopAndSequence(
        val stopName: String,
        val sequence: Int
    )

    companion object {
        private var INSTANCE: CwlRepository? = null
        fun getInstance(): CwlRepository {
            if (INSTANCE == null) INSTANCE = CwlRepository()
            return INSTANCE!!
        }
    }
}