package com.devkor.kodaero

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.devkor.kodaero.databinding.InnerMapContainerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
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
    private val buildingId: Int,
    private val fragment: InnerMapFragment, // InnerMapFragment의 참조 추가
    private val replaceInnermapCallback: (String?) -> Unit // 추가된 부분
) : View.OnTouchListener {

    private val isNodeMaskBuild: Boolean = false
    private var fileName: String = "" // obsolete
    private var maskIndex: Int = 0

    private var currentSnackbar: Snackbar? = null  // 현재 표시 중인 스낵바를 저장
    private var lastMessage: String? = null  // 마지막으로 표시된 메시지를 저장

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?.let {
            // 터치 이벤트가 ACTION_DOWN일 때만 처리
            if (it.action == MotionEvent.ACTION_UP) {
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

                    // 마스크 노드 찍기용
                    if(isNodeMaskBuild) {
                        if (maskIndex != 0) {
                            val newMessage = "MaskIndex: $maskIndex"

                            // 이전 메시지와 동일하다면 새로 표시하지 않고 기존 스낵바를 유지
                            if (newMessage == lastMessage && currentSnackbar?.isShown == true) {
                                return true
                            }

                            // 이전에 표시된 스낵바가 있으면 즉시 중단
                            currentSnackbar?.dismiss()

                            // 새로운 스낵바 메시지를 생성
                            currentSnackbar = Snackbar.make(
                                v ?: return@let,
                                newMessage,
                                Snackbar.LENGTH_INDEFINITE
                            )

                            // 스낵바의 애니메이션을 최소화
                            currentSnackbar?.setAnimationMode(Snackbar.ANIMATION_MODE_FADE) // 페이드 애니메이션 사용 (빠르게 표시)

                            // 스낵바의 텍스트 크기 및 속성 설정
                            val snackbarView = currentSnackbar?.view
                            val snackbarTextView =
                                snackbarView?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

                            snackbarTextView?.let {
                                it.textSize = 24f  // 텍스트 크기를 크게 설정
                                it.maxLines = 3  // 필요한 경우 여러 줄을 허용
                            }

                            snackbarView?.let {
                                val params = it.layoutParams as ViewGroup.MarginLayoutParams
                                params.setMargins(0, 0, 0, 0)  // 마진 최소화

                                // 스낵바를 화면의 위쪽에 표시하도록 설정
                                if (params is CoordinatorLayout.LayoutParams) {
                                    params.gravity = Gravity.TOP
                                } else if (params is ViewGroup.MarginLayoutParams) {
                                    params.topMargin = 0  // 스낵바를 상단에 붙임
                                }
                                it.layoutParams = params
                            }

                            // 스낵바를 즉시 표시 (자동으로 사라지지 않음)
                            currentSnackbar?.show()

                            // 마지막 메시지 업데이트
                            lastMessage = newMessage

                        }
                    }

                    openInnermapModal(maskIndex)
                    replaceInnermapCallback(fileName)
                    Log.e("InnerMapTouchHandler","Replace callback")
                }
            }
        }
        return true
    }

    fun openInnermapModal(maskIndex: Int) {
        Log.d("InnerMapTouchHandler", "Matching Room: $fileName")

        val originalBuildingName = innerMapBinding.root.findViewById<TextView>(R.id.building_name).text.toString()
        // val modifiedBuildingName = originalBuildingName.replace(Constants.KU_PREFIX, "")

        val standardBottomSheet = innerMapBinding.includedModal.root.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)

        val roomDetail = standardBottomSheet.findViewById<TextView>(R.id.modal_sheet_building_address)
        // 첫 번째 API 호출: MaskInfo를 가져옴
        fetchMaskInfo(buildingId, floor, maskIndex, "CLASSROOM") { roomId ->
            if (roomId != null) {
                // 두 번째 API 호출: Place 정보 가져오기
                fetchPlaceInfo(roomId) { placeInfo ->
                    if (placeInfo != null && placeInfo.maskIndex != 0) {
                        val fetchedRoomName = placeInfo.name
                        standardBottomSheet.findViewById<TextView>(R.id.modal_sheet_building_name).text = fetchedRoomName


                        // Binding 객체를 통해 UI 업데이트
                        fragment.selectedPlaceId = roomId // 예시로 추가된 코드

                        if(placeInfo.detail != ".") {
                            roomDetail.text = placeInfo.detail
                            roomDetail.visibility = View.VISIBLE
                        }
                        else{
                            roomDetail.text = null
                            roomDetail.visibility = View.GONE
                        }
                        standardBottomSheet.findViewById<View>(R.id.modal_sheet_operating_container).visibility = View.GONE
                        // standardBottomSheet.findViewById<View>(R.id.consent_container).visibility = View.GONE
                        standardBottomSheet.findViewById<View>(R.id.innermap_container).visibility = View.GONE
                        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                        standardBottomSheet.findViewById<View>(R.id.modal_depart_button).setOnClickListener {
                            navigateToGetDirectionsFragment(true, fetchedRoomName, roomId, "PLACE")
                        }

                        standardBottomSheet.findViewById<View>(R.id.modal_arrive_button).setOnClickListener {
                            navigateToGetDirectionsFragment(false, fetchedRoomName, roomId, "PLACE")
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
        private fun fetchPlaceInfo(placeId : Int, callback: (PlaceInfoResponse?) -> Unit) {
        RetrofitClient.instance.getPlaceInfo(placeId).enqueue(object : Callback<ApiResponse<PlaceInfoResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<PlaceInfoResponse>>,
                response: Response<ApiResponse<PlaceInfoResponse>>
            ) {
                if (response.isSuccessful && response.body()?.statusCode == 0) {
                    val placeInfo = response.body()?.data
                    Log.d("InnerMapTouchHandler", "Fetched Place Info: $placeInfo")
                    callback(placeInfo)
                } else {
                    Log.e("InnerMapTouchHandler", "Failed to fetch place info: ${response.errorBody()?.string()}")
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
            ?.add(R.id.main_container, getDirectionsFragment,"DirectionFragment")
            ?.addToBackStack("DirectionFragment")
            ?.commit()
    }

    private fun getBuildingInfo(buildingId: Int): BuildingItem? {
        // BuildingCache에서 빌딩 정보를 가져옴
        val building = BuildingCache.get(buildingId)

        if (building != null) {
            // 캐시에 빌딩 정보가 있는 경우 해당 정보를 반환
            return building
        } else {
            return null // 또는 예외 처리
        }
    }


}
