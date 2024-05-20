package com.example.deckor_teamc_front

import com.naver.maps.geometry.LatLng

data class BuildingItem(
    val name: String,
    val address: String,
    val distance: String,
    val location: LatLng
)
