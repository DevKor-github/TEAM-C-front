package com.devkor.kodaero

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookmarkManager(private val context: Context, private val apiService: ApiService) {

    fun addBookmarks(selectedCategories: List<CategoryItem>, locationType: String, locationId: Int, memo: String) {
        val categoryIdList = selectedCategories.map { it.categoryId }
        val requestBody = BookmarkRequest(categoryIdList, locationType, locationId, memo)

        apiService.addBookmarks(requestBody).enqueue(object : Callback<ApiResponse<Any>> {
            override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                if (response.isSuccessful) {
                    if (categoryIdList.isEmpty()) {
                        Toast.makeText(context, "북마크 저장이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "북마크가 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "북마크 추가에 실패했습니다: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("BookmarkManager", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                Toast.makeText(context, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("BookmarkManager", "Network failure: ${t.message}", t)
            }
        })
    }


    fun fetchBookmarks(categoryId: Int, callback: (List<Bookmark>?) -> Unit) {
        apiService.getBookmarks(categoryId).enqueue(object : Callback<ApiResponse<BookmarkResponse>> {
            override fun onResponse(call: Call<ApiResponse<BookmarkResponse>>, response: Response<ApiResponse<BookmarkResponse>>) {
                if (response.isSuccessful) {
                    val bookmarkResponse = response.body()?.data?.bookmarkList
                    callback(bookmarkResponse)
                } else {
                    Log.e("FetchDataViewModel", "Failed to fetch bookmarks: ${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<ApiResponse<BookmarkResponse>>, t: Throwable) {
                Log.e("FetchDataViewModel", "Network error: ${t.message}", t)
                callback(null)
            }
        })
    }

    fun deleteBookmark(bookmarkId: Int) {
        val apiService = RetrofitClient.instance

        apiService.deleteBookmark(bookmarkId).enqueue(object : Callback<ApiResponse<Any>> {
            override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "북마크가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "북마크 삭제에 실패했습니다: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                Toast.makeText(context, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // 카테고리 삭제 API 호출
    fun deleteCategory(categoryId: Int) {
        val apiService = RetrofitClient.instance

        apiService.deleteCategory(categoryId).enqueue(object : Callback<ApiResponse<Any>> {
            override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "카테고리가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    // 필요하다면 UI 갱신 로직 추가
                } else {
                    Toast.makeText(context, "카테고리 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                Toast.makeText(context, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}

data class BookmarkRequest(
    val categoryIdList: List<Int>,
    val locationType: String,
    val locationId: Int,
    val memo: String
)
