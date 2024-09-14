package com.devkor.kodaero

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kakao.sdk.user.model.User
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

    private val _individualFacilityList = MutableLiveData<List<FacilityItem>>()
    val individualFacilityList: LiveData<List<FacilityItem>> get() = _individualFacilityList

    private val _buildingList = MutableLiveData<List<BuildingItem>>()
    val buildingList: LiveData<List<BuildingItem>> get() = _buildingList

    private val _buildingSearchList = MutableLiveData<List<BuildingSearchItem>>()
    val buildingSearchList: LiveData<List<BuildingSearchItem>> get() = _buildingSearchList

    private val _roomList = MutableLiveData<List<RoomList>>()
    val roomList: LiveData<List<RoomList>> get() = _roomList

    private val _routeResponse = MutableLiveData<RouteResponse?>()
    val routeResponse: MutableLiveData<RouteResponse?> get() = _routeResponse

    private val _placeInfoResponse = MutableLiveData<PlaceInfoResponse?>()
    val placeInfoResponse: LiveData<PlaceInfoResponse?> get() = _placeInfoResponse

    private val _userTokens = MutableLiveData<UserTokens?>()
    val userTokens: LiveData<UserTokens?> get() = _userTokens

    private val _userInfo = MutableLiveData<UserInfo?>()
    val userInfo: LiveData<UserInfo?> get() = _userInfo

    var responseCode: Int? = null

    private val service = RetrofitClient.instance

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

    fun fetchBuildingList(placeType: String) {
        Log.d("FetchBuildingList","Done")
        service.getAllBuildings(placeType).enqueue(object : Callback<ApiResponse<BuildingListResponse>> {
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

    fun searchFacilities(placeType: String) {
        service.searchFacilities(placeType).enqueue(object : Callback<ApiResponse<IndividualFacilityListResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<IndividualFacilityListResponse>>,
                response: Response<ApiResponse<IndividualFacilityListResponse>>
            ) {
                if (response.isSuccessful) {
                    val facilities = response.body()?.data?.facilities ?: emptyList()
                    _individualFacilityList.value = facilities
                    Log.d("FetchDataViewModel", "Fetched ${facilities.size} facilities of type $placeType")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FetchDataViewModel", "Error fetching facilities: $errorBody")
                }
            }

            override fun onFailure(call: Call<ApiResponse<IndividualFacilityListResponse>>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure fetching facilities: ${t.message}")
            }
        })
    }


    fun getFacilities(buildingId: Int, placeType: String) {
        service.getFacilities(buildingId, placeType).enqueue(object : Callback<ApiResponse<FacilityListResponse>> {
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
        barrierFree: Boolean? = null
    ) {
        service.getRoutes(startType, startId, startLat, startLong, endType, endId, endLat, endLong)
            .enqueue(object : Callback<ApiResponse<List<RouteResponse>>> {
                override fun onResponse(call: Call<ApiResponse<List<RouteResponse>>>, response: Response<ApiResponse<List<RouteResponse>>>) {
                    if (response.isSuccessful) {
                        val routeResponses = response.body()?.data

                        if (!routeResponses.isNullOrEmpty()) {
                            // barrierFree 값에 따라 첫 번째 또는 두 번째 RouteResponse를 선택
                            val selectedRouteResponse = if (barrierFree == null || barrierFree == false) {
                                routeResponses[0]  // barrierFree가 false이면 첫 번째 원소 선택
                            } else {
                                routeResponses[1]  // barrierFree가 true
                            }

                            // 선택된 RouteResponse를 LiveData에 설정
                            _routeResponse.value = selectedRouteResponse
                        } else {
                            Log.e("FetchDataViewModel", "No routes found in response")
                        }
                    } else {
                        Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<RouteResponse>>>, t: Throwable) {
                    Log.e("FetchDataViewModel", "Failure: ${t.message}")
                }
            })
    }

    fun fetchPlaceInfo(roomId: Int, callback: (PlaceInfoResponse?) -> Unit) {
        service.getPlaceInfo(roomId)
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

    fun getUserTokens(provider: String, email: String, token: String) {
        val loginRequest = LoginRequest(provider, email, token)
        val service = RetrofitClient.instance
        service.getUserTokens(loginRequest).enqueue(object : Callback<ApiResponse<UserTokens>> {
            override fun onResponse(call: Call<ApiResponse<UserTokens>>, response: Response<ApiResponse<UserTokens>>) {
                if (response.isSuccessful) {
                    _userTokens.value = response.body()?.data
                } else {
                    _userTokens.value = null
                    Log.e("FetchDataViewModel", "Token fetch failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<UserTokens>>, t: Throwable) {
                _userTokens.value = null
                Log.e("FetchDataViewModel", "API call failed: ${t.message}")
            }
        })
    }

    fun fetchUserInfo() {
        val accessToken = TokenManager.getAccessToken()
        val refreshToken = TokenManager.getRefreshToken()

        if (accessToken == null || refreshToken == null) {
            Log.e("FetchDataViewModel", "No tokens available.")
            return
        }

        service.getUserInfo(accessToken, refreshToken).enqueue(object : Callback<ApiResponse<UserInfo>> {
            override fun onResponse(call: Call<ApiResponse<UserInfo>>, response: Response<ApiResponse<UserInfo>>) {
                responseCode = response.code()

                if (response.isSuccessful) {
                    val newAccessToken = response.headers()["AccessToken"]

                    if (!newAccessToken.isNullOrEmpty()) {
                        Log.d("FetchDataViewModel", "New access token received: $newAccessToken")
                        TokenManager.saveTokens(newAccessToken, refreshToken)
                    }

                    _userInfo.value = response.body()?.data
                    Log.d("FetchDataViewModel", "User info: ${response.body()?.data}")
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<UserInfo>>, t: Throwable) {
                Log.e("FetchDataViewModel", "API call failed: ${t.message}")
            }
        })
    }

    fun submitSuggestion(title: String, type: String, content: String) {
        val accessToken = TokenManager.getAccessToken()
        val refreshToken = TokenManager.getRefreshToken()

        if (accessToken == null || refreshToken == null) {
            Log.e("FetchDataViewModel", "No tokens available.")
            return
        }

        val suggestionRequest = SuggestionRequest(title, type, content)

        service.summitSuggestion(accessToken, refreshToken, suggestionRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                responseCode = response.code()

                if (response.isSuccessful) {
                    val newAccessToken = response.headers()["AccessToken"]

                    if (!newAccessToken.isNullOrEmpty()) {
                        Log.d("FetchDataViewModel", "New access token received: $newAccessToken")
                        TokenManager.saveTokens(newAccessToken, refreshToken)
                    }
                    Log.d("FetchDataViewModel", "Suggestion submitted successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FetchDataViewModel", "Error submitting suggestion: $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure submitting suggestion: ${t.message}")
            }
        })
    }

}
