package com.example.deckor_teamc_front

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View

class RouteView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val path = Path()
    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val scalePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
        textSize = 30f
    }

    private var coordinates: List<Pair<Float, Float>> = emptyList()

    fun setRouteCoordinates(coordinates: List<Pair<Float, Float>>) {
        // y축에 300을 더한 새로운 좌표 리스트를 생성
        this.coordinates = coordinates.map { Pair(it.first, it.second + 370f) }
        invalidate() // 뷰를 다시 그리도록 요청
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (coordinates.isEmpty()) return

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        // 가로는 1680으로 고정
        val scaleFactorX = viewWidth / 1680f

        path.rewind()

        val firstPoint = coordinates.first()
        path.moveTo(
            firstPoint.first * scaleFactorX,
            firstPoint.second * scaleFactorX // 이미 y축에 300이 더해져 있음
        )
        Log.d("com.example.deckor_teamc_front.RouteView", "Point: (${firstPoint.first * scaleFactorX}, ${firstPoint.second * scaleFactorX})")

        for (point in coordinates.drop(1)) {
            path.lineTo(
                point.first * scaleFactorX,
                point.second * scaleFactorX // 이미 y축에 300이 더해져 있음
            )
            Log.d("com.example.deckor_teamc_front.RouteView", "Point: (${point.first * scaleFactorX}, ${point.second * scaleFactorX})")
        }

        // 경로를 그리기 전에 축적을 100 단위로 표시
        drawScale(canvas, scaleFactorX, viewWidth, viewHeight)

        // 경로를 Canvas에 그리기
        canvas.drawPath(path, paint)
    }

    private fun drawScale(canvas: Canvas, scaleFactorX: Float, viewWidth: Float, viewHeight: Float) {
        val scaleInterval = 100f * scaleFactorX

        var x = 0f
        while (x <= viewWidth) {
            canvas.drawLine(x, viewHeight - 50f, x, viewHeight - 20f, scalePaint)
            canvas.drawText("${(x / scaleFactorX).toInt()}m", x, viewHeight - 60f, scalePaint)
            x += scaleInterval
        }

        var y = 0f
        while (y <= viewHeight) {
            canvas.drawLine(50f, y, 20f, y, scalePaint)
            canvas.drawText("${(y / scaleFactorX).toInt()}m", 60f, y + 10f, scalePaint)
            y += scaleInterval
        }
    }
}
