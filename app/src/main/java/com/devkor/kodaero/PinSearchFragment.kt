package com.devkor.kodaero

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.devkor.kodaero.databinding.FragmentPinSearchBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PinSearchFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentPinSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private val LOCATION_PERMISSION_REQUEST_CODE = 5000

    private val initCameraPosition: LatLng = LatLng(37.59, 127.03)

    private lateinit var viewModel: FetchDataViewModel
    private lateinit var facilityType: String

    private val markers = mutableListOf<Marker>()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var pinSearchAdapter: PinSearchAdapter

    private val selectedMarkers = mutableListOf<Marker>()
    private val unselectedMarkers = mutableListOf<Marker>()

    private val facilitiesMap = mutableMapOf<Int, List<FacilityItem>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPinSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToHomeButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        facilityType = arguments?.getString(ARG_FACILITY_NAME) ?: "Facility"
        binding.facilityName.text = getFacilityNameInKorean(facilityType)

        initMapView()

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)

        lifecycleScope.launch {
            if (facilityType in listOf("CAFE", "CAFETERIA", "CONVENIENCE_STORE")) {
                viewModel.individualFacilityList.observe(viewLifecycleOwner, { facilities ->
                    if (facilities.isNotEmpty()) {
                        updateMarkers(facilities = facilities)
                    }
                })

                withContext(Dispatchers.IO) {
                    viewModel.searchFacilities(facilityType)
                }
            } else {
                viewModel.buildingList.observe(viewLifecycleOwner, { buildingList ->
                    if (buildingList.isNotEmpty()) {
                        updateMarkers(buildingList = buildingList)
                    }
                })

                withContext(Dispatchers.IO) {
                    viewModel.fetchBuildingList(facilityType)
                }
            }
        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.pinSearchIncludedLayout.standardBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        pinSearchAdapter = PinSearchAdapter()
        binding.pinSearchIncludedLayout.pinSearchListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.pinSearchIncludedLayout.pinSearchListRecyclerview.adapter = pinSearchAdapter

        view.post {
            bottomSheetBehavior.peekHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f, resources.displayMetrics).toInt()
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.peekHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f, resources.displayMetrics).toInt()
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    if (selectedMarkers.isNotEmpty()) {
                        val currentSelectedMarker = selectedMarkers.first()
                        unselectMarker(currentSelectedMarker)
                        unselectAllMarkers()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun getFacilityNameInKorean(facilityType: String): String {
        return when (facilityType) {
            "CAFE" -> "카페"
            "CAFETERIA" -> "식당"
            "CONVENIENCE_STORE" -> "편의점"
            "READING_ROOM" -> "열람실"
            "STUDY_ROOM" -> "스터디룸"
            "LOUNGE" -> "라운지"
            "WATER_PURIFIER" -> "정수기"
            "PRINTER" -> "프린터"
            "VENDING_MACHINE" -> "자판기"
            "SMOKING_AREA" -> "흡연구역"
            "SLEEPING_ROOM" -> "수면실"
            "BOOK_RETURN_MACHINE" -> "도서 반납기"
            else -> facilityType
        }
    }

    private fun initMapView() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map_view, it).commit()
            }
        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun updateMarkers(buildingList: List<BuildingItem>? = null, facilities: List<FacilityItem>? = null) {
        if (!::naverMap.isInitialized) {
            return
        }

        markers.clear()

        buildingList?.forEach { building ->
            viewModel.getFacilities(building.buildingId, facilityType)
        }

        viewModel.facilityList.observe(viewLifecycleOwner, { facilityMap ->
            facilityMap.forEach { (buildingId, facilities) ->
                facilitiesMap[buildingId] = facilities.map {
                    it.copy(facilityType = this@PinSearchFragment.facilityType) // Copy with facilityType
                }
                val count = facilities.size
                val building = buildingList?.find { it.buildingId == buildingId }
                if (building != null) {
                    addMarker(LatLng(building.latitude ?: 0.0, building.longitude ?: 0.0), building.buildingId, count.toString())
                }
            }
            if (::naverMap.isInitialized) {
                unselectAllMarkers()
            }
        })

        viewModel.individualFacilityList.observe(viewLifecycleOwner, { facilities ->
            facilities.forEach { facility ->
                facilitiesMap[facility.facilityId] = listOf(facility).map {
                    it.copy(facilityType = this@PinSearchFragment.facilityType) // Copy with facilityType
                }
                addMarker(
                    LatLng(facility.latitude, facility.longitude),
                    facility.facilityId,
                    facility.name
                )
            }
            if (::naverMap.isInitialized) {
                unselectAllMarkers()
            }
        })
    }


    private fun addMarker(position: LatLng, id: Int, text: String) {
        binding.root.post {
            val marker = Marker().apply {
                this.position = position
                tag = id

                if (facilityType in listOf("CAFE", "CAFETERIA", "CONVENIENCE_STORE")) {
                    val drawableName = "pin_${facilityType.lowercase()}"
                    val drawableResId = resources.getIdentifier(drawableName, "drawable", requireContext().packageName)
                    icon = OverlayImage.fromBitmap(createCustomDrawableWithText(drawableResId, text))
                } else {
                    val drawableName = "pin_${facilityType.lowercase()}"
                    val drawableResId = resources.getIdentifier(drawableName, "drawable", requireContext().packageName)
                    icon = OverlayImage.fromBitmap(createDrawableWithText(drawableResId, text))
                }
            }

            marker.setOnClickListener {
                onMarkerClick(marker)
                true
            }

            unselectedMarkers.add(marker)
            markers.add(marker)
            marker.map = naverMap
        }
    }

    private fun onMarkerClick(marker: Marker) {
        when {
            selectedMarkers.isEmpty() -> {
                selectMarker(marker)
            }

            selectedMarkers.isNotEmpty() -> {
                val currentSelectedMarker = selectedMarkers.first()
                if (currentSelectedMarker == marker) {
                    unselectMarker(currentSelectedMarker)
                    unselectAllMarkers()
                } else {
                    unselectMarker(currentSelectedMarker)
                    selectMarker(marker)
                }
            }
        }
    }

    private fun selectMarker(marker: Marker) {
        selectedMarkers.add(marker)
        unselectedMarkers.remove(marker)

        unselectedMarkers.forEach { unselectedMarker ->
            val unselectedDrawableName = "unselected_pin_${facilityType.lowercase()}"
            val unselectedDrawableResId = resources.getIdentifier(unselectedDrawableName, "drawable", requireContext().packageName)
            unselectedMarker.icon = OverlayImage.fromResource(unselectedDrawableResId)
        }

        val selectedDrawableName = "pin_${facilityType.lowercase()}"
        val selectedDrawableResId = resources.getIdentifier(selectedDrawableName, "drawable", requireContext().packageName)

        val id = marker.tag as Int
        val facilityName = facilitiesMap[id]?.firstOrNull { it.facilityId == id }?.name ?: ""

        marker.icon = if (facilityType in listOf("CAFE", "CAFETERIA", "CONVENIENCE_STORE")) {
            OverlayImage.fromBitmap(createCustomDrawableWithText(selectedDrawableResId, facilityName))
        } else {
            OverlayImage.fromBitmap(createDrawableWithText(selectedDrawableResId, (facilitiesMap[id]?.size ?: "").toString()))
        }

        val facilities = facilitiesMap[id] ?: emptyList()
        pinSearchAdapter.submitList(facilities)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun unselectMarker(marker: Marker) {
        selectedMarkers.remove(marker)
        unselectedMarkers.add(marker)

        val unselectedDrawableName = "unselected_pin_${facilityType.lowercase()}"
        val unselectedDrawableResId = resources.getIdentifier(unselectedDrawableName, "drawable", requireContext().packageName)
        marker.icon = OverlayImage.fromResource(unselectedDrawableResId)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun unselectAllMarkers() {
        if (selectedMarkers.isNotEmpty()) {
            val currentSelectedMarker = selectedMarkers.first()
            unselectMarker(currentSelectedMarker)
        }

        markers.forEach { marker ->
            unselectedMarkers.add(marker)
            selectedMarkers.remove(marker)

            val unselectedDrawableName = "pin_${facilityType.lowercase()}"
            val unselectedDrawableResId = resources.getIdentifier(unselectedDrawableName, "drawable", requireContext().packageName)
            val id = marker.tag as Int
            val facilityName = facilitiesMap[id]?.firstOrNull { it.facilityId == id }?.name ?: ""

            marker.icon = if (facilityType in listOf("CAFE", "CAFETERIA", "CONVENIENCE_STORE")) {
                OverlayImage.fromBitmap(createCustomDrawableWithText(unselectedDrawableResId, facilityName))
            } else {
                OverlayImage.fromBitmap(createDrawableWithText(unselectedDrawableResId, (facilitiesMap[id]?.size ?: "").toString()))
            }

            marker.map = naverMap
        }
    }

    fun hasSelectedMarkers(): Boolean {
        return selectedMarkers.isNotEmpty()
    }

    private fun createDrawableWithText(drawableResId: Int, text: String?, marginTop: Int = 20): Bitmap {
        val originalBitmap = getDrawableAsBitmap(drawableResId)

        val bitmapWithMargin = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height - marginTop,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmapWithMargin)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)

        if (text.isNullOrEmpty()) return bitmapWithMargin

        val paintCircle = Paint().apply {
            color = ContextCompat.getColor(requireContext(), R.color.red)
            isAntiAlias = true
        }
        val circleRadius = 25f
        val circleX = bitmapWithMargin.width - circleRadius - 10f
        val circleY = circleRadius + 10f
        canvas.drawCircle(circleX, circleY, circleRadius, paintCircle)

        val paintText = Paint().apply {
            color = ContextCompat.getColor(requireContext(), android.R.color.white)
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textBounds = android.graphics.Rect()
        paintText.getTextBounds(text, 0, text.length, textBounds)
        val textX = circleX - (textBounds.width() / 2)
        val textY = circleY + (textBounds.height() / 2)
        canvas.drawText(text, textX, textY, paintText)

        return bitmapWithMargin
    }

    private fun createCustomDrawableWithText(drawableResId: Int, text: String): Bitmap {
        val originalBitmap = getDrawableAsBitmap(drawableResId)

        val paintStroke = Paint().apply {
            color = ContextCompat.getColor(requireContext(), android.R.color.white) // Stroke color
            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics)
            typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 3f
            textAlign = Paint.Align.CENTER
        }

        val paintFill = Paint().apply {
            color = ContextCompat.getColor(requireContext(), R.color.facility_name) // Text fill color
            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics)
            typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
            isAntiAlias = true
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
        }

        val textWidth = paintFill.measureText(text)
        val bitmapWidth = maxOf(originalBitmap.width.toFloat(), textWidth).toInt()
        val bitmapWithText = Bitmap.createBitmap(
            bitmapWidth,
            originalBitmap.height + 10,
            Bitmap.Config.ARGB_8888
        ).apply {
            density = originalBitmap.density
        }

        val canvas = Canvas(bitmapWithText)
        val bitmapLeft = (bitmapWidth - originalBitmap.width) / 2f
        canvas.drawBitmap(originalBitmap, bitmapLeft, 30f, null)
        val x = bitmapWidth / 2f
        val y = paintFill.textSize

        canvas.drawText(text, x, y, paintStroke)

        canvas.drawText(text, x, y, paintFill)

        return bitmapWithText
    }

    private fun getDrawableAsBitmap(drawableResId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableResId)!!
        return Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true

        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow

        val initPosition = arguments?.getParcelable<LatLng>(ARG_INIT_CAMERA_POSITION) ?: initCameraPosition
        val initZoom = arguments?.getDouble(ARG_INIT_ZOOM_LEVEL) ?: 14.3

        val cameraZoomUpdate = CameraUpdate.zoomTo(initZoom)
        val cameraScrollUpdate = CameraUpdate.scrollTo(initPosition)
        naverMap.moveCamera(cameraZoomUpdate)
        naverMap.moveCamera(cameraScrollUpdate)

        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow

        viewModel.individualFacilityList.observe(viewLifecycleOwner, { facilities ->
            if (facilities.isNotEmpty()) {
                updateMarkers(facilities = facilities)
            }
        })

        viewModel.buildingList.value?.let { buildingList ->
            if (buildingList.isNotEmpty()) {
                updateMarkers(buildingList = buildingList)
            }
        }

        naverMap.setOnMapClickListener { _, _ ->
            if (selectedMarkers.isNotEmpty()) {
                val currentSelectedMarker = selectedMarkers.first()
                unselectMarker(currentSelectedMarker)
                unselectAllMarkers()
            }
        }

        unselectAllMarkers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_FACILITY_NAME = "facility_name"
        private const val ARG_INIT_CAMERA_POSITION = "init_camera_position"
        private const val ARG_INIT_ZOOM_LEVEL = "init_zoom_level"

        fun newInstance(facilityName: String, initCameraPosition: LatLng, initZoomLevel: Double): PinSearchFragment {
            val fragment = PinSearchFragment()
            val args = Bundle().apply {
                putString(ARG_FACILITY_NAME, facilityName)
                putParcelable(ARG_INIT_CAMERA_POSITION, initCameraPosition)
                putDouble(ARG_INIT_ZOOM_LEVEL, initZoomLevel)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
