package com.example.deckor_teamc_front

data class BuildingItem(
        val id: Int? = null,
        val name: String,
        val address: String? = null,
        val floor: Int? = null,
        val longitude: Double? = null,
        val latitude: Double? = null,
        val placeType: String
)
