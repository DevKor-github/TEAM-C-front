package com.devkor.kodaero

object MarkerZoomLevelThreshold {
    // 줌 레벨에 대응하는 ID 리스트를 다양하게 배치합니다.
    val zoomToIdsMap: Map<Double, List<Int>> = mapOf(
        1.0 to listOf(48, 15, 2, 32, 21, 14, 13, 33, 44, 10, 24, 3, 18),  // 일부 ID만 포함
        15.0 to listOf(7, 12, 20, 1, 26, 35, 45, 46, 49, 50, 53, 55), // 더 많은 ID 포함
        16.0 to (1..61).toList() // 모든 ID를 포함
    )

    // ID에 해당하는 최소 줌 임계값을 가져오는 함수
    fun getThresholdForId(id: Int): Double {
        // ID가 포함된 최소 줌 레벨을 찾아 반환
        return zoomToIdsMap.entries
            .firstOrNull { it.value.contains(id) }
            ?.key ?: 0.0 // ID가 없으면 0 값을 반환
    }

    val availableBuildingList: List<Int> = listOf(
        1, 2, 3, 10, 12, 13, 14, 15, 17, 18, 20, 21, 25, 26, 29, 32, 33, 39, 48, 42, 44, 45, 46, 49, 50, 53, 55, 56
    )
}
