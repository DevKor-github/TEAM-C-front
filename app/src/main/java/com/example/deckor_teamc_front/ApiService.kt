package com.example.deckor_teamc_front

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("search")
    fun search(
        @Query("keyword") keyword: String,
        @Query("building_id") buildingId: Int? = null
    ): Call<ApiResponse>
}



data class ApiResponse(
    val statusCode: Int,
    val message: String,
    val data: List<BuildingItem>
)

data class BuildingItem(
    val id: Int?,
    val name: String,
    val address: String?,
    val longitude: Double?,
    val latitude: Double?,
    val floor: Int?,
    val placeType: String
)
