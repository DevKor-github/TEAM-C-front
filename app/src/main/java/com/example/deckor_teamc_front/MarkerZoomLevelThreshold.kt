package com.example.deckor_teamc_front

object MarkerZoomLevelThreshold {
    // 줌 레벨에 대응하는 ID 리스트를 다양하게 배치합니다.
    val zoomToIdsMap: Map<Double, List<Int>> = mapOf(
        14.0 to listOf(1, 8, 15, 22, 29, 36, 43, 50, 32),  // 일부 ID만 포함
        15.0 to listOf(2, 9, 16, 23, 30, 37, 44, 51, 3, 10, 17, 24, 31, 38, 45, 52, 4, 11, 18, 25, 32, 39, 46, 53), // 더 많은 ID 포함
        16.0 to (1..61).toList() // 모든 ID를 포함
    )

    // ID에 해당하는 최소 줌 임계값을 가져오는 함수
    fun getThresholdForId(id: Int): Double {
        // ID가 포함된 최소 줌 레벨을 찾아 반환
        return zoomToIdsMap.entries
            .firstOrNull { it.value.contains(id) }
            ?.key ?: 0.0 // ID가 없으면 0 값을 반환
    }
}
