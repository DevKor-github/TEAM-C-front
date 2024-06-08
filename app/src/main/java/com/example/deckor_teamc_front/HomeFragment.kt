package com.example.deckor_teamc_front

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.deckor_teamc_front.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource

class HomeFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private val LOCATION_PERMISSION_REQUEST_CODE = 5000

    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private var isImageOneDisplayed = true
    private var areMarkersVisible = true

    private var cameraPosition: LatLng? = null

    private lateinit var viewModel: FetchDataViewModel

    private val markers = mutableListOf<Marker>()

    private lateinit var selectedBuildingName: String
    private var selectedBuildingFloor: Int? = 1
        get() = field ?: 1
        set(value) {
            field = value ?: 1
        }
    private var selectedBuildingId: Int? = 1
        get() = field ?: 1
        set(value) {
            field = value ?: 1
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        closeModal()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchButton.setOnClickListener {
            navigateToSearchBuildingFragment()
            closeModal()
        }

        binding.searchRouteButton.setOnClickListener {
            navigateToGetDirectionsFragment()
            closeModal()
        }

        if (!hasPermission()) {
            requestPermissions(PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            initMapView()
        }

        val pinOnoffButton: ImageButton = binding.pinOnoffButton
        pinOnoffButton.setOnClickListener {
            // 마커를 토글하는 로직 추가
            if (areMarkersVisible) {
                hideMarkers()
            } else {
                showMarkers()
            }
            areMarkersVisible = !areMarkersVisible

            // 이미지 토글 로직은 그대로 유지
            if (isImageOneDisplayed) {
                pinOnoffButton.setImageResource(R.drawable.pin_on_button)
            } else {
                pinOnoffButton.setImageResource(R.drawable.pin_off_button)
            }
            isImageOneDisplayed = !isImageOneDisplayed
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

    private fun hasPermission(): Boolean {
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        val cameraUpdate = CameraUpdate.zoomTo(17.0)
        naverMap.moveCamera(cameraUpdate)

        if(cameraPosition!=null){
            moveCameraToPosition(cameraPosition!!)

        }

        val includedLayout = binding.includedLayout.root

        val standardBottomSheet = includedLayout.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        val buildingName = includedLayout.findViewById<TextView>(R.id.building_name)

        standardBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    else -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        val innerMapButton = includedLayout.findViewById<Button>(R.id.modal_innermap_button)

        innerMapButton.setOnClickListener {
            navigateToInnerMapFragment()
            closeModal()
        }

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)
        observeViewModel()
        viewModel.fetchBuildingList()
        // API 제공 될 때 까지 임시로 제거

    }

    private fun hideMarkers() {
        markers.forEach { it.map = null }
    }

    private fun showMarkers() {
        markers.forEach { it.map = naverMap }
    }

    private fun navigateToSearchBuildingFragment() {
        val searchBuildingFragment = SearchBuildingFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(R.id.main_container, searchBuildingFragment)
        transaction.addToBackStack("HomeFragment")
        transaction.commit()
    }

    private fun navigateToGetDirectionsFragment() {
        val getDirectionsFragment = GetDirectionsFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(R.id.main_container, getDirectionsFragment)
        transaction.addToBackStack("HomeFragment")
        transaction.commit()
    }

    private fun navigateToInnerMapFragment() {
        val innerMapFragment =
            selectedBuildingFloor?.let { selectedBuildingId?.let { it1 ->
                InnerMapFragment.newInstance(selectedBuildingName, it,
                    it1
                )
            } }
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        if (innerMapFragment != null) {
            transaction.add(R.id.main_container, innerMapFragment)
        }
        transaction.addToBackStack("HomeFragment")
        transaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun moveCameraToPosition(position: LatLng) {

        if (::naverMap.isInitialized) {
            naverMap.moveCamera(CameraUpdate.scrollTo(position).animate(CameraAnimation.Easing,1000L))

            Handler(Looper.getMainLooper()).postDelayed({

                expandModal()
            }, 100L)
        } else {
            cameraPosition = position
        }
    }
    private fun expandModal() {

        val includedLayout = binding.includedLayout.root
        val standardBottomSheet = includedLayout.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)

        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

    }

    private fun closeModal() {
        val includedLayout = binding.includedLayout.root
        val standardBottomSheet = includedLayout.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun observeViewModel() {
        viewModel.buildingList.observe(viewLifecycleOwner, Observer { buildingList ->
            buildingList.forEach { building ->
                // 여기서 UI 업데이트 또는 로그 출력
                Log.d("ExampleFragment", "Building: $building")
                setMarker(building)
            }
        })
    }

    private fun setMarker(building: BuildingItem) {
        val marker = Marker().apply {
            position = LatLng(building.latitude ?: 0.0, building.longitude ?: 0.0)
            map = naverMap
            icon = OverlayImage.fromResource(R.drawable.spot)
        }

        markers.add(marker) // 마커 리스트에 추가

        marker.setOnClickListener {
            val buildingName = binding.includedLayout.root.findViewById<TextView>(R.id.building_name)
            val buildingClass = binding.includedLayout.root.findViewById<TextView>(R.id.building_class)
            val buildingAddress = binding.includedLayout.root.findViewById<TextView>(R.id.building_address)
            val standardBottomSheet = binding.includedLayout.root.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
            val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)

            buildingName.text = building.name
            buildingClass.text = building.placeType
            buildingAddress.text = building.address
            selectedBuildingName = building.name
            selectedBuildingFloor = building.floor
            selectedBuildingId = building.buildingId
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            true
        }
    }
}