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
import com.example.deckor_teamc_front.databinding.InnerMapContainerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.fragment.app.FragmentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InnerMapTouchHandler(
    private val context: Context,
    private val imageView: ImageView,
    private val bitmap: Bitmap,
    private val colorMap: Map<Int, String>,
    private val innerMapBinding: InnerMapContainerBinding,
    private val floor: Int,
    private val id: Int,
    private val replaceInnermapCallback: (String?) -> Unit // 추가된 부분
) : View.OnTouchListener {

    private var fileName: String = "" // obsolete
    private var maskIndex: Int = 0

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

                    maskIndex = redValue

                    // 로그로 디버깅
                    Log.d("InnerMapTouchHandler", "Touched pixel at ($bitmapX, $bitmapY) with red value: $redValue")

                    // 딕셔너리에서 R값에 해당하는 파일명을 검색
                    fileName = colorMap[redValue].toString()

                    // 파일명이 존재하면 토스트 메시지로 표시하고, 모달 열기 함수 호출
                    openInnermapModal()
                    replaceInnermapCallback(fileName)
                    Log.e("InnerMapTouchHandler","Replace callback")
                }
            }
        }
        return true
    }
    private fun openInnermapModal() {
        Log.d("InnerMapTouchHandler", "Matching Room: $fileName")

        val originalBuildingName = innerMapBinding.root.findViewById<TextView>(R.id.building_name).text.toString()
        // val modifiedBuildingName = originalBuildingName.replace(Constants.KU_PREFIX, "")

        val standardBottomSheet = innerMapBinding.includedModal.root.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)

        // 첫 번째 API 호출: MaskInfo를 가져옴
        fetchMaskInfo(id, floor, maskIndex, "CLASSROOM") { roomId ->
            if (roomId != null) {
                // 두 번째 API 호출: Place 정보 가져오기
                fetchPlaceInfo(roomId) { placeName ->
                    if (placeName != null && maskIndex != 0) {
                        val modifiedRoomName = "$placeName"

                        standardBottomSheet.findViewById<TextView>(R.id.modal_sheet_building_name).text = modifiedRoomName
                        // standardBottomSheet.findViewById<View>(R.id.consent_container).visibility = View.GONE
                        standardBottomSheet.findViewById<View>(R.id.innermap_container).visibility = View.GONE
                        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                        standardBottomSheet.findViewById<View>(R.id.modal_depart_button).setOnClickListener {
                            navigateToGetDirectionsFragment(true, modifiedRoomName, roomId, "CLASSROOM")
                        }

                        standardBottomSheet.findViewById<View>(R.id.modal_arrive_button).setOnClickListener {
                            navigateToGetDirectionsFragment(false, modifiedRoomName, roomId, "CLASSROOM")
                        }
                    } else {
                        Log.e("InnerMapTouchHandler", "Failed to fetch place name")
                    }
                }
            } else {
                Log.e("InnerMapTouchHandler", "Failed to fetch room info")
            }
        }
    }

    // 첫 번째 API 호출: MaskInfo를 가져옴
    private fun fetchMaskInfo(id: Int, floor: Int, redValue: Int, type: String, callback: (Int?) -> Unit) {
        RetrofitClient.instance.getMaskInfo(id, floor, redValue, type).enqueue(object : Callback<ApiResponse<MaskInfoResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<MaskInfoResponse>>,
                response: Response<ApiResponse<MaskInfoResponse>>
            ) {
                if (response.isSuccessful && response.body()?.statusCode == 0) {
                    val roomId = response.body()?.data?.placeId
                    Log.d("InnerMapTouchHandler", "Fetched Place ID: $roomId")
                    callback(roomId)
                } else {
                    Log.e("InnerMapTouchHandler", "Failed to fetch place info: ${response.errorBody()?.string()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<ApiResponse<MaskInfoResponse>>, t: Throwable) {
                Log.e("InnerMapTouchHandler", "$id $floor $redValue call failed: ${t.message}")
                callback(null)
            }
        })
    }

    // 두 번째 API 호출: Place 정보 가져오기
    private fun fetchPlaceInfo(roomId: Int, callback: (String?) -> Unit) {
        RetrofitClient.instance.getPlaceInfo(roomId, "CLASSROOM").enqueue(object : Callback<ApiResponse<PlaceInfoResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<PlaceInfoResponse>>,
                response: Response<ApiResponse<PlaceInfoResponse>>
            ) {
                if (response.isSuccessful && response.body()?.statusCode == 0) {
                    val placeName = response.body()?.data?.name
                    Log.d("InnerMapTouchHandler", "Fetched Place Name: $placeName")
                    callback(placeName)
                } else {
                    Log.e("InnerMapTouchHandler", "Failed to fetch place name: ${response.errorBody()?.string()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<ApiResponse<PlaceInfoResponse>>, t: Throwable) {
                Log.e("InnerMapTouchHandler", "API call failed: ${t.message}")
                callback(null)
            }
        })
    }




    private fun navigateToGetDirectionsFragment(isStartingPoint: Boolean, buildingName: String, roomId: Int, roomType: String) {

        val getDirectionsFragment = GetDirectionsFragment().apply {
            arguments = Bundle().apply {
                putBoolean("isStartingPoint", isStartingPoint)
                putString("buildingName", buildingName)
                putInt("placeId", roomId)
                putString("placeType", roomType)
            }
        }
        val activity = context as? FragmentActivity
        activity?.supportFragmentManager?.beginTransaction()
            ?.add(R.id.main_container, getDirectionsFragment)
            ?.addToBackStack(null)
            ?.commit()
    }



}
