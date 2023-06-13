package com.example.myapplication.long

import com.example.myapplication.long.model.BusLine
import com.example.myapplication.long.model.BusStop
import com.example.myapplication.long.model.City
import kotlinx.coroutines.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val EARTH_RADIUS = 6371.0 // Radius of the Earth in kilometers
private const val KM_PER_MINUTE:Double = 17.5/60
private const val MINUTE_IN_MILLISECOND = 60*1000L
