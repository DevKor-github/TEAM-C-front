package com.devkor.kodaero

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatEditText

class CenteredEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {

    init {
        // 기본 중앙 정렬 및 설정
        setGravity(Gravity.CENTER)
        setHintTextColor(Color.parseColor("#B0B0B0"))
        hint = "1~18자 이내 입력"
        background = null // 밑줄 제거

        // 텍스트 변경 시마다 중앙 정렬 강제
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                setSelection(s?.length ?: 0) // 커서를 텍스트 끝으로 이동
            }
        })
    }
}
