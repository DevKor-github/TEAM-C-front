package com.devkor.kodaero

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat

class CustomEditTextWithHint @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private val hintTypeface: Typeface? = ResourcesCompat.getFont(context, R.font.pretendard_regular)
    private val textTypeface: Typeface? = ResourcesCompat.getFont(context, R.font.pretendard_semibold)

    init {
        // 초기 폰트 설정
        updateTypeface(text)

        // TextWatcher 추가
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // 텍스트가 변경될 때마다 폰트 업데이트
                updateTypeface(s)
            }
        })

        // Focusable, Clickable, Enabled 속성 확인 및 설정
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        isEnabled = true
    }

    private fun updateTypeface(text: CharSequence?) {
        typeface = if (text.isNullOrEmpty()) {
            hintTypeface
        } else {
            textTypeface
        }
    }
}
