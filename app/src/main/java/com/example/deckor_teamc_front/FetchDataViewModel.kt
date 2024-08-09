package com.example.deckor_teamc_front

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

class FetchDataViewModel : ViewModel() {
    private val _buildingSearchItems = MutableLiveData<List<BuildingSearchItem>>()
    val buildingSearchItems: LiveData<List<BuildingSearchItem>> get() = _buildingSearchItems

    private val _buildingDetailList = MutableLiveData<List<BuildingDetailItem>>()
    val buildingDetailList: LiveData<List<BuildingDetailItem>> get() = _buildingDetailList

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

    fun searchBuildings(keyword: String, buildingId: Int? = null) {
        val call = if (buildingId != null) {
            service.search(keyword, buildingId)
        } else {
            service.search(keyword)
        }

        call.enqueue(object : Callback<ApiResponse<List<BuildingSearchItem>>> {
            override fun onResponse(call: Call<ApiResponse<List<BuildingSearchItem>>>, response: Response<ApiResponse<List<BuildingSearchItem>>>) {
                if (response.isSuccessful) {
                    _buildingSearchItems.value = response.body()?.data ?: emptyList()
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<BuildingSearchItem>>>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })
    }

    fun fetchBuildingList() {
        Log.d("FetchBuildingList","Done")
        service.getAllBuildings().enqueue(object : Callback<ApiResponse<BuildingListResponse>> {
            override fun onResponse(call: Call<ApiResponse<BuildingListResponse>>, response: Response<ApiResponse<BuildingListResponse>>) {
                if (response.isSuccessful) {
                    // 응답 성공 시, raw JSON 로그 출력
                    val rawJson = response.body()?.let {
                        Gson().toJson(it)
                    }
                    Log.d("FetchBuildingList", "Raw JSON response: $rawJson")

                    // 데이터가 정상적으로 파싱되었는지 로그 확인
                    val buildingList = response.body()?.data?.list ?: emptyList()
                    Log.d("FetchBuildingList", "Parsed Building List: $buildingList")

                    _buildingList.value = buildingList
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
        service.searchFacilities(type).enqueue(object : Callback<ApiResponse<BuildingDetailListResponse>> {
            override fun onResponse(call: Call<ApiResponse<BuildingDetailListResponse>>, response: Response<ApiResponse<BuildingDetailListResponse>>) {
                if (response.isSuccessful) {
                    _buildingDetailList.value = response.body()?.data?.list ?: emptyList()
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<BuildingDetailListResponse>>, t: Throwable) {
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
}
