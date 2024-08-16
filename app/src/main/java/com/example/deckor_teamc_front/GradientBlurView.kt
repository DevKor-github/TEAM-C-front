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
    private val itemHeight = context.resources.getDimensionPixelSize(R.dimen.item_height)
    private val blurHeight = dpToPx(itemHeight) / 3 // 블러 효과를 적용할 높이
    private val blurRadius = 1f // 블러 반경 설정, 적절한 반경으로 설정

    var hasTopBlur = false
    var hasBottomBlur = false

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
        // 위쪽에 블러 적용: 가장 위가 블러가 가장 강하고, 아래로 갈수록 약해짐
        val shader = LinearGradient(
            0f, 0f, 0f, blurHeight,
            Color.WHITE, Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        paint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        canvas.drawRect(0f, 0f, width.toFloat(), blurHeight, paint)
    }

    private fun drawBottomBlur(canvas: Canvas) {
        // 아래쪽에 블러 적용: 가장 아래가 블러가 가장 강하고, 위로 갈수록 약해짐
        val shader = LinearGradient(
            0f, height - blurHeight, 0f, height.toFloat(),
            Color.TRANSPARENT, Color.WHITE,
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        paint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        canvas.drawRect(0f, height - blurHeight, width.toFloat(), height.toFloat(), paint)
    }

    // dp를 픽셀로 변환하는 유틸리티 함수
    private fun dpToPx(dp: Int): Float {
        return dp * context.resources.displayMetrics.density
    }
}
