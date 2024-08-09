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
            if (it.action == MotionEvent.ACTION_DOWN) {
                val imageViewWidth = imageView.width.toFloat()
                val imageViewHeight = imageView.height.toFloat()
                val bitmapWidth = bitmap.width.toFloat()
                val bitmapHeight = bitmap.height.toFloat()

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

                val bitmapX = ((viewX - dx) / scale).toInt()
                val bitmapY = ((viewY - dy) / scale).toInt()

                if (bitmapX >= 0 && bitmapY >= 0 && bitmapX < bitmap.width && bitmapY < bitmap.height) {
                    val pixel = bitmap.getPixel(bitmapX, bitmapY)
                    val redValue = (pixel shr 16) and 0xFF

                    // 로그로 디버깅
                    Log.d("InnerMapTouchHandler", "Touched pixel at ($bitmapX, $bitmapY) with red value: $redValue")

                    // 딕셔너리에서 R값에 해당하는 밸류를 검색
                    fileName = colorMap[redValue]

                    // 토스트 메시지로 표시
                    if (fileName != null) {
                        Toast.makeText(context, "File: $fileName", Toast.LENGTH_SHORT).show()

                        openInnermapModal()

                    } else {
                        Toast.makeText(context, "No matching file for R value: $redValue", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return true
    }
    private fun openInnermapModal() {
        roomList.forEach {
            Log.d("InnerMapTouchHandler", "RoomList name: ${it.name}")
        }
        val matchingRoom = roomList.find { it.name == fileName }
        if (matchingRoom != null) {
            Log.d("InnerMapTouchHandler", "Matching Room: $matchingRoom")
        } else {
            Log.e("InnerMapTouchHandler", "No matching room found for file name: $fileName $roomList" )
        }


        // 해당하는 건물명을 가져와 길찾기 프래그먼트에 전달
        val originalBuildingName = innerMapBinding.root.findViewById<TextView>(R.id.building_name).text.toString()
        val modifiedBuildingName = originalBuildingName.replace(Constants.KU_PREFIX, "")

        val standardBottomSheet = innerMapBinding.includedModal.root.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)

        val modifiedRoomName = modifiedBuildingName + " " + matchingRoom?.name

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
