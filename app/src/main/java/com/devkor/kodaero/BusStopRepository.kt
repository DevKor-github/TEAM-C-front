package com.devkor.kodaero

import android.content.Context
import com.google.gson.Gson
import java.io.InputStreamReader

class BusStopRepository(private val context: Context) {

    fun getBusStops(): List<BusStop>? {
        // res/raw/bus_stops.json 파일을 읽음
        val inputStream = context.resources.openRawResource(R.raw.bus_stops)
        val reader = InputStreamReader(inputStream)

        // Gson으로 JSON 파싱
        val gson = Gson()
        val busStopList = gson.fromJson(reader, BusStopList::class.java)

        // 반환된 정류장 리스트
        return busStopList.stops
    }
}
