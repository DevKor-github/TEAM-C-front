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


    private val _buildingList = MutableLiveData<List<BuildingItem>>()
    val buildingList: LiveData<List<BuildingItem>> get() = _buildingList

    private val _roomList = MutableLiveData<List<RoomList>>()
    val roomList: LiveData<List<RoomList>> get() = _roomList

    private val service = RetrofitClient.instance


    //임시데이터
    init {
        // 임시 데이터 설정
        loadDummyData()
    }

    private fun loadDummyData() {
        val dummyBuildings = listOf(
            BuildingItem(1, "애기능생활관", "서울 성북구 안암로 73-15", 127.0274333,37.5843837, 7, "Building"),
            BuildingItem(2, "우당교양관", "서울 성북구 고려대로 104 105", 127.0313414,37.586868, 4, "Building"),
        )
        _buildingList.value = dummyBuildings
    }
    //Init 부터 여기 까지 임시 데이터

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
                    Log.e("com.example.deckor_teamc_front.FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<RoomListResponse>>, t: Throwable) {
                Log.e("com.example.deckor_teamc_front.FetchDataViewModel", "Failure: ${t.message}")
            }
        })
    }
}
