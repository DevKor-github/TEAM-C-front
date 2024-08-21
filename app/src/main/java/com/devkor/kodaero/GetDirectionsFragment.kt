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
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay

class  GetDirectionsFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentGetDirectionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FetchDataViewModel by viewModels()

    private var startingPointHint: String? = null
    private var arrivalPointHint: String? = null
    private var startingPointPlaceType: String? = null
    private var arrivalPointPlaceType: String? = null
    private var startingPointId: Int = 0
    private var arrivalPointId: Int = 0

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
            if (buildingName != null) {
                binding.searchStartingPointBar.text = buildingName
                startingPointId = roomId
                startingPointPlaceType = roomType
                Log.e("GetDirectionFragment","Item is Filled $startingPointId $arrivalPointId")
            }
        } else {
            arrivalPointHint = buildingName
            if (buildingName != null) {
                binding.searchArrivalPointBar.text = buildingName
                arrivalPointId = roomId
                arrivalPointPlaceType = roomType
                Log.e("GetDirectionFragment","Item is Filled $startingPointId $arrivalPointId")
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener("requestKey") { key, bundle ->
            val result = bundle.getString("buildingName")
            val isStartingPoint = bundle.getBoolean("isStartingPoint")
            val placeType = bundle.getString("placeType")
            val id = bundle.getInt("id")

            if (isStartingPoint) {
                startingPointHint = result
                startingPointPlaceType = placeType
                startingPointId = id
                if (result != null) {
                    binding.searchStartingPointBar.text = result
                }
            } else {
                arrivalPointHint = result
                arrivalPointPlaceType = placeType
                arrivalPointId = id
                if (result != null) {
                    binding.searchArrivalPointBar.text = result
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
        if (startingPointPlaceType != null && startingPointId != null && arrivalPointPlaceType != null && arrivalPointId != null) {
            viewModel.getRoutes(
                startType = startingPointPlaceType!!,
                startId = startingPointId!!,
                endType = arrivalPointPlaceType!!,
                endId = arrivalPointId!!
            )
        }

        Log.e("","hh")

        viewModel.routeResponse.observe(viewLifecycleOwner) { routeResponse ->
            if (routeResponse != null) {
                if (routeResponse.path != null) {
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
                else Log.e("GetDirectionsFragment","Path is null")
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
                this.width = 20
                this.outlineWidth = 5
                this.outlineColor = ContextCompat.getColor(requireContext(), R.color.red)
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

    fun showCurrentRouteStep(routePath: RoutePath, routeResponse: RouteResponse) {

        if (routePath.inOut) {
            // Handle indoor navigation
            val buildingId = routePath.buildingId
            val floor = routePath.floor

            navigateToInnerMapFragment(buildingId, floor, routePath.info, routeResponse, true)

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
            nextRoutePath?.let { showCurrentRouteStep(it, routeResponse) }
            Log.e("eeeeeeeee","")
        } else {
            // Handle end of route or do nothing
        }
    }

    private fun navigateToInnerMapFragment(buildingId: Int?, floor: Int?, info: String, routeResponse: RouteResponse, addBackStack: Boolean) {
        if (buildingId == null || floor == null) {
            Log.e("navigateToInnerMapFragment", "Building ID or Floor is null: buildingId=$buildingId, floor=$floor")
            return
        }

        // Add routeResponse to DirectionSearchRouteDataHolder
        DirectionSearchRouteDataHolder.splitedRoute = routeResponse

        // Proceed with fragment navigation
        viewModel.fetchPlaceInfo(buildingId, "CLASSROOM") { placeInfo ->
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
                                    routeResponse = routeResponse,
                                    addBackStack = false
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


    fun startRouteNavigation(routeResponse: RouteResponse) {
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
        Log.e("fffffffffffff","innermapabcedfreset")
        getRoutes()
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        pendingRouteResponse?.let {
            drawRoute(it)
            displayDuration(it.duration)
            pendingRouteResponse = null
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