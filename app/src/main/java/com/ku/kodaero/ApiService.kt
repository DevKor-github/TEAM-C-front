package com.ku.kodaero

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
    fun getAllBuildings(
        @Query("placeType") placeType: String
    ): Call<ApiResponse<BuildingListResponse>>

    @GET("search/buildings/{buildingId}")
    fun getBuildingDetail(
        @Path("buildingId") buildingId: Int
    ): Call<ApiResponse<BuildingDetailItem>>

    @GET("search/facilities")
    fun searchFacilities(
        @Query("placeType") placeType: String
    ): Call<ApiResponse<IndividualFacilityListResponse>>

    @GET("search/buildings/{buildingId}/facilities")
    fun getFacilities(
        @Path("buildingId") buildingId: Int,
        @Query("placeType") placeType: String
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
        @Query("endLong") endLong: Double? = null
    ): Call<ApiResponse<List<RouteResponse>>>  // List<RouteResponse>로 반환 타입 변경

    @GET("search/buildings/{id}/floor/{floor}/mask/{redValue}")
        fun getMaskInfo(
        @Path("id") id: Int,
        @Path("floor") floor: Int,
        @Path("redValue") redValue: Int,
        @Query("type") type: String
    ): Call<ApiResponse<MaskInfoResponse>>

    @GET("search/place/{placeId}")
    fun getPlaceInfo(
        @Path("placeId") roomId: Int
    ): Call<ApiResponse<PlaceInfoResponse>>

    @POST("users/login")
    fun getUserTokens(
        @Body loginRequest: LoginRequest
    ): Call<ApiResponse<UserTokens>>

    @GET("users/mypage")
    fun getUserInfo(
    ): Call<ApiResponse<UserInfo>>

    @DELETE("users")
    fun secessionUser(
    ): Call<Void>

    @POST("suggestions")
    fun summitSuggestion(
        @Body suggestionRequest: SuggestionRequest
    ): Call<Void>

    @GET("categories")
    fun getCategories(
        @Query("type") type: String,
        @Query("id") id: Int
    ): Call<ApiResponse<CategoryResponse>>

    @POST("categories")
    fun addCategory(
        @Header("AccessToken") accessToken: String,
        @Body categoryItem: CategoryItemRequest
    ): Call<ApiResponse<Any>>


    @POST("/api/bookmarks")
    fun addBookmarks(@Body request: BookmarkRequest): Call<ApiResponse<Any>>


    @GET("/api/categories/{categoryId}/bookmarks")
    fun getBookmarks(@Path("categoryId") categoryId: Int): Call<ApiResponse<BookmarkResponse>>

    // 북마크 삭제 API 메서드
    @DELETE("/api/categories/{categoryId}/bookmarks/{bookmarkId}")
    fun deleteBookmark(
        @Path("categoryId") categoryId: Int,
        @Path("bookmarkId") bookmarkId: Int
    ): Call<ApiResponse<Any>>


    @DELETE("/api/categories/{categoryId}")
    fun deleteCategory(@Path("categoryId") categoryId: Int): Call<ApiResponse<Any>>

    @PATCH("users/username")
    fun editUserName(
        @Query("username") username: String
    ): Call<Void>

    @GET("/api/koyeon/pubs")
    fun getPubs(): Call<ApiResponse<PubsResponse>>

    @GET("/api/koyeon")
    fun getKoyeonStatus(): Call<ApiResponse<KoyeonStatus>>

    @GET("/api/koyeon/pubs/{pubId}")
    fun getPubInfo(@Path("pubId") pubId: Int): Call<ApiResponse<PubDetail>>

}


data class KoyeonStatus(
    val id: Int,
    val isKoyeon: Boolean
)



data class PubsResponse(
    val list: List<Pub>
)

data class Pub(
    val id: Int,
    val name: String,
    val longitude: Double,
    val latitude: Double
)

@Parcelize
data class PubDetail(
    val id: Int,
    val name: String,
    val sponsor: String?,
    val longitude: Double,
    val latitude: Double,
    val address: String?,
    val operatingTime: String?,
    val menus: List<String>?
) : Parcelable


data class LoginRequest(
    val provider: String,
    val email: String,
    val token: String
)

data class SuggestionRequest(
    val title: String,
    val type: String,
    val content: String
)

data class UserTokens(
    val accessToken: String,
    val refreshToken: String
)

@Parcelize
data class UserInfo(
    val username: String,
    val email: String,
    val profileUrl: String?,
    val provider: String,
    val role: String,
    val level: String,
    val categoryCount: Int
) : Parcelable

