package com.ku.kodaero

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryViewModel : ViewModel() {

    // ApiService를 내부에서 초기화
    private val apiService: ApiService = RetrofitClient.instance

    // MutableLiveData를 사용하여 데이터를 관리
    private val _categories = MutableLiveData<List<CategoryItem>>()
    val categories: LiveData<List<CategoryItem>> get() = _categories

    // API 호출을 통해 카테고리 데이터를 가져오는 메서드
    fun fetchCategories(buildingId: Int) {
        Log.d("com.devkor.kodaero.CategoryViewModel", "Fetching categories for buildingId: $buildingId")

        apiService.getCategories(type = "BUILDING", id = buildingId).enqueue(object : Callback<ApiResponse<CategoryResponse>> {
            override fun onResponse(call: Call<ApiResponse<CategoryResponse>>, response: Response<ApiResponse<CategoryResponse>>) {
                if (response.isSuccessful) {
                    val categoryResponse = response.body()
                    if (categoryResponse != null) {
                        _categories.value = categoryResponse.data.categoryList ?: emptyList()
                        Log.d("com.devkor.kodaero.CategoryViewModel", "Categories loaded: ${categoryResponse.data.categoryList}")
                    } else {
                        Log.e("com.devkor.kodaero.CategoryViewModel", "Response body is null")
                    }
                } else {
                    Log.e("com.devkor.kodaero.CategoryViewModel", "Response failed with status code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<CategoryResponse>>, t: Throwable) {
                Log.e("com.devkor.kodaero.CategoryViewModel", "Network request failed", t)
            }
        })
    }


    // API 호출을 통해 카테고리 데이터를 가져오는 메서드
    fun fetchCategoriesPlace(buildingId: Int) {
        Log.d("com.devkor.kodaero.CategoryViewModel", "Fetching categories for buildingId: $buildingId")

        apiService.getCategories(type = "PLACE", id = buildingId).enqueue(object : Callback<ApiResponse<CategoryResponse>> {
            override fun onResponse(call: Call<ApiResponse<CategoryResponse>>, response: Response<ApiResponse<CategoryResponse>>) {
                if (response.isSuccessful) {
                    val categoryResponse = response.body()
                    if (categoryResponse != null) {
                        _categories.value = categoryResponse.data.categoryList ?: emptyList()
                        Log.d("com.devkor.kodaero.CategoryViewModel", "Categories loaded: ${categoryResponse.data.categoryList}")
                    } else {
                        Log.e("com.devkor.kodaero.CategoryViewModel", "Response body is null")
                    }
                } else {
                    Log.e("com.devkor.kodaero.CategoryViewModel", "Response failed with status code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<CategoryResponse>>, t: Throwable) {
                Log.e("com.devkor.kodaero.CategoryViewModel", "Network request failed", t)
            }
        })
    }

    suspend fun fetchCategoriesBuildingSync(buildingId: Int): List<CategoryItem>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCategories("BUILDING", buildingId).execute()
                if (response.isSuccessful) {
                    response.body()?.data?.categoryList
                } else {
                    Log.e("CategoryViewModel", "Failed to fetch categories: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error fetching categories: ${e.message}")
                null
            }
        }
    }


    suspend fun fetchCategoriesPlaceSync(placeId: Int): List<CategoryItem>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCategories("PLACE", placeId).execute()
                if (response.isSuccessful) {
                    response.body()?.data?.categoryList
                } else {
                    Log.e("CategoryViewModel", "Failed to fetch categories: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error fetching categories: ${e.message}")
                null
            }
        }
    }

}
