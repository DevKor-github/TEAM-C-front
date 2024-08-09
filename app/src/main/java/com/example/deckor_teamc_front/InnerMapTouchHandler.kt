package com.example.deckor_teamc_front

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.deckor_teamc_front.databinding.InnerMapContainerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.fragment.app.FragmentActivity

class InnerMapTouchHandler(
    private val context: Context,
    private val imageView: ImageView,
    private val bitmap: Bitmap,
    private val colorMap: Map<Int, String>,
    private val roomList: List<RoomList>,
    private val innerMapBinding: InnerMapContainerBinding
) : View.OnTouchListener {

    private var fileName: String? = null

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?.let {
            // 터치 이벤트가 ACTION_DOWN일 때만 처리
            if (it.action == MotionEvent.ACTION_DOWN) {
                val imageViewWidth = imageView.width.toFloat()
                val imageViewHeight = imageView.height.toFloat()
                val bitmapWidth = bitmap.width.toFloat()
                val bitmapHeight = bitmap.height.toFloat()

                // 이미지 뷰와 비트맵의 크기를 디버깅
                Log.d("Debug", "ImageView Width: $imageViewWidth, Height: $imageViewHeight")
                Log.d("Debug", "Bitmap Width: $bitmapWidth, Height: $bitmapHeight")

                // 이미지 뷰에서 터치한 좌표
                val viewX = event.x
                val viewY = event.y

                // 이미지 뷰의 스케일 타입이 fitCenter인 경우 비트맵 좌표로 변환
                val scale: Float = if (bitmapWidth / bitmapHeight > imageViewWidth / imageViewHeight) {
                    imageViewWidth / bitmapWidth
                } else {
                    imageViewHeight / bitmapHeight
                }

                val dx = (imageViewWidth - bitmapWidth * scale) / 2
                val dy = (imageViewHeight - bitmapHeight * scale) / 2

                // dx와 dy 값을 디버깅
                Log.d("Debug", "dx: $dx, dy: $dy")

                // 터치한 좌표를 비트맵의 좌표로 변환
                val bitmapX = ((viewX - dx) / scale).toInt()
                val bitmapY = ((viewY - dy) / scale).toInt()

                // 변환된 비트맵 좌표를 디버깅
                Log.d("Debug", "Touch at ImageView: ($viewX, $viewY)")
                Log.d("Debug", "Mapped to Bitmap: ($bitmapX, $bitmapY)")

                // 비트맵 좌표가 유효한 범위 내에 있는지 확인
                if (bitmapX >= 0 && bitmapY >= 0 && bitmapX < bitmap.width && bitmapY < bitmap.height) {
                    // 비트맵에서 해당 좌표의 픽셀 값을 가져옴
                    val pixel = bitmap.getPixel(bitmapX, bitmapY)
                    // 픽셀 값에서 R 값을 추출
                    val redValue = (pixel shr 16) and 0xFF

                    // 로그로 디버깅
                    Log.d("InnerMapTouchHandler", "Touched pixel at ($bitmapX, $bitmapY) with red value: $redValue")

                    // 딕셔너리에서 R값에 해당하는 파일명을 검색
                    fileName = colorMap[redValue]

                    // 파일명이 존재하면 토스트 메시지로 표시하고, 모달 열기 함수 호출
                    if (fileName != null) {
                        Toast.makeText(context, "File: $fileName", Toast.LENGTH_SHORT).show()
                        openInnermapModal()
                    } else {
                        // 파일명이 존재하지 않으면 알림 메시지 표시
                        Toast.makeText(context, "No matching file for R value: $redValue", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return true
    }

    private fun openInnermapModal() {
        /*
        roomList.forEach {
            Log.d("InnerMapTouchHandler", "RoomList name: ${it.name}")
        }
        val matchingRoom = roomList.find { it.name == fileName }
        if (matchingRoom != null) {
            Log.d("InnerMapTouchHandler", "Matching Room: $matchingRoom")
        } else {
            Log.e("InnerMapTouchHandler", "No matching room found for file name: $fileName $roomList" )
        }
        */
        // 임시 파일명 추후 인덱스에 해당하는 실제 건물 명으로 변경예정 + ID 값 첨부
        val matchingRoom = fileName
        Log.d("InnerMapTouchHandler", "Matching Room: $matchingRoom")


        // 해당하는 건물명을 가져와 길찾기 프래그먼트에 전달
        val originalBuildingName = innerMapBinding.root.findViewById<TextView>(R.id.building_name).text.toString()
        val modifiedBuildingName = originalBuildingName.replace(Constants.KU_PREFIX, "")

        val standardBottomSheet = innerMapBinding.includedModal.root.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)

        val modifiedRoomName = modifiedBuildingName + " " + matchingRoom//?.name

        standardBottomSheet.findViewById<TextView>(R.id.building_name).text = modifiedRoomName

//        standardBottomSheet.findViewById<View>(R.id.consent_container).visibility = View.GONE
        standardBottomSheet.findViewById<View>(R.id.innermap_container).visibility = View.GONE
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED


        standardBottomSheet.findViewById<View>(R.id.modal_depart_button).setOnClickListener {
            navigateToGetDirectionsFragment(true,modifiedRoomName)
        }

        standardBottomSheet.findViewById<View>(R.id.modal_arrive_button).setOnClickListener {
            navigateToGetDirectionsFragment(false,modifiedRoomName)
        }
    }


    private fun navigateToGetDirectionsFragment(isStartingPoint: Boolean, buildingName: String) {
        val getDirectionsFragment = GetDirectionsFragment().apply {
            arguments = Bundle().apply {
                putBoolean("isStartingPoint", isStartingPoint)
                putString("buildingName", buildingName)
            }
        }
        val activity = context as? FragmentActivity
        activity?.supportFragmentManager?.beginTransaction()
            ?.add(R.id.main_container, getDirectionsFragment)
            ?.addToBackStack(null)
            ?.commit()
    }
}