data class BuildingListResponse(
    val list: List<BuildingItem>
)

data class BuildingSearchResponse(
    val list: List<BuildingSearchItem>
)

data class BuildingItem(
    val buildingId: Int,
    val name: String,
    val imageUrl: String,
    val detail: String,
    val address: String?,
    val weekdayOperatingTime: String?,
    val saturdayOperatingTime: String?,
    val sundayOperatingTime: String?,
    val needStudentCard: Boolean,
    val longitude: Double?,
    val latitude: Double?,
    val floor: Int,
    val underFloor: Int,
    val nextBuildingTime: String?,
    val placeTypes: List<String>,
    val operating: Boolean,
    val isFromPlaceInfo: Boolean = false
)

data class BuildingDetailItem(
    val buildingId: Int,
    val name: String,
    val address: String?,
    val longitude: Double?,
    val latitude: Double?,
    val imageUrl: String?,
    val weekdayOperatingTime: String?,
    val saturdayOperatingTime: String?,
    val sundayOperatingTime: String?,
    val details: String?,
    val bookmarked: Boolean,
    val existTypes: List<String>,
    val nextBuildingTime: String,
    val mainFacilityList: List<BuildingDetailMainFacilityList>,
    val operating: Boolean
)

data class BuildingDetailMainFacilityList(
    val name: String,
    val type: String,
    val placeId: Int,
    val imageUrl: String?
)


data class BuildingSearchItem(
    val id: Int?,
    val name: String,
    val address: String?,
    val longitude: Double?,
    val latitude: Double?,
    val floor: Int,
    val locationType: String,
    val placeType: String,
    val buildingId: Int
)
// RoomListResponse 클래스
data class RoomListResponse(
    val roomList: List<RoomList>,
    val nodeList: List<NodeList>
)

// RoomList 데이터 클래스
data class RoomList(
    val id: Int,
    val placeType: String,
    val name: String,
    val detail: String,
    val availability: Boolean,
    val plugAvailability: Boolean,
    val imageUrl: String?,
    val weekdayOperatingTime: String?,
    val saturdayOperatingTime: String?,
    val sundayOperatingTime: String?,
    val longitude: Double,
    val latitude: Double,
    val xcoord: Int,
    val ycoord: Int,
    val operating: Boolean
)

// NodeList 데이터 클래스
data class NodeList(
    val id: Int,
    val type: String,
    val xcoord: Int,
    val ycoord: Int
)

// Restroom 데이터 클래스
data class Restroom(
    val id: Int,
    val type: String,
    val xcoord: Int,
    val ycoord: Int
)

data class FacilityListResponse(
    val buildingId: Int,
    val buildingName: String,
    val type: String,
    val facilities: Map<String, List<FacilityItem>>
)

data class IndividualFacilityListResponse(
    val facilities: List<FacilityItem>
)

data class FacilityItem(
    val id: Int,
    val placeType: String,
    val name: String,
    val availability: Boolean,
    val weekdayOperatingTime: String?,
    val saturdayOperatingTime: String?,
    val sundayOperatingTime: String?,
    val imageUrl: String?,
    val longitude: Double,
    val latitude: Double,
    val detail: String,
    val buildingId: Int,
    val buildingName: String?,
    val floor: Int,
    val address: String?,
    val needStudentCard: Boolean?,
    val plugAvailability: Boolean,
    val locationType: String?,
    val description: String,
    val starAverage: String,
    val xcoord: Int,
    val ycoord: Int,
    val operating: Boolean
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
    val buildingId: Int,
    val floor: Int,
    val type: String,
    val placeId: Int,
    val facilityType: String,
    val name: String,
    val detail: String,
    val availability: Boolean,
    val plugAvailability: Boolean,
    val imageUrl: String?,
    val operatingTime: String,
    val longitude: Double,
    val latitude: Double,
    val maskIndex: Int,
    val bookmarked: Boolean,
    val nextPlaceTime: String,
    val operating: Boolean,
    val xcoord: Int,
    val ycoord: Int
)

data class CategoryResponse(
    val categoryList: List<CategoryItem>,
    val bookmarkId: Int?
)

data class CategoryItem(
    val categoryId: Int,
    val category: String,
    val color: String,  // 색상 값
    val memo: String,
    val bookmarkCount: Int,
    val bookmarked: Boolean
)

data class CategoryItemRequest(
    val category: String,
    val color: String,
    val memo: String
)

data class BookmarkResponse(
    val bookmarkList: List<Bookmark>
)

data class Bookmark(
    val bookmarkId: Int,
    val locationType: String,
    val locationId: Int,
    val memo: String
)