package com.example.deckor_teamc_front

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deckor_teamc_front.databinding.FragmentPinSearchBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource

class PinSearchFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentPinSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private val LOCATION_PERMISSION_REQUEST_CODE = 5000

    private var cameraPosition: LatLng? = null

    private lateinit var viewModel: FetchDataViewModel
    private lateinit var facilityType: String

    private val markers = mutableListOf<Marker>()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var pinSearchAdapter: PinSearchAdapter

    // To hold facilities for each building
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
        viewModel.buildingList.observe(viewLifecycleOwner, { buildingList ->
            if (buildingList.isNotEmpty()) {
                updateMarkers(buildingList)
            }
        })
        viewModel.searchFacilities(facilityType)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.pinsearchIncludedLayout.standardBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        pinSearchAdapter = PinSearchAdapter()
        binding.pinsearchIncludedLayout.pinSearchListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.pinsearchIncludedLayout.pinSearchListRecyclerview.adapter = pinSearchAdapter

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.peekHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f, resources.displayMetrics).toInt()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Handle on slide events if needed
            }
        })
    }

    private fun getFacilityNameInKorean(facilityType: String): String {
        return when (facilityType) {
            "CAFE" -> "카페"
            "CAFETERIA" -> "식당"
            "CONVENIENCE_STORE" -> "편의점"
            "READING_ROOM" -> "열람실"
            "STUDY_ROOM" -> "스터디룸"
            "REST_AREA" -> "휴게 공간"
            "WATER_PURIFIER" -> "정수기"
            "PRINTER" -> "프린터"
            "VENDING_MACHINE" -> "자판기"
            "SMOKING_AREA" -> "흡연구역"
            "SLEEPING_ROOM" -> "수면실"
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

    private fun updateMarkers(buildingList: List<BuildingItem>) {
        markers.clear()
        buildingList.forEach { building ->
            viewModel.getFacilities(building.buildingId, facilityType)
        }

        // Observe the facilityList to update markers once data is fetched
        viewModel.facilityList.observe(viewLifecycleOwner, { facilityMap ->
            facilityMap.forEach { (buildingId, facilities) ->
                facilitiesMap[buildingId] = facilities
                val count = facilities.size
                val building = buildingList.find { it.buildingId == buildingId }
                if (building != null) {
                    addMarker(building, count)
                }
            }
            if (::naverMap.isInitialized) {
                showMarkersOnMap()
            }
        })
    }

    private fun addMarker(building: BuildingItem, count: Int) {
        val drawableName = "pin_${facilityType.lowercase()}"
        val drawableResId = resources.getIdentifier(drawableName, "drawable", requireContext().packageName)
        val marker = Marker().apply {
            position = LatLng(building.latitude ?: 0.0, building.longitude ?: 0.0)
            icon = OverlayImage.fromBitmap(createDrawableWithText(drawableResId, count.toString()))
            tag = building.buildingId
        }
        marker.setOnClickListener {
            onMarkerClick(marker)
            true
        }
        markers.add(marker)
    }

    private fun onMarkerClick(marker: Marker) {
        val buildingId = marker.tag as Int
        val facilities = facilitiesMap[buildingId] ?: emptyList()
        pinSearchAdapter.submitList(facilities)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showMarkersOnMap() {
        markers.forEach { it.map = naverMap }
    }

    private fun getDrawableAsBitmap(drawableResId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableResId)!!
        return Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
    }

    private fun createDrawableWithText(drawableResId: Int, text: String): Bitmap {
        val bitmap = getDrawableAsBitmap(drawableResId)
        val canvas = Canvas(bitmap)

        val paintCircle = Paint().apply {
            color = ContextCompat.getColor(requireContext(), R.color.red)
            isAntiAlias = true
        }
        val circleRadius = 25f
        val circleX = bitmap.width - circleRadius - 10f
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

        return bitmap
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        val cameraUpdate = CameraUpdate.zoomTo(16.0)
        naverMap.moveCamera(cameraUpdate)

        cameraPosition?.let {
            moveCameraToPosition(it)
        }

        showMarkersOnMap()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun moveCameraToPosition(position: LatLng) {
        if (::naverMap.isInitialized) {
            naverMap.moveCamera(CameraUpdate.scrollTo(position).animate(CameraAnimation.Easing, 1000L))
        } else {
            cameraPosition = position
        }
    }

    companion object {
        private const val ARG_FACILITY_NAME = "facility_name"

        fun newInstance(facilityName: String): PinSearchFragment {
            val fragment = PinSearchFragment()
            val args = Bundle().apply {
                putString(ARG_FACILITY_NAME, facilityName)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
