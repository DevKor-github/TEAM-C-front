package com.example.deckor_teamc_front

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchBuildingViewModel : ViewModel() {

    private val _buildingItems = MutableLiveData<List<BuildingItem>>()
    val buildingItems: LiveData<List<BuildingItem>> get() = _buildingItems

    init {
        fetchBuildingItems()
    }

    private fun fetchBuildingItems() {
        // 로컬 데이터 설정
        val localBuildingItems = listOf(
            BuildingItem(1, "애기능생활관", "서울 성북구 안암로 73-15", null, 0.0, 0.0, "BUILDING"),
            BuildingItem(2, "애기능생활관 학생식당", "서울 성북구 안암로 73-15", null, 0.0, 0.0, "FACILITY"),
            BuildingItem(3, "애기능생활관 302호", "서울 성북구 안암로 73-15", 3, 0.0, 0.0, "CLASSROOM"),
            BuildingItem(4, "애기능생활관 101호", "서울 성북구 안암로 73-15", 1, 0.0, 0.0, "CLASSROOM"),
            BuildingItem(5, "애기능생활관 102호", "서울 성북구 안암로 73-15", 1, 0.0, 0.0, "CLASSROOM"),
            BuildingItem(6, "애기능생활관 103호", "서울 성북구 안암로 73-15", 1, 0.0, 0.0, "CLASSROOM")
        )
        _buildingItems.value = localBuildingItems
    }
}
