package com.devkor.kodaero

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.devkor.kodaero.databinding.FragmentGetDirectionsBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource

class GetDirectionsFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentGetDirectionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FetchDataViewModel by viewModels()

    private var startingPointHint: String? = null
    private var arrivalPointHint: String? = null
    private var startingPointPlaceType: String? = null
    private var arrivalPointPlaceType: String? = null
    private var startingPointId: Int = 0
    private var arrivalPointId: Int = 0
    private var startingPointLat: Double? = null
    private var startingPointLng: Double? = null
    private var arrivalPointLat: Double? = null
    private var arrivalPointLng: Double? = null

    private lateinit var mapView: MapView
    private var naverMap: NaverMap? = null
    private var pendingRouteResponse: RouteResponse? = null

    private val pathOverlays = mutableListOf<PathOverlay>()
    private val markers = mutableListOf<Marker>()

    var currentRouteIndex = 0

    private var fetchedRouteResponse: RouteResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGetDirectionsBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        val isStartingPointAssigned = arguments?.getBoolean("isStartingPoint") ?: true
        val buildingName = arguments?.getString("buildingName") ?: "" // 기본값을 설정
        val roomId = arguments?.getInt("placeId") ?: -1 // 기본값 -1, 실제로 존재하지 않는 ID로 설정
        val roomType = arguments?.getString("placeType") ?: "CLASSROOM" // 기본값 설정


        if (isStartingPointAssigned) {
            startingPointHint = buildingName
            binding.searchStartingPointBar.text = buildingName
            startingPointId = roomId
            startingPointPlaceType = roomType
            Log.e("GetDirectionFragment","Item is Filled $startingPointId $arrivalPointId")
        } else {
            arrivalPointHint = buildingName
            binding.searchArrivalPointBar.text = buildingName
            arrivalPointId = roomId
            arrivalPointPlaceType = roomType
            Log.e("GetDirectionFragment","Item is Filled $startingPointId $arrivalPointId")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener("requestKey") { _, bundle ->
            val result = bundle.getString("buildingName")
            val isStartingPoint = bundle.getBoolean("isStartingPoint")
            val placeType = bundle.getString("placeType")
            val id = bundle.getInt("id")
            val lat = bundle.getDouble("lat")
            val lng = bundle.getDouble("lng")

            if (isStartingPoint) {
                startingPointHint = result
                startingPointPlaceType = placeType
                startingPointId = id
                startingPointLat = lat
                startingPointLng = lng
                if (result != null) {
                    binding.searchStartingPointBar.text = result
                }
                if (startingPointPlaceType == "COORD" && arrivalPointPlaceType == "COORD" && false){
                    arrivalPointHint = null
                    binding.searchArrivalPointBar.text = null
                }
            } else {
                arrivalPointHint = result
                arrivalPointPlaceType = placeType
                arrivalPointId = id
                arrivalPointLat = lat
                arrivalPointLng = lng
                if (result != null) {
                    binding.searchArrivalPointBar.text = result
                }
                if (startingPointPlaceType == "COORD" && arrivalPointPlaceType == "COORD" && false){
                    startingPointHint = null
                    binding.searchStartingPointBar.text = null
                }
            }


            if (startingPointHint != null && arrivalPointHint != null) {
                Log.e("GetDirectionFragment","Item is Filled $startingPointId $arrivalPointId")
                getRoutes()
            }
        }

        binding.searchStartingPointBar.setOnClickListener {
            navigateToGetDirectionsSearchBuildingFragment(true)
        }

        binding.searchArrivalPointBar.setOnClickListener {
            navigateToGetDirectionsSearchBuildingFragment(false)
        }

        binding.backToHomeButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack("HomeFragment", 0)
        }

        binding.switchButton.setOnClickListener {
            switchHints()
            getRoutes()
        }

        startingPointHint?.let {
            binding.searchStartingPointBar.text = it
        }

        arrivalPointHint?.let {
            binding.searchArrivalPointBar.text = it
        }

    }

    private fun switchHints() {
        val tempHint = startingPointHint
        startingPointHint = arrivalPointHint
        arrivalPointHint = tempHint

        val tempPlaceType = startingPointPlaceType
        startingPointPlaceType = arrivalPointPlaceType
        arrivalPointPlaceType = tempPlaceType

        val tempId = startingPointId
        startingPointId = arrivalPointId
        arrivalPointId = tempId

        if (startingPointHint.isNullOrEmpty()) {
            binding.searchStartingPointBar.text = null
            binding.searchStartingPointBar.hint = "출발지를 입력해주세요"
        } else {
            binding.searchStartingPointBar.text = startingPointHint
        }

        if (arrivalPointHint.isNullOrEmpty()) {
            binding.searchArrivalPointBar.text = null
            binding.searchArrivalPointBar.hint = "도착지를 입력해주세요"
        } else {
            binding.searchArrivalPointBar.text = arrivalPointHint
        }

    }

    private fun navigateToGetDirectionsSearchBuildingFragment(isStartingPoint: Boolean) {
        val getDirectionsSearchBuildingFragment = GetDirectionsSearchBuildingFragment()
        getDirectionsSearchBuildingFragment.arguments = Bundle().apply {
            putBoolean("isStartingPoint", isStartingPoint)
        }
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(R.id.main_container, getDirectionsSearchBuildingFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getRoutes() {
        if (startingPointPlaceType != null && arrivalPointPlaceType != null) {
            viewModel.getRoutes(
                startType = startingPointPlaceType!!,
                startId = startingPointId,
                startLat = startingPointLat,
                startLong = startingPointLng,
                endType = arrivalPointPlaceType!!,
                endId = arrivalPointId,
                endLat = arrivalPointLat,
                endLong = arrivalPointLng
            )
        }

        Log.e("ergwgewgrewhrh","$startingPointPlaceType, $startingPointId, $startingPointLat, $startingPointLng, $arrivalPointPlaceType, $arrivalPointId, $arrivalPointLat, $arrivalPointLng")

        viewModel.routeResponse.observe(viewLifecycleOwner) { routeResponse ->
            if (routeResponse != null) {
                binding.directionErrorContainer.visibility = View.GONE
                fetchedRouteResponse = routeResponse
                Log.e("", "notnull")
                binding.getDirectionsMapLayout.visibility = View.VISIBLE

                binding.getDirectionsGuideLayout.visibility = View.GONE
                binding.getDirectionsSelectDirectionLayout.visibility = View.VISIBLE

                binding.toDetailRouteButton.setOnClickListener {
                    startRouteNavigation(routeResponse)
                }
                if (naverMap != null) {
                    drawRoute(routeResponse)
                    displayDuration(routeResponse.duration)
                } else {
                    pendingRouteResponse = routeResponse
                }
            }
            else Log.e("GetDirectionsFragment","RouteResponse is null")
        }
    }

    private fun displayDuration(durationInSeconds: Int) {
        val durationInMinutes = Math.round(durationInSeconds / 60.0).toInt()
        binding.getDirectionsRouteTime.text = durationInMinutes.toString()

        val durationInMeter = Math.round(durationInSeconds * 1.25).toInt()
        binding.getDirectionsRouteDistance.text = durationInMeter.toString()
    }

    private fun drawRoute(routeResponse: RouteResponse) {
        if (naverMap == null) {
            return
        }

        // 모든 오버레이 및 마커 제거
        removeAllOverlays()

        val coords = routeResponse.path.filter { !it.inOut }
            .flatMap { it.route }
            .map { LatLng(it[0], it[1]) }

        if (coords.isNotEmpty()) {
            val startLatLng = coords.first()
            val endLatLng = coords.last()

            // Start Marker 설정
            val startMarker = Marker().apply {
                position = startLatLng
                icon = OverlayImage.fromResource(R.drawable.icon_start_point)
                map = naverMap
            }
            markers.add(startMarker) // Marker 리스트에 추가

            // End Marker 설정
            val endMarker = Marker().apply {
                position = endLatLng
                icon = OverlayImage.fromResource(R.drawable.icon_arrive_point)
                map = naverMap
            }
            markers.add(endMarker) // Marker 리스트에 추가

            // PathOverlay 생성 및 지도에 추가
            val path = PathOverlay().apply {
                this.coords = coords
                this.color = ContextCompat.getColor(requireContext(), R.color.red)
                this.width = 15
                this.outlineWidth = 2
                this.outlineColor = ContextCompat.getColor(requireContext(), R.color.neon_red)
                this.map = naverMap
            }
            pathOverlays.add(path) // PathOverlay 리스트에 추가

            // 카메라 업데이트
            val bounds = LatLngBounds.Builder()
                .include(startLatLng)
                .include(endLatLng)
                .build()

            val cameraUpdate = CameraUpdate.fitBounds(bounds, 200)
            naverMap?.moveCamera(cameraUpdate)
        }
    }

    private fun showCurrentRouteStep(routePath: RoutePath, routeResponse: RouteResponse) {

        if (routePath.inOut) {
            // Handle indoor navigation
            val buildingId = routePath.buildingId
            val floor = routePath.floor

            navigateToInnerMapFragment(buildingId, floor, routePath.info, routeResponse)

        } else {
            val latitude = routePath.route.firstOrNull()?.get(0) ?: return
            val longitude = routePath.route.firstOrNull()?.get(1) ?: return

            val cameraUpdate = CameraUpdate
                .scrollAndZoomTo(LatLng(latitude, longitude), 18.0)
                .animate(CameraAnimation.Fly)
            naverMap?.moveCamera(cameraUpdate)

            // Update the guide UI
            binding.getDirectionsGuideInout.text = "실외"
            binding.getDirectionsGuideInfo.text = routePath.info

            binding.getDirectionsGuideNumber.text = (currentRouteIndex + 1).toString()

            binding.getDirectionsGuideLayout.visibility = View.VISIBLE
            binding.getDirectionsSelectDirectionLayout.visibility = View.GONE

            when {
                routePath.info.contains("층으로", ignoreCase = true) -> {
                    binding.toNextGuideButton.setImageResource(R.drawable.move_floor_button)
                }
                routePath.info.contains("나가세요", ignoreCase = true) -> {
                    binding.toNextGuideButton.setImageResource(R.drawable.move_outside_button)
                }
                routePath.info.contains("들어가세요", ignoreCase = true) -> {
                    binding.toNextGuideButton.setImageResource(R.drawable.move_inside_button)
                }
            }

            if (routePath.info == "도착") {
                binding.toNextGuideButton.visibility = View.GONE
            } else {
                binding.toNextGuideButton.visibility = View.VISIBLE
                binding.toNextGuideButton.setOnClickListener {
                    moveToNextRouteStep(routeResponse)
                }
            }
        }
    }

    private fun moveToNextRouteStep(routeResponse: RouteResponse) {
        currentRouteIndex++

        if (currentRouteIndex < routeResponse.path.size) {
            val nextRoutePath = routeResponse.path[currentRouteIndex]
            showCurrentRouteStep(nextRoutePath, routeResponse)
        } else {
            // Handle end of route or do nothing
        }
    }

    private fun navigateToInnerMapFragment(
        buildingId: Int?,
        floor: Int?,
        info: String,
        routeResponse: RouteResponse
    ) {
        if (buildingId == null || floor == null) {
            Log.e("navigateToInnerMapFragment", "Building ID or Floor is null: buildingId=$buildingId, floor=$floor")
            return
        }

        // Add routeResponse to DirectionSearchRouteDataHolder
        DirectionSearchRouteDataHolder.splitedRoute = routeResponse

        // Proceed with fragment navigation
        viewModel.fetchPlaceInfo(buildingId) { placeInfo ->
            placeInfo?.let {
                val buildingItem = BuildingCache.get(it.buildingId)
                if (buildingItem != null) {
                    val innerMapFragment = InnerMapFragment.newInstanceFromSearch(
                        selectedBuildingName = BuildingCache.get(buildingId)!!.name,
                        selectedBuildingAboveFloor = BuildingCache.get(buildingId)!!.floor,
                        selectedBuildingUnderFloor = BuildingCache.get(buildingId)!!.underFloor,
                        selectedBuildingId = buildingId,
                        selectedRoomFloor = floor,
                        selectedRoomMask = it.maskIndex,
                        hasDirection = true
                    )

                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.add(R.id.main_container, innerMapFragment)
                    transaction.addToBackStack("InnerMapFragment")
                    transaction.commit()

                    requireActivity().supportFragmentManager.executePendingTransactions()

                    // Set the route info to the InnerMapFragment
                    innerMapFragment.setRouteInfo(info, currentRouteIndex)

                    // Set up toNextGuideButton functionality
                    innerMapFragment.binding.toNextGuideButton.setOnClickListener {
                        currentRouteIndex++
                        val nextRoutePath = routeResponse.path.getOrNull(currentRouteIndex)
                        nextRoutePath?.let {
                            if(nextRoutePath.buildingId != 0) {
                                navigateToInnerMapFragment(
                                    buildingId = nextRoutePath.buildingId,
                                    floor = nextRoutePath.floor,
                                    info = nextRoutePath.info,
                                    routeResponse = routeResponse
                                )
                            }
                            else{
                                currentRouteIndex--
                                moveToNextRouteStep(routeResponse)
                                requireActivity().supportFragmentManager.popBackStack("DirectionFragment", 0)
                                Log.e("navigateToInnerMapFragment", "No more route steps available.")
                                true
                            }
                        } ?: run {
                            Log.e("navigateToInnerMapFragment", "Route is null.")
                        }
                    }
                } else {
                    Log.d("navigateToInnerMapFragment", "BuildingItem not found in cache for buildingId: ${it.buildingId}")
                }
            } ?: run {
                Log.d("navigateToInnerMapFragment", "Failed to fetch place info.")
            }
        }
    }



    private fun startRouteNavigation(routeResponse: RouteResponse) {
        currentRouteIndex = 0
        pendingRouteResponse = routeResponse

        // Call the method that uses routeResponse
        routeResponse.path.firstOrNull()?.let {
            showCurrentRouteStep(it, routeResponse)
        }
    }

    private fun removeAllOverlays() {
        // 저장된 PathOverlay 제거
        pathOverlays.forEach { it.map = null }
        pathOverlays.clear()

        // 저장된 Marker 제거
        markers.forEach { it.map = null }
        markers.clear()
    }

    fun resetRouteStep(){
        currentRouteIndex = 0
        getRoutes()
    }


    override fun onMapReady(map: NaverMap) {
        naverMap = map


        // LocationSource 설정
        val locationSource = FusedLocationSource(this, 1)
        naverMap!!.locationSource = locationSource
        naverMap!!.locationTrackingMode = LocationTrackingMode.NoFollow

        naverMap!!.uiSettings.isLocationButtonEnabled = false
        naverMap!!.uiSettings.isZoomControlEnabled = false

        pendingRouteResponse?.let {
            drawRoute(it)
            displayDuration(it.duration)
            pendingRouteResponse = null
        }


        binding.locationBuuton.visibility = View.VISIBLE
        binding.locationBuuton.setOnClickListener {
            val locationHelper = LocationHelper(requireActivity())

            locationHelper.checkAndRequestLocationPermission(
                onPermissionGranted = {
                    locationHelper.checkGpsEnabledAndRequestLocation { lat, lng ->

                    }
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}