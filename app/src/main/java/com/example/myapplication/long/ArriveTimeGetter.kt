package com.example.myapplication.long

import com.example.myapplication.long.model.BusLine
import kotlin.time.Duration

interface ArriveTimeGetter {
    fun getArriveTime(busLine: BusLine, nowStopName:String, isToEnd:Boolean): Duration
}