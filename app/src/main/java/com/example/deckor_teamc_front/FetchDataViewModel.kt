package com.example.deckor_teamc_front

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FetchDataViewModel : ViewModel() {
    private val _buildingSearchItems = MutableLiveData<List<BuildingSearchItem>>()
    val buildingSearchItems: LiveData<List<BuildingSearchItem>> get() = _buildingSearchItems

    private val _buildingDetail = MutableLiveData<BuildingDetailItem?>()
    val buildingDetail: MutableLiveData<BuildingDetailItem?> get() = _buildingDetail

    private val _facilityList = MutableLiveData<Map<Int, List<FacilityItem>>>()
    val facilityList: LiveData<Map<Int, List<FacilityItem>>> get() = _facilityList

    private val _buildingList = MutableLiveData<List<BuildingItem>>()
    val buildingList: LiveData<List<BuildingItem>> get() = _buildingList

    private val _buildingSearchList = MutableLiveData<List<BuildingSearchItem>>()
    val buildingSearchList: LiveData<List<BuildingSearchItem>> get() = _buildingSearchList

    private val _roomList = MutableLiveData<List<RoomList>>()
    val roomList: LiveData<List<RoomList>> get() = _roomList

    private val _routeResponse = MutableLiveData<RouteResponse>()
    val routeResponse: LiveData<RouteResponse> get() = _routeResponse

    private val service = RetrofitClient.instance

    private val _placeInfoResponse = MutableLiveData<PlaceInfoResponse?>()
    val placeInfoResponse: LiveData<PlaceInfoResponse?> get() = _placeInfoResponse

    fun searchBuildings(keyword: String, buildingId: Int? = null) {
        val call = if (buildingId != null) {
            service.search(keyword, buildingId)
        } else {
            service.search(keyword)
        }

        call.enqueue(object : Callback<ApiResponse<BuildingSearchResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<BuildingSearchResponse>>,
                response: Response<ApiResponse<BuildingSearchResponse>>
            ) {
                if (response.isSuccessful) {
                    _buildingSearchItems.value = response.body()?.data?.list ?: emptyList()
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<BuildingSearchResponse>>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })

    }

    fun fetchBuildingList() {
        Log.d("FetchBuildingList","Done")
        service.getAllBuildings().enqueue(object : Callback<ApiResponse<BuildingListResponse>> {
            override fun onResponse(call: Call<ApiResponse<BuildingListResponse>>, response: Response<ApiResponse<BuildingListResponse>>) {
                if (response.isSuccessful) {
                    _buildingList.value = response.body()?.data?.list ?: emptyList()
                } else {
                    // 응답 실패 시, 에러 로그 출력
                    val errorBody = response.errorBody()?.string()
                    Log.e("FetchDataViewModel", "Error response: $errorBody")
                }
            }

            override fun onFailure(call: Call<ApiResponse<BuildingListResponse>>, t: Throwable) {
                // 네트워크 실패 시 로그 출력
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })
    }

    fun fetchBuildingDetail(buildingId: Int) {
        Log.d("FetchDataViewModel", "Starting fetch for buildingId: $buildingId")

        service.getBuildingDetail(buildingId).enqueue(object : Callback<ApiResponse<BuildingDetailItem>> {
            override fun onResponse(call: Call<ApiResponse<BuildingDetailItem>>, response: Response<ApiResponse<BuildingDetailItem>>) {
                if (response.isSuccessful) {
                    val buildingDetail = response.body()?.data
                    Log.d("FetchDataViewModel", "Response: $response")

                    if (buildingDetail != null) {
                        _buildingDetail.value = buildingDetail
                        Log.d("FetchDataViewModel", "Received building detail: $buildingDetail")
                    } else {
                        Log.e("FetchDataViewModel", "Received null building detail.")
                    }
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<BuildingDetailItem>>, t: Throwable) {
                Log.e("FetchDataViewModel", "API call failed: ${t.message}")
            }
        })
    }

    fun fetchRoomList(buildingId: Int, buildingFloor: Int) {
        service.searchBuildingFloor(buildingId, buildingFloor).enqueue(object : Callback<ApiResponse<RoomListResponse>> {
            override fun onResponse(call: Call<ApiResponse<RoomListResponse>>, response: Response<ApiResponse<RoomListResponse>>) {
                if (response.isSuccessful) {
                    _roomList.value = response.body()?.data?.roomList ?: emptyList()
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<RoomListResponse>>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })
    }

    fun searchFacilities(type: String) {
        service.searchFacilities(type).enqueue(object : Callback<ApiResponse<BuildingListResponse>> {
            override fun onResponse(call: Call<ApiResponse<BuildingListResponse>>, response: Response<ApiResponse<BuildingListResponse>>) {
                if (response.isSuccessful) {
                    _buildingList.value = response.body()?.data?.list ?: emptyList()
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<BuildingListResponse>>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })
    }

    fun getFacilities(buildingId: Int, type: String) {
        service.getFacilities(buildingId, type).enqueue(object : Callback<ApiResponse<FacilityListResponse>> {
            override fun onResponse(call: Call<ApiResponse<FacilityListResponse>>, response: Response<ApiResponse<FacilityListResponse>>) {
                if (response.isSuccessful) {
                    val facilities = response.body()?.data?.facilities ?: emptyMap()
                    val updatedFacilitiesMap = _facilityList.value.orEmpty().toMutableMap()
                    updatedFacilitiesMap[buildingId] = facilities.values.flatten()
                    _facilityList.value = updatedFacilitiesMap
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<FacilityListResponse>>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })
    }

    fun getRoutes(
        startType: String,
        startId: Int? = null,
        startLat: Double? = null,
        startLong: Double? = null,
        endType: String,
        endId: Int? = null,
        endLat: Double? = null,
        endLong: Double? = null,
        barrierFree: String? = null
    ) {
        service.getRoutes(startType, startId, startLat, startLong, endType, endId, endLat, endLong, barrierFree)
            .enqueue(object : Callback<ApiResponse<RouteResponse>> {
                override fun onResponse(call: Call<ApiResponse<RouteResponse>>, response: Response<ApiResponse<RouteResponse>>) {
                    if (response.isSuccessful) {
                        _routeResponse.value = response.body()?.data
                    } else {
                        Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<RouteResponse>>, t: Throwable) {
                    Log.e("FetchDataViewModel", "Failure: ${t.message}")
                }
            })
    }

    fun fetchPlaceInfo(roomId: Int, placeType: String, callback: (PlaceInfoResponse?) -> Unit) {
        service.getPlaceInfo(roomId, placeType)
            .enqueue(object : Callback<ApiResponse<PlaceInfoResponse>> {
                override fun onResponse(
                    call: Call<ApiResponse<PlaceInfoResponse>>,
                    response: Response<ApiResponse<PlaceInfoResponse>>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body()?.data)
                    } else {
                        Log.d("FetchDataViewModel", "API response unsuccessful. Code: ${response.code()}, Message: ${response.message()}")
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<ApiResponse<PlaceInfoResponse>>, t: Throwable) {
                    Log.d("FetchDataViewModel", "API call failed: ${t.message}")
                    callback(null)
                }
            })
    }

    // RouteResponse를 업데이트할 때 사용하는 메서드
    fun updateSplitedRoute(route: RouteResponse) {
        DirectionSearchRouteDataHolder.splitedRoute = route
    }

}
