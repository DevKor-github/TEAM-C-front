package com.devkor.kodaero

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat

class CustomButtonWithHint @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    private val hintTypeface: Typeface? = ResourcesCompat.getFont(context, R.font.pretendard_regular)
    private val textTypeface: Typeface? = ResourcesCompat.getFont(context, R.font.pretendard_semibold)

    init {
        // 초기 상태 설정
        if (text.isNullOrEmpty()) {
            typeface = hintTypeface
            setTextColor(ResourcesCompat.getColor(resources, R.color.gray, null))
        } else {
            typeface = textTypeface
            setTextColor(ResourcesCompat.getColor(resources, R.color.map_main, null))
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        updateTypeface(text)
    }

    private fun updateTypeface(text: CharSequence?) {
        if (text.isNullOrEmpty()) {
            typeface = hintTypeface
            setTextColor(ResourcesCompat.getColor(resources, R.color.gray, null))
        } else {
            typeface = textTypeface
            setTextColor(ResourcesCompat.getColor(resources, R.color.map_main, null))
        }
    }
}
