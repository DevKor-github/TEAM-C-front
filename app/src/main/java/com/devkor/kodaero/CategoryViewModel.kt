package com.devkor.kodaero

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    // 카테고리 추가 메서드
    fun addCategoryToServer(accessToken: String, category: String, color: String, memo: String) {
        val newCategory = CategoryItemRequest(category, color, memo)

        apiService.addCategory(accessToken, newCategory).enqueue(object : Callback<ApiResponse<Any>> {
            override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                if (response.isSuccessful) {
                    // 성공적으로 추가된 경우 처리
                    Log.d("CategoryViewModel", "Category added successfully")
                } else {
                    Log.e("CategoryViewModel", "Failed to add category: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                Log.e("CategoryViewModel", "Failed to add category", t)
            }
        })
    }
}
