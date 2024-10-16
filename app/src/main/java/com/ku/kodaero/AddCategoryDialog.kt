package com.ku.kodaero

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddCategoryDialog(context: Context, private val categoryViewModel: CategoryViewModel) : Dialog(context) {

    private var onCategoryAddListener: ((String, String) -> Unit)? = null
    private var selectedColor: String = "red"
    private var selectedButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_category)


        // Dialog의 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val addButton = findViewById<Button>(R.id.add_button)
        val categoryInput = findViewById<EditText>(R.id.category_input)

        val touchArea = findViewById<LinearLayout>(R.id.touch_area)

        touchArea.setOnClickListener {
            categoryInput.hint = ""  // 힌트 제거
            categoryInput.requestFocus()
            // 클릭된 것처럼 처리하기
            categoryInput.performClick()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(categoryInput, InputMethodManager.SHOW_IMPLICIT)
        }


        categoryInput.gravity = Gravity.CENTER

        categoryInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                categoryInput.gravity = Gravity.CENTER
            }
        }

        categoryInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 0) {
                    categoryInput.gravity = Gravity.CENTER
                } else {
                    categoryInput.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                }
            }
        })


        // Initialize color buttons
        val colorButtons = mapOf(
            "red" to findViewById<ImageButton>(R.id.color_red),
            "orange" to findViewById<ImageButton>(R.id.color_orange),
            "yellow" to findViewById<ImageButton>(R.id.color_yellow),
            "green" to findViewById<ImageButton>(R.id.color_green),
            "blue" to findViewById<ImageButton>(R.id.color_blue),
            "purple" to findViewById<ImageButton>(R.id.color_purple),
            "pink" to findViewById<ImageButton>(R.id.color_pink)
        )

        // Set default selected button
        selectedButton = colorButtons[selectedColor]
        selectedButton?.setBackgroundResource(R.drawable.icon_circle_red_selected) // 기본 선택된 상태로 설정

        // Handle color button clicks
        for ((color, button) in colorButtons) {
            button.setOnClickListener {
                selectedButton?.setBackgroundResource(getUnselectedDrawable(selectedColor)) // 이전 선택된 버튼 초기화
                selectedColor = color
                selectedButton = button
                button.setBackgroundResource(getSelectedDrawable(color)) // 선택된 버튼으로 변경
            }
        }

        addButton.setOnClickListener {
            val category = categoryInput.text.toString()

            if (category.isNotEmpty()) {
                onCategoryAddListener?.invoke(category, selectedColor)
                sendCategoryToServer(category, selectedColor) // API 요청
                dismiss()
            } else {
                Toast.makeText(context, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setOnCategoryAddListener(listener: (String, String) -> Unit) {
        onCategoryAddListener = listener
    }

    // API 요청을 보내는 메서드
    private fun sendCategoryToServer(category: String, color: String) {
        val apiService = RetrofitClient.instance
        val accessToken = TokenManager.getAccessToken()


        val requestData = CategoryItemRequest(category, color, "") // 데이터 객체 생성

        if (accessToken != null) {
            apiService.addCategory("$accessToken", requestData).enqueue(object : Callback<ApiResponse<Any>> {
                override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "카테고리가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "카테고리 추가에 실패했습니다: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                    onCategoryAddSuccess()
                }

                override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                    Toast.makeText(context, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(context, "액세스 토큰이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onCategoryAddSuccess() {
        // 카테고리 추가 후 다시 데이터를 가져오도록 ViewModel에 요청
        categoryViewModel.fetchCategories(1)
    }


    // 선택된 버튼의 드로어블을 반환하는 메서드
    private fun getSelectedDrawable(color: String): Int {
        return when (color) {
            "red" -> R.drawable.icon_circle_red_selected
            "orange" -> R.drawable.icon_circle_orange_selected
            "yellow" -> R.drawable.icon_circle_yellow_selected
            "green" -> R.drawable.icon_circle_green_selected
            "blue" -> R.drawable.icon_circle_blue_selected
            "purple" -> R.drawable.icon_circle_purple_selected
            "pink" -> R.drawable.icon_circle_pink_selected
            else -> R.drawable.icon_circle_red_selected
        }
    }

    // 선택되지 않은 버튼의 드로어블을 반환하는 메서드
    private fun getUnselectedDrawable(color: String): Int {
        return when (color) {
            "red" -> R.drawable.icon_circle_red
            "orange" -> R.drawable.icon_circle_orange
            "yellow" -> R.drawable.icon_circle_yellow
            "green" -> R.drawable.icon_circle_green
            "blue" -> R.drawable.icon_circle_blue
            "purple" -> R.drawable.icon_circle_purple
            "pink" -> R.drawable.icon_circle_pink
            else -> R.drawable.icon_circle_red
        }
    }
}
