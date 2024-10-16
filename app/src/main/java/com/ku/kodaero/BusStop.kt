package com.ku.kodaero

import com.naver.maps.geometry.LatLng

data class Location(
    val latitude: Double,
    val longitude: Double
)

data class BusStop(
    val name: String,
    val location: Location,
    val place_id: Int,
    val departure_times: List<String>,
    val path_node: List<List<Double>>
) {
    fun getPathNodeAsLatLng(): List<LatLng> {
        return path_node.map { LatLng(it[0], it[1]) }
    }
}


data class BusStopList(
    val stops: List<BusStop>
)