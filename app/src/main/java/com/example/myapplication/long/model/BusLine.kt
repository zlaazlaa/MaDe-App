package com.example.myapplication.long.model


data class BusLine(
    val name:String,
    val city: City,
    private val busStopNames : List<String> = ArrayList(),
){
    val startStopName:String?
    get()=busStopNames.firstOrNull()


    val endStopName:String?
    get()=busStopNames.lastOrNull()

    fun getNextStopName(nowStopName:String,isToEndStop : Boolean):String?{
        val index = busStopNames.indexOf(nowStopName)
        return if(index == -1) null
        else busStopNames.getOrNull(
            if(isToEndStop) index+1 else index-1
        )
    }

    override fun equals(other: Any?): Boolean {
        if(other is BusLine) return this.name == other.name&& this.city == other.city
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + city.hashCode()
        return result
    }
}