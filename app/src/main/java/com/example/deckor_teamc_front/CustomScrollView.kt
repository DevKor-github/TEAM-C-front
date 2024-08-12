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

    // 콜백 인터페이스 정의
    interface OnFloorSelectedListener {
        fun onFloorSelected(floor: Int)
    }

    private var onFloorSelectedListener: OnFloorSelectedListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_custom_scroll, this, true)
        container = findViewById(R.id.container)
    }

    fun setOnFloorSelectedListener(listener: OnFloorSelectedListener) {
        onFloorSelectedListener = listener
    }

    fun setFloors(minNumber: Int, maxNumber: Int, floor: Int) {
        container.removeAllViews()
        if (minNumber >= 0 && maxNumber <= 1) {
            this.isVisible = false
            return
        } else {
            this.isVisible = true
        }

        for (i in maxNumber downTo minNumber) {
            if (i == 0) continue // 0층은 무시
            val itemLayout = LayoutInflater.from(context).inflate(R.layout.custom_scroll_item_layout, container, false) as LinearLayout
            val textView = itemLayout.findViewById<TextView>(R.id.item_text)
            textView.text = if (i < 0) "B${-i}" else i.toString()

            // 폰트 설정
            textView.setTypeface(ResourcesCompat.getFont(context, R.font.pretendard_regular))

            // 텍스트 색상 설정
            textView.setTextColor(Color.parseColor("#424242"))

            // 클릭 리스너 설정
            itemLayout.setOnClickListener {
                changeLayoutStyle(itemLayout, textView)
                // 콜백 호출
                onFloorSelectedListener?.onFloorSelected(i)
            }

            container.addView(itemLayout)
        }

        // 레이아웃 크기 조정
        adjustLayoutSize(maxNumber - minNumber)

        // 입력된 층을 기본 선택 및 스크롤 위치 설정
        post {
            val itemHeight = context.resources.getDimensionPixelSize(R.dimen.item_height)
            val modifiedFloor = if (floor > 0) floor else floor + 1 // Floor에 맞게 스크롤의 초기 위치와 색 변환을 수행하기 위한 로직

            scrollTo(0, (((maxNumber - 5) - (modifiedFloor - 1)) * itemHeight))
            container.getChildAt((maxNumber - 1) - (modifiedFloor -1))?.let {
                if (it is LinearLayout) {
                    val textView = it.findViewById<TextView>(R.id.item_text)
                    changeLayoutStyle(it, textView)
                    onFloorSelectedListener?.onFloorSelected(floor)
                }
            }
        }
    }

    private fun adjustLayoutSize(floorCount: Int) {
        val itemHeight = context.resources.getDimensionPixelSize(R.dimen.item_height)
        val maxVisibleItems = 5
        val params = this.layoutParams
        params.height = if (floorCount <= maxVisibleItems) {
            itemHeight * floorCount
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
    }
}
