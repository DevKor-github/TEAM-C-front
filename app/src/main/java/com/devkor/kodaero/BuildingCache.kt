package com.devkor.kodaero

object BuildingCache {
    private val cache = mutableMapOf<Int, BuildingItem>()

    // Building 객체를 캐시에 추가하거나 업데이트
    fun put(buildingId: Int, building: BuildingItem) {
        cache[buildingId] = building
    }

    // Building 객체를 캐시에서 가져옴
    fun get(buildingId: Int): BuildingItem? {
        return cache[buildingId]
    }

    // 캐시를 비우는 함수 (필요에 따라 사용)
    fun clear() {
        cache.clear()
    }
}
