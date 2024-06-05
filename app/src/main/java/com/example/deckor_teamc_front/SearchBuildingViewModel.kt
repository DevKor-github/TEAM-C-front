package com.example.deckor_teamc_front

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchBuildingViewModel : ViewModel() {
    private val _buildingItems = MutableLiveData<List<BuildingItem>>()
    val buildingItems: LiveData<List<BuildingItem>> get() = _buildingItems

    private val service = RetrofitClient.instance

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
                    Log.e("SearchBuildingViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<BuildingItem>>>, t: Throwable) {
                Log.e("SearchBuildingViewModel", "Failure: ${t.message}")
            }
        })
    }
}
