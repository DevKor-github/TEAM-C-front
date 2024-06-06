package com.example.deckor_teamc_front

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible

class CustomScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    private val container: LinearLayout
    private var selectedLayout: LinearLayout? = null
    private var selectedFloor: Int = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.view_custom_scroll, this, true)
        container = findViewById(R.id.container)
    }

    fun setMaxNumber(maxNumber: Int) {
        container.removeAllViews()
        if (maxNumber <= 1) {
            this.isVisible = false
            return
        } else {
            this.isVisible = true
        }

        for (i in maxNumber downTo 1) {  // 역순으로 추가
            val itemLayout = LayoutInflater.from(context).inflate(R.layout.custom_scroll_item_layout, container, false) as LinearLayout
            val textView = itemLayout.findViewById<TextView>(R.id.item_text)
            textView.text = i.toString()

            // 폰트 설정
            textView.setTypeface(ResourcesCompat.getFont(context, R.font.pretendard_regular))

            // 텍스트 색상 설정
            textView.setTextColor(Color.parseColor("#424242"))

            // 클릭 리스너 설정
            itemLayout.setOnClickListener {
                changeLayoutStyle(itemLayout, textView)
            }

            container.addView(itemLayout)
        }

        // 레이아웃 크기 조정
        adjustLayoutSize(maxNumber)

        // 마지막 항목으로 스크롤
        post {
            fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun adjustLayoutSize(maxNumber: Int) {
        val itemHeight = context.resources.getDimensionPixelSize(R.dimen.item_height)
        val maxVisibleItems = 5
        val params = this.layoutParams
        params.height = if (maxNumber <= maxVisibleItems) {
            itemHeight * maxNumber
        } else {
            itemHeight * maxVisibleItems
        }
        this.layoutParams = params
    }

    private fun changeLayoutStyle(layout: LinearLayout, textView: TextView) {
        // 이전에 선택된 레이아웃이 있다면 원래 상태로 복구
        selectedLayout?.let {
            val previousTextView = it.findViewById<TextView>(R.id.item_text)
            it.setBackgroundColor(Color.TRANSPARENT) // 원래 배경색으로 복구
            previousTextView.setTypeface(ResourcesCompat.getFont(context, R.font.pretendard_regular)) // 원래 글씨체로 복구
        }

        // 현재 선택된 레이아웃 스타일 변경
        layout.setBackgroundColor(Color.parseColor("#FFD8D8")) // 레이아웃의 배경색 변경
        textView.setTypeface(ResourcesCompat.getFont(context, R.font.pretendard_bold)) // 글씨를 볼드 처리

        // 현재 선택된 레이아웃을 추적
        selectedLayout = layout

        // 클릭된 텍스트 로그로 반환
        Log.d("CustomTextView", "Clicked text: ${textView.text}")
        selectedFloor = textView.text.toString().toInt()
    }
}
