package com.example.deckor_teamc_front

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchBuildingViewModel : ViewModel() {
    private val _buildingItems = MutableLiveData<List<BuildingItem>>()
    val buildingItems: LiveData<List<BuildingItem>> get() = _buildingItems

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://3.34.68.172:8080/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(ApiService::class.java)

    fun searchBuildings(keyword: String, buildingId: Int? = null) {
        val call = if (buildingId != null) {
            service.search(keyword, buildingId)
        } else {
            service.search(keyword)
        }

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    _buildingItems.value = response.body()?.data ?: emptyList()
                } else {
                    Log.e("SearchBuildingViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("SearchBuildingViewModel", "Failure: ${t.message}")
            }
        })
    }
}
