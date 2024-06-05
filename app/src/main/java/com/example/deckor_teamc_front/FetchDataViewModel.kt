package com.example.deckor_teamc_front

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FetchDataViewModel : ViewModel() {
    private val _roomList = MutableLiveData<List<RoomList>>()
    val roomList: LiveData<List<RoomList>> get() = _roomList

    private val service = RetrofitClient.instance

    fun fetchRoomList(buildingId: Int) {
        service.searchBuildingFloor(buildingId).enqueue(object : Callback<ApiResponse<RoomListResponse>> {
            override fun onResponse(call: Call<ApiResponse<RoomListResponse>>, response: Response<ApiResponse<RoomListResponse>>) {
                if (response.isSuccessful) {
                    _roomList.value = response.body()?.data?.roomList ?: emptyList()
                    // 로그 출력
                    _roomList.value?.forEach { room ->
                        Log.d("com.example.deckor_teamc_front.InnerMapViewModel", "Room: $room")
                    }
                } else {
                    Log.e("com.example.deckor_teamc_front.InnerMapViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<RoomListResponse>>, t: Throwable) {
                Log.e("com.example.deckor_teamc_front.InnerMapViewModel", "Failure: ${t.message}")
            }
        })
    }
}
