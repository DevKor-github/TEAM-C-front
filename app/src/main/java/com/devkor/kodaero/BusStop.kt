package com.devkor.kodaero

data class Location(
    val latitude: Double,
    val longitude: Double
)

data class BusStop(
    val name: String,
    val location: Location,
    val departure_times: List<String>
)

data class BusStopList(
    val stops: List<BusStop>
)