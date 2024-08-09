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
    ): Call<ApiResponse<BuildingSearchResponse>>

    @GET("search/buildings/{buildingId}/floor/{floor}/rooms")//임시데이터
    fun searchBuildingFloor(
        @Path("buildingId") buildingId: Int,
        @Path("floor") floor: Int? = 1
    ): Call<ApiResponse<RoomListResponse>>

    @GET("search/buildings")
    fun getAllBuildings(): Call<ApiResponse<BuildingListResponse>>

    @GET("search/facilities")
    fun searchFacilities(
        @Query("type") type: String
    ): Call<ApiResponse<BuildingDetailListResponse>>

    @GET("search/buildings/{buildingId}/facilities")
    fun getFacilities(
        @Path("buildingId") buildingId: Int,
        @Query("type") type: String
    ): Call<ApiResponse<FacilityListResponse>>

    @GET("routes")
    fun getRoutes(
        @Query("startType") startType: String,
        @Query("startId") startId: Int? = null,
        @Query("startLat") startLat: Double? = null,
        @Query("startLong") startLong: Double? = null,
        @Query("endType") endType: String,
        @Query("endId") endId: Int? = null,
        @Query("endLat") endLat: Double? = null,
        @Query("endLong") endLong: Double? = null,
        @Query("barrierFree") barrierFree: String? = null
    ): Call<ApiResponse<RouteResponse>>

    @GET("search/buildings/{id}/floor/{floor}/mask/{redValue}")
    fun getMaskInfo(
        @Path("id") id: Int,
        @Path("floor") floor: Int,
        @Path("redValue") redValue: Int,
        @Query("type") type: String
    ): Call<ApiResponse<MaskInfoResponse>>

    @GET("search/place/{roomid}")
    fun getPlaceInfo(
        @Path("roomid") roomId: Int,
        @Query("placeType") placeType: String
    ): Call<ApiResponse<PlaceInfoResponse>>
}

data class BuildingListResponse(
    val list: List<BuildingItem>
)

data class BuildingSearchResponse(
    val list: List<BuildingSearchItem>
)


data class BuildingItem(
    val buildingId: Int?,
    val name: String,
    val imageUrl: String,
    val detail: String,
    val address: String?,
    val operatingTime: String,
    val needStudentCard: Boolean,
    val longitude: Double?,
    val latitude: Double?,
    val floor: Int?,
    val underFloor: Int?,
    val nextBuildingTime: String,
    val facilityTypes: List<String>,
    val operating: Boolean
)


data class BuildingSearchItem(
    val id: Int,
    val name: String,
    val address: String?,
    val longitude: Double?,
    val latitude: Double?,
    val floor: Int,
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

data class BuildingDetailListResponse(
    val list: List<BuildingDetailItem>
)

data class BuildingDetailItem(
    val buildingId: Int,
    val name: String,
    val address: String?,
    val longitude: Double?,
    val latitude: Double?,
    val floor: Int?,
    val imageUrl: String?,
    val detail: String?,
    val operatingTime: String?,
    val needStudentCard: Boolean?
)

data class FacilityListResponse(
    val buildingId: Int,
    val buildingName: String,
    val type: String,
    val facilities: Map<String, List<FacilityItem>>
)

data class FacilityItem(
    val facilityId: Int,
    val name: String,
    val availability: Boolean,
    val longitude: Double,
    val latitude: Double,
    val xcoord: Int,
    val ycoord: Int
)

data class RouteResponse(
    val duration: Int,
    val path: List<RoutePath>
)

data class RoutePath(
    val inOut: Boolean,
    val buildingId: Int?,
    val floor: Int?,
    val route: List<List<Double>>,
    val info: String
)

data class MaskInfoResponse(
    val placeType: String,
    val placeId: Int
)

data class PlaceInfoResponse(
    val type: String,
    val id: Int,
    val name: String,
    val detail: String,
    val availability: Boolean,
    val plugAvailability: Boolean,
    val imageUrl: String?,
    val operatingTime: String,
    val longitude: Double?,
    val latitude: Double?,
    val maskIndex: Int,
    val bookmarked: Boolean,
    val nextPlaceTime: String,
    val xcoord: Int,
    val ycoord: Int,
    val operating: Boolean
)