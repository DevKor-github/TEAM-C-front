package com.example.deckor_teamc_front

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class ApiResponse<T>(
    val statusCode: Int,
    val message: String,
    val data: T
)

interface ApiService {
    @GET("search")
    fun search(
        @Query("keyword") keyword: String,
        @Query("building_id") buildingId: Int? = null
    ): Call<ApiResponse<List<BuildingItem>>>

    @GET("search/buildings/{buildingId}/floor/2/rooms")//임시데이터
    fun searchBuildingFloor(@Path("buildingId") buildingId: Int): Call<ApiResponse<RoomListResponse>>

    @GET("allBuildings")
    fun getAllBuildings(): Call<ApiResponse<BuildingListResponse>>
}

data class BuildingListResponse(
    val buildingList: List<BuildingItem>
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

data class RoomListResponse(
    val roomList: List<RoomList>
)

data class RoomList(
    val type: String,
    val id: Int,
    val facilityType: String,
    val name: String,
    val detail: String,
    val availability: Boolean,
    val plugAvailability: Boolean,
    val imageUrl: String,
    val operatingTime: String,
    val longitude: Double,
    val latitude: Double,
    val xcoord: Int,
    val ycoord: Int
)
