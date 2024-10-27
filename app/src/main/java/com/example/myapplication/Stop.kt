package com.example.myapplication

import java.io.File

data class Stop(val stop_id : String, val stop_code : String, val stop_name: String, val stop_lat : Double, val stop_lon : Double)
fun parseGtfsStops(file: File): List<Stop> {
    val lines = file.readLines().drop(1) // Skip header
    return lines.map { line ->
        val data = line.split(",")
        Stop(data[0], data[1], data[2], data[3].toDouble(), data[4].toDouble())
    }
}