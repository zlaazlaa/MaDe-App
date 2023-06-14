package com.example.myapplication.long2.model

data class BusStop(
    val name: String,
    val city: City,
    val busLines: List<BusLine>,
){
    override operator fun equals(other: Any?): Boolean {
        if(other is BusStop) return this.name == other.name && this.city == other.city
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + city.hashCode()
        return result
    }
}