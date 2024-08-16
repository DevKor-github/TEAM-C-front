package com.example.deckor_teamc_front

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes

class GradientBlurView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private val blurHeight = context.resources.getDimensionPixelSize(R.dimen.item_height).toFloat()
    private val blurRadius = Float.MIN_VALUE

    var hasTopBlur = false
    var hasBottomBlur = false

    private var startIntensity = 200 // 최대 강도
    private var endIntensity = 0     // 투명
    private var maxPoint = 0.3f      // 블러가 최대가 되는 지점 (0.0 ~ 1.0)

    // White color variable
    private val whiteColor = Color.WHITE

    init {
        context.withStyledAttributes(attrs, R.styleable.GradientBlurView) {
            hasTopBlur = getBoolean(R.styleable.GradientBlurView_hasTopBlur, false)
            hasBottomBlur = getBoolean(R.styleable.GradientBlurView_hasBottomBlur, false)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (hasTopBlur) {
            drawTopBlur(canvas)
        }

        if (hasBottomBlur) {
            drawBottomBlur(canvas)
        }
    }

    private fun drawTopBlur(canvas: Canvas) {
        val colors = intArrayOf(
            Color.argb(startIntensity, Color.red(whiteColor), Color.green(whiteColor), Color.blue(whiteColor)), // 시작 강도 (최대)
            Color.argb(startIntensity, Color.red(whiteColor), Color.green(whiteColor), Color.blue(whiteColor)), // maxPoint까지 강도 유지
            Color.argb(endIntensity, Color.red(whiteColor), Color.green(whiteColor), Color.blue(whiteColor))    // maxPoint 이후로 투명해짐
        )

        val positions = floatArrayOf(
            0f,                   // 시작
            maxPoint,             // maxPoint
            1f                    // 끝
        )

        val shader = LinearGradient(
            0f, 0f, 0f, blurHeight,
            colors,
            positions,
            Shader.TileMode.CLAMP // CLAMP를 사용하여 끝부분에서 색상이 유지되도록 설정
        )
        paint.shader = shader
        paint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        canvas.drawRect(0f, 0f, width.toFloat(), blurHeight, paint)
    }

    private fun drawBottomBlur(canvas: Canvas) {
        val colors = intArrayOf(
            Color.argb(endIntensity, Color.red(whiteColor), Color.green(whiteColor), Color.blue(whiteColor)),   // maxPoint 이전에는 투명
            Color.argb(startIntensity, Color.red(whiteColor), Color.green(whiteColor), Color.blue(whiteColor)), // maxPoint 이후에는 강도 유지 (최대)
            Color.argb(startIntensity, Color.red(whiteColor), Color.green(whiteColor), Color.blue(whiteColor))  // 끝까지 강도 유지 (최대)
        )

        val positions = floatArrayOf(
            0f,                   // 시작
            1f - maxPoint,        // maxPoint에서 대칭이 되도록 설정
            1f                    // 끝
        )

        val shader = LinearGradient(
            0f, height - blurHeight, 0f, height.toFloat(),
            colors,
            positions,
            Shader.TileMode.CLAMP // CLAMP를 사용하여 끝부분에서 색상이 유지되도록 설정
        )
        paint.shader = shader
        paint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        canvas.drawRect(0f, height - blurHeight, width.toFloat(), height.toFloat(), paint)
    }
}
