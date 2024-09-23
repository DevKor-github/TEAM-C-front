package com.devkor.kodaero

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
class RouteView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val path = Path()

    // 네온 효과를 위한 Paint
    private val neonPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND  // 선 끝을 둥글게 설정
        strokeJoin = Paint.Join.ROUND  // 선이 만나는 부분을 둥글게 설정
        setShadowLayer(6f, 0f, 0f, Color.parseColor("#FF8A80"))
    }

    // 경로를 그릴 기본 Paint
    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND  // 선 끝을 둥글게 설정
        strokeJoin = Paint.Join.ROUND  // 선이 만나는 부분을 둥글게 설정
    }

    // 출발지와 도착지 원을 그릴 Paint
    private val circlePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // 네온 효과용 투명 원 Paint
    private val neonCirclePaint = Paint().apply {
        color = Color.parseColor("#FF8A80")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var coordinates: List<Pair<Float, Float>> = emptyList()

    fun setRouteCoordinates(coordinates: List<Pair<Float, Float>>) {
        this.coordinates = coordinates
        invalidate() // 뷰를 다시 그리도록 요청
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (coordinates.isEmpty()) return

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val originalSize = 1680f
        val scaleFactor = minOf(viewWidth / originalSize, viewHeight / originalSize)
        val offsetX = (viewWidth - originalSize * scaleFactor) / 2
        val offsetY = (viewHeight - originalSize * scaleFactor) / 2

        path.rewind()

        val firstPoint = coordinates.first()
        path.moveTo(
            offsetX + firstPoint.first * scaleFactor,
            offsetY + firstPoint.second * scaleFactor
        )

        for (point in coordinates.drop(1)) {
            path.lineTo(
                offsetX + point.first * scaleFactor,
                offsetY + point.second * scaleFactor
            )
        }

        // 네온 효과를 위한 경로를 먼저 그림
        canvas.drawPath(path, neonPaint)

        // 경로를 기본 Paint로 다시 그림
        canvas.drawPath(path, paint)

        // 출발지 원 그리기 (네온 효과 원 + 강조 원)
        drawCircleWithNeonEffect(canvas, offsetX + firstPoint.first * scaleFactor, offsetY + firstPoint.second * scaleFactor)

        // 도착지 원 그리기 (네온 효과 원 + 강조 원)
        val lastPoint = coordinates.last()
        drawCircleWithNeonEffect(canvas, offsetX + lastPoint.first * scaleFactor, offsetY + lastPoint.second * scaleFactor)
    }

    private fun drawCircleWithNeonEffect(canvas: Canvas, cx: Float, cy: Float) {
        canvas.drawCircle(cx, cy, 10f, neonCirclePaint)
        canvas.drawCircle(cx, cy, 6f, circlePaint)
    }
}
