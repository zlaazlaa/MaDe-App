package com.example.myapplication.long2.model

data class City(val cityStr:String ,val cityName:String = "No City Name"):Comparable<City>
{
    override fun equals(other: Any?): Boolean {
        if(other is City) return this.cityStr == other.cityStr
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return cityStr.hashCode()
    }

    override fun compareTo(other: City): Int {
        return this.cityStr.compareTo(other.cityStr)
    }
}
