package com.example.myapplication.long.model

//存放站点或线路，用来呈现搜索结果和收藏界面
open class StopOrLine{
    val city: City
    get(){
        return busStop?.city ?: busLine!!.city
    }
//    val name :String
    val name :String
    get(){
        return busStop?.name ?: busLine!!.name
    }

    var busStop: BusStop? = null
    private set

    var busLine: BusLine? = null
    private set


    constructor(
        busStop: BusStop,
    ) {
        this.busStop = busStop
    }

    constructor(
        busLine: BusLine,
    ) {
        this.busLine = busLine
    }

    fun isBusLine() = this.busLine != null
    fun isBusStop() = this.busStop != null

    override fun equals(other: Any?): Boolean {
        if(other is StopOrLine)return this.busStop == other.busStop&&
                this.busLine == other.busLine
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = (busStop?.hashCode() ?: 0)
        result = 31 * result + (busLine?.hashCode() ?: 0)
        return result
    }
}
