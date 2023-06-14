package com.example.myapplication.long2.model
fun List<Favorite>.getFavorite(busLine: BusLine): Favorite?{
    return this.find {
        it.busLine == busLine
    }
}
fun List<Favorite>.getFavorite(busStop: BusStop): Favorite?{
    return this.find {
        it.busStop == busStop
    }
}

//继承StopOrLine 提供类型码，用来post数据
class Favorite: StopOrLine {
    constructor(busLine: BusLine) : super(busLine)
    constructor(busStop: BusStop): super(busStop)
    val typeCode:Int
    get(){
        return if(isBusLine()) TYPE_CODE_BUS_LINE
        else if(isBusStop()) TYPE_CODE_BUS_STOP
        else -1 //unknown type
    }

    companion object{
        const val TYPE_CODE_BUS_LINE = 0
        const val TYPE_CODE_BUS_STOP = 1
    }
}