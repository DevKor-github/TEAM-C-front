package com.example.deckor_teamc_front

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FetchDataViewModel : ViewModel() {
    private val _buildingItems = MutableLiveData<List<BuildingItem>>()
    val buildingItems: LiveData<List<BuildingItem>> get() = _buildingItems

    private val _buildingList = MutableLiveData<List<BuildingItem>>()
    val buildingList: LiveData<List<BuildingItem>> get() = _buildingList

    private val _buildingDetailList = MutableLiveData<List<BuildingDetailItem>>()
    val buildingDetailList: LiveData<List<BuildingDetailItem>> get() = _buildingDetailList

    private val _facilityList = MutableLiveData<Map<String, List<FacilityItem>>>()
    val facilityList: LiveData<Map<String, List<FacilityItem>>> get() = _facilityList

    private val _roomList = MutableLiveData<List<RoomList>>()
    val roomList: LiveData<List<RoomList>> get() = _roomList

    private val service = RetrofitClient.instance

    // 임시 데이터
    init {
        // 임시 데이터 설정
        loadDummyData()
    }

    private fun loadDummyData() {
        val dummyBuildings = listOf(
            BuildingItem(1, "애기능생활관", "서울 성북구 안암로 73-15", 127.0274333, 37.5843837, 7, "Building"),
            BuildingItem(2, "우당교양관", "서울 성북구 고려대로 104 105", 127.0313414, 37.586868, 4, "Building")
        )
        _buildingList.value = dummyBuildings
    }

    fun fetchBuildingList() {
        service.getAllBuildings().enqueue(object : Callback<ApiResponse<BuildingListResponse>> {
            override fun onResponse(call: Call<ApiResponse<BuildingListResponse>>, response: Response<ApiResponse<BuildingListResponse>>) {
                if (response.isSuccessful) {
                    _buildingList.value = response.body()?.data?.buildingList ?: emptyList()
                    _buildingList.value?.forEach { building ->
                        Log.d("FetchDataViewModel", "Building: $building")
                    }
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<BuildingListResponse>>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })
    }

    fun searchBuildings(keyword: String, buildingId: Int? = null) {
        val call = if (buildingId != null) {
            service.search(keyword, buildingId)
        } else {
            service.search(keyword)
        }

        call.enqueue(object : Callback<ApiResponse<List<BuildingItem>>> {
            override fun onResponse(call: Call<ApiResponse<List<BuildingItem>>>, response: Response<ApiResponse<List<BuildingItem>>>) {
                if (response.isSuccessful) {
                    _buildingItems.value = response.body()?.data ?: emptyList()
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<BuildingItem>>>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })
    }

    fun fetchRoomList(buildingId: Int) {
        service.searchBuildingFloor(buildingId).enqueue(object : Callback<ApiResponse<RoomListResponse>> {
            override fun onResponse(call: Call<ApiResponse<RoomListResponse>>, response: Response<ApiResponse<RoomListResponse>>) {
                if (response.isSuccessful) {
                    _roomList.value = response.body()?.data?.roomList ?: emptyList()
                    // 로그 출력
                    _roomList.value?.forEach { room ->
                        Log.d("com.example.deckor_teamc_front.FetchDataViewModel", "Room: $room")
                    }
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
                    _buildingDetailList.value = response.body()?.data?.buildingList ?: emptyList()
                    _buildingDetailList.value?.forEach { building ->
                        Log.d("FetchDataViewModel", "Building: $building")
                    }
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
                    _facilityList.value = response.body()?.data?.facilities ?: emptyMap()
                    _facilityList.value?.forEach { (floor, facilities) ->
                        facilities.forEach { facility ->
                            Log.d("FetchDataViewModel", "Facility: $facility on floor $floor")
                        }
                    }
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<FacilityListResponse>>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })
    }
}
