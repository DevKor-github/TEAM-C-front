package com.devkor.kodaero

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.devkor.kodaero.databinding.FragmentHomeBinding
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


    private val initCameraPosition: LatLng = LatLng(37.59, 127.03)
    private var cameraPosition: LatLng? = null

    private lateinit var viewModel: FetchDataViewModel

    private val markers = mutableListOf<Marker>()

    private lateinit var selectedBuildingName: String
    private var selectedBuildingId: Int? = 1
        get() = field ?: 1
        set(value) {
            field = value ?: 1
        }
    private var selectedBuildingAboveFloor: Int? = null
    private var selectedBuildingUnderFloor: Int? = null


    private var previousZoom: Double? = null

    private var isInitialSetup = true

    private val backStackListener = FragmentManager.OnBackStackChangedListener {
        if (isInitialSetup) {
            isInitialSetup = false
        } else {
            updateStatusBar(currentFragmentCheck = true)
        }
    }


    // 콜백이 이미 등록되었는지 여부를 추적하는 변수
    private var isBottomSheetCallbackRegistered = false

    // 바텀시트 상태 추적 변수
    private var isInitialExpand = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        closeModal()

        activity?.supportFragmentManager?.addOnBackStackChangedListener(backStackListener)

        updateStatusBar(currentFragmentCheck = false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val targetLayout = view?.findViewById<ViewGroup>(R.id.home_menu_container)

        if (targetLayout != null) {
            handleLayout(targetLayout)
        }

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
                areMarkersVisible = !areMarkersVisible
                showMarkers()
                areMarkersVisible = !areMarkersVisible
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

        setHorizontalScrollViewButtonListeners()

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
        // naverMap.uiSettings.isLocationButtonEnabled = true
        // naverMap.locationTrackingMode = LocationTrackingMode.Follow
        naverMap.uiSettings.isZoomControlEnabled = false

        naverMap.addOnCameraChangeListener { reason, animated ->
            val currentZoom = naverMap.cameraPosition.zoom

            // 줌 레벨이 임계점을 넘었는지 확인
            val isThresholdChanged = MarkerZoomLevelThreshold.zoomToIdsMap.keys.any { threshold ->
                (previousZoom ?: 0.0) < threshold && currentZoom >= threshold ||
                        (previousZoom ?: 0.0) >= threshold && currentZoom < threshold
            }

            if (isThresholdChanged) {
                updateMarkersVisibility(currentZoom)
            }

            // 이전 줌 레벨 업데이트
            previousZoom = currentZoom
        }

        val cameraZoomUpdate = CameraUpdate.zoomTo(14.3)
        val cameraScrollUpdate = CameraUpdate.scrollTo(initCameraPosition)
        naverMap.moveCamera(cameraZoomUpdate)
        naverMap.moveCamera(cameraScrollUpdate)

        val includedLayout = binding.includedLayout.root

        val standardBottomSheet = includedLayout.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

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

        val prefix = "고려대학교 서울캠퍼스"

        val modalDepartButton = includedLayout.findViewById<Button>(R.id.modal_depart_button)

        modalDepartButton.setOnClickListener {
            val cleanedBuildingName = selectedBuildingName.removePrefix(prefix).trim()
            putBuildingDirectionsFragment(true, cleanedBuildingName, "BUILDING", selectedBuildingId)
            closeModal()
        }

        val modalArriveButton = includedLayout.findViewById<Button>(R.id.modal_arrive_button)

        modalArriveButton.setOnClickListener {
            val cleanedBuildingName = selectedBuildingName.removePrefix(prefix).trim()
            putBuildingDirectionsFragment(false, cleanedBuildingName, "BUILDING", selectedBuildingId)
            closeModal()
        }

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)
        observeViewModel()
        viewModel.fetchBuildingList("")
        // API 제공 될 때 까지 임시로 제거

        naverMap.setOnMapClickListener { _, _ ->
            closeModal()
        }

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        // 화면이 꺼질 때 실행하고 싶은 함수 호출
        closeModal()
        Log.d("MyFragment", "Screen is turned off, function executed.")
    }

    private fun hideMarkers() {
        markers.forEach { it.map = null }
    }

    private fun showMarkers() {
        updateMarkersVisibility(naverMap.cameraPosition.zoom)
        // markers.forEach { it.map = naverMap }
    }

    private fun navigateToSearchBuildingFragment() {
        val searchBuildingFragment = SearchBuildingFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(R.id.main_container, searchBuildingFragment)
        transaction.addToBackStack("SearchFragment")
        transaction.commit()
    }

    private fun navigateToGetDirectionsFragment() {
        val getDirectionsFragment = GetDirectionsFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(R.id.main_container, getDirectionsFragment,"DirectionFragment")
        transaction.addToBackStack("DirectionFragment")
        transaction.commit()
    }

    private fun navigateToInnerMapFragment() {
        if (selectedBuildingAboveFloor != null && selectedBuildingUnderFloor != null && selectedBuildingId != null && selectedBuildingName != null) {
            val innerMapFragment = InnerMapFragment.newInstance(
                selectedBuildingName!!,
                selectedBuildingAboveFloor!!,
                selectedBuildingUnderFloor!!,
                selectedBuildingId!!
            )

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(R.id.main_container, innerMapFragment)
            transaction.addToBackStack("InnerMapFragment")
            transaction.commit()
        } else {
            Log.e("navigateToInnerMapFragment", "Missing one or more required arguments")
        }
    }


    private fun setHorizontalScrollViewButtonListeners() {
        val buttonIds = listOf(
            binding.cafeButton,
            binding.cafeteriaButton,
            binding.convenienceStoreButton,
            binding.readingRoomButton,
            binding.studyRoomButton,
            binding.bookReturnMachineButton,
            binding.loungeButton,
            binding.waterPurifierButton,
            binding.vendingMachineButton,
            binding.printerButton,
            binding.tumblerWasherButton,
            binding.onestopAutoMachineButton,
            binding.bankButton,
            binding.smokingAreaButton,
            binding.showerRoomButton,
            binding.gymButton,
            binding.sleepingRoomButton
        )

        for (button in buttonIds) {
            button.setOnClickListener {
                val idString = resources.getResourceEntryName(it.id)
                val keyword = idString.replace("_button", "").uppercase()
                navigateToPinSearchFragment(keyword)
            }
        }
    }

    private fun navigateToPinSearchFragment(keyword: String) {
        val currentCameraPosition = getCurrentCameraPosition()
        val currentZoomLevel = getCurrentZoomLevel()

        val pinSearchFragment = PinSearchFragment.newInstance(keyword, 0, currentCameraPosition, currentZoomLevel)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_container, pinSearchFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun getCurrentCameraPosition(): LatLng {
        return naverMap.cameraPosition.target
    }

    fun getCurrentZoomLevel(): Double {
        return naverMap.cameraPosition.zoom
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun moveCameraToPosition(position: LatLng) {
        if (::naverMap.isInitialized) {
            val cameraUpdate = CameraUpdate.scrollAndZoomTo(position, 16.0)
                .animate(CameraAnimation.Easing, 1000L)
            naverMap.moveCamera(cameraUpdate)
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

    fun closeModal() {
        val includedLayout = binding.includedLayout.root
        val standardBottomSheet = includedLayout.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    // BottomSheet의 확장 상태를 확인하는 메서드
    fun isBottomSheetExpanded(): Boolean {
        val includedLayout = binding.includedLayout.root
        val standardBottomSheet = includedLayout.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        return standardBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN
    }

    private fun observeViewModel() {
        viewModel.buildingList.observe(viewLifecycleOwner, Observer { buildingList ->
            buildingList.forEach { building ->
                // BuildingCache에 저장
                BuildingCache.put(building.buildingId, building)

                // 캐시된 정보를 사용하거나 새로운 정보를 추가
                setMarker(building)
                val currentZoom = naverMap.cameraPosition.zoom
                updateMarkersVisibility(currentZoom)
            }
        })
    }


    private fun setMarker(building: BuildingItem) {
        if (building.buildingId in MarkerZoomLevelThreshold.availableBuildingList) {
            val marker = Marker().apply {
                position = LatLng(building.latitude ?: 0.0, building.longitude ?: 0.0)
                map = naverMap
                icon = OverlayImage.fromResource(R.drawable.spot)
                tag = building.buildingId
            }

            markers.add(marker) // 마커 리스트에 추가

            marker.setOnClickListener {
                openBuildingModal(building.buildingId)
                true
            }
        }
    }

    fun openBuildingModal(buildingId: Int) {
        val building = getBuildingInfo(buildingId) ?: return

        val buildingName =
            binding.includedLayout.root.findViewById<TextView>(R.id.modal_sheet_building_name)
        val buildingAddress =
            binding.includedLayout.root.findViewById<TextView>(R.id.modal_sheet_building_address)
        val buildingOperating =
            binding.includedLayout.root.findViewById<TextView>(R.id.modal_sheet_operating_status)
        val buildingNextOperating =
            binding.includedLayout.root.findViewById<TextView>(R.id.modal_sheet_deadline)
        val buildingNextOperatingCont =
            binding.includedLayout.root.findViewById<TextView>(R.id.modal_sheet_deadline_cont)
        val standardBottomSheet =
            binding.includedLayout.root.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)

        buildingName.text = building.name
        buildingAddress.text = building.address

        when {
            building.weekdayOperatingTime.contains("00:00-00:00") ||
                    building.saturdayOperatingTime.contains("00:00-00:00") ||
                    building.sundayOperatingTime.contains("00:00-00:00") -> {

                buildingOperating.text = "운영 정보 없음"
                buildingOperating.setTextColor(resources.getColor(R.color.gray, null))
                buildingNextOperating.text = null
                buildingNextOperatingCont.text = null
            }

            building.weekdayOperatingTime.contains("00:00-23:59") &&
                    building.saturdayOperatingTime.contains("00:00-23:59") &&
                    building.sundayOperatingTime.contains("00:00-23:59") -> {

                buildingOperating.text = "운영 중"
                buildingOperating.setTextColor(resources.getColor(R.color.red, null))
                buildingNextOperating.text = "상시 운영"
                buildingNextOperatingCont.text = null
            }

            else -> {
                if (building.operating) {
                    buildingOperating.text = "운영 중"
                    buildingOperating.setTextColor(resources.getColor(R.color.red, null))
                    buildingNextOperating.text = building.nextBuildingTime ?: "운영 시간이 설정되지 않았습니다"
                    buildingNextOperatingCont.text = "에 운영 종료"
                } else {
                    buildingOperating.text = "운영 종료"
                    buildingOperating.setTextColor(resources.getColor(R.color.red, null))
                    buildingNextOperating.text = building.nextBuildingTime ?: "운영 시간이 설정되지 않았습니다"
                    buildingNextOperatingCont.text = "에 운영 시작"
                }
            }
        }

        selectedBuildingName = building.name
        selectedBuildingAboveFloor = building.floor
        selectedBuildingUnderFloor = building.underFloor
        selectedBuildingId = building.buildingId

        val facilityTypesRecyclerView =
            binding.includedLayout.root.findViewById<RecyclerView>(R.id.modal_sheet_facility_types)
        val adapter = FacilityTypeAdapter(building.placeTypes, requireContext())
        facilityTypesRecyclerView.adapter = adapter

        standardBottomSheet.setOnClickListener {
            navigateToBuildingDetailFragment()
            closeModal()
        }

        isInitialExpand = true

        if (!isBottomSheetCallbackRegistered) {
            standardBottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    Log.e("", "turned changed $newState $isInitialExpand")
                    if (newState == BottomSheetBehavior.STATE_EXPANDED && !isInitialExpand) {
                        navigateToBuildingDetailFragment()
                        closeModal()
                    }
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN) {
                        isInitialExpand = true
                    } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        isInitialExpand = false
                        Log.e("", "turned flase")
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // 슬라이드 상태에서의 추가 처리
                }
            })

            // 콜백이 등록되었음을 기록
            isBottomSheetCallbackRegistered = true
        }

        // 바텀시트 상태를 바로 확장 상태로 설정
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun navigateToBuildingDetailFragment() {
        selectedBuildingId?.let { id ->
            val fragmentManager = parentFragmentManager

            // Remove any existing BuildingDetailFragment for this building from the back stack
            fragmentManager.popBackStack("BuildingDetailFragment_$id", FragmentManager.POP_BACK_STACK_INCLUSIVE)

            val fragment = BuildingDetailFragment.newInstance(id)
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.main_container, fragment, "BuildingDetailFragment_$id")
            transaction.addToBackStack("BuildingDetailFragment_$id")
            transaction.commit()
        }
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

    private fun updateMarkersVisibility(zoom: Double) {
        for (marker in markers) {
            val buildingId = marker.tag as? Int
            if (buildingId != null) {
                // ID에 해당하는 줌 임계값을 가져옴
                val threshold = MarkerZoomLevelThreshold.getThresholdForId(buildingId)

                if (zoom >= threshold && areMarkersVisible) {
                    marker.map = naverMap // 줌 값이 임계값 이상이면 마커 표시
                } else {
                    marker.map = null // 줌 값이 임계값 이하이면 마커 숨김
                }
            }
        }
    }

    fun updateSelectedBuilding(id: Int) {
        selectedBuildingId = id
        Log.e("updateSelectedBuilding", "$selectedBuildingId")

        // buildingList 중에 id가 같은 데이터를 찾아 selectedBuildingName, selectedBuildingFloor를 갱신
        val building = viewModel.buildingList.value?.find { it.buildingId == id }
        if (building != null) {
            selectedBuildingName = building.name
            selectedBuildingAboveFloor = building.floor
            selectedBuildingUnderFloor = building.underFloor
            Log.d("updateSelectedBuilding", "Selected building name: $selectedBuildingName")
        } else {
            Log.e("updateSelectedBuilding", "Building with id $id not found")
        }
    }

    private fun putBuildingDirectionsFragment(isStartingPoint: Boolean, buildingName: String, placeType: String, id: Int?) {
        val getDirectionsFragment = GetDirectionsFragment().apply {
            arguments = Bundle().apply {
                putBoolean("isStartingPoint", isStartingPoint)
                putString("buildingName", buildingName)
                putString("placeType", placeType)
                if (id != null) {
                    putInt("placeId", id)
                }
            }
        }
        val activity = context as? FragmentActivity
        activity?.supportFragmentManager?.beginTransaction()
            ?.add(R.id.main_container, getDirectionsFragment,"DirectionFragment")
            ?.addToBackStack("DirectionFragment")
            ?.commit()
    }

    private fun updateStatusBar(currentFragmentCheck: Boolean) {
        // 상태 바 높이를 가져와 status_bar에 적용
        val statusBarView = activity?.window?.decorView?.findViewById<LinearLayout>(R.id.status_bar)
        val statusBarHeight = getStatusBarHeight()

        // 메인 컨테이너에 프래그먼트가 있는 지 확인(현재 홈 프래그 먼트 인지 확인)
        val isCurrentFragmentTop = if (currentFragmentCheck) {
            requireActivity().supportFragmentManager.findFragmentById(R.id.main_container) == null
        } else {
            true
        }

        if (isCurrentFragmentTop) {
            // 프래그먼트가 최상단에 있으면 상태 바 레이아웃 높이를 그대로 유지하고 배경색을 투명으로 설정
            statusBarView?.layoutParams?.height = statusBarHeight
            statusBarView?.setBackgroundColor(Color.TRANSPARENT)
        } else {
            // 그렇지 않으면 상태 바 높이로 설정하고 배경색을 흰색으로 설정
            statusBarView?.layoutParams?.height = statusBarHeight
            statusBarView?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        // 레이아웃 변경 사항을 적용
        statusBarView?.requestLayout()

    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }


    private fun handleLayout(layout: ViewGroup) {
        // 상태 바 높이를 가져와서 상단 패딩을 적용하는 함수
        layout.setOnApplyWindowInsetsListener { view, insets ->
            val statusBarHeight = insets.systemWindowInsetTop

            // 패딩 적용: 기존 패딩 값은 유지하고 상단 패딩만 상태 바 높이로 설정
            view.setPadding(
                view.paddingLeft,        // 기존 왼쪽 패딩 유지
                statusBarHeight,         // 상단 패딩을 상태 바 높이로 설정
                view.paddingRight,       // 기존 오른쪽 패딩 유지
                view.paddingBottom       // 기존 하단 패딩 유지
            )

            // 소비된 insets를 반환하여 추가적인 시스템 처리 방지
            insets
        }

        // Insets을 강제로 적용하여 초기 패딩 설정
        layout.requestApplyInsets()
    }


}