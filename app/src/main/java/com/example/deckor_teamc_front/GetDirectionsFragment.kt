package com.example.deckor_teamc_front

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.deckor_teamc_front.databinding.FragmentGetDirectionsBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay

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

    private lateinit var mapView: MapView
    private var naverMap: NaverMap? = null
    private var pendingRouteResponse: RouteResponse? = null

    private val pathOverlays = mutableListOf<PathOverlay>()
    private val markers = mutableListOf<Marker>()

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
                Log.e("","notnulleeeee")
                binding.getDirectionsMapLayout.visibility = View.VISIBLE
                if (naverMap != null) {
                    drawRoute(routeResponse)
                    displayDuration(routeResponse.duration)
                } else {
                    pendingRouteResponse = routeResponse
                }
            }
            else Log.e("","eeeee")
        }
    }

    private fun displayDuration(durationInSeconds: Int) {
        val durationInMinutes = Math.round(durationInSeconds / 60.0).toInt()
        binding.getDirectionsRouteTime.text = durationInMinutes.toString()
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

            // 앞쪽 경로와 뒤쪽 경로를 분할
            val (startRoute, endRoute) = splitRouteByInOut(routeResponse)

            // Start Marker 설정
            val startMarker = Marker().apply {
                position = startLatLng
                icon = OverlayImage.fromResource(R.drawable.icon_start_point)
                map = naverMap
                if (startingPointPlaceType != "BUILDING") {
                    setOnClickListener {
                        navigateToInnerMapFragment(startingPointId, startRoute)
                        true
                    }
                }
            }
            markers.add(startMarker) // Marker 리스트에 추가

            // End Marker 설정
            val endMarker = Marker().apply {
                position = endLatLng
                icon = OverlayImage.fromResource(R.drawable.icon_arrive_point)
                map = naverMap
                if (arrivalPointPlaceType != "BUILDING") {
                    setOnClickListener {
                        navigateToInnerMapFragment(arrivalPointId, endRoute)
                        true
                    }
                }
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


    private fun splitRouteByInOut(routeResponse: RouteResponse): Pair<RouteResponse, RouteResponse> {
        // inOut이 false인 첫 번째 경로를 찾음
        val splitIndex = routeResponse.path.indexOfFirst { !it.inOut }

        if (splitIndex == -1) {
            // 만약 inOut이 false인 경로가 없으면 전체 루트를 반환
            return Pair(routeResponse, routeResponse)
        }

        // 앞쪽 경로 (시작 루트)
        val startRoute = routeResponse.copy(path = routeResponse.path.take(splitIndex + 1))

        // 뒤쪽 경로 (끝 루트)
        val endRoute = routeResponse.copy(path = routeResponse.path.drop(splitIndex + 1))

        return Pair(startRoute, endRoute)
    }



    private fun navigateToInnerMapFragment(roomId: Int, route: RouteResponse) {
        viewModel.updateSplitedRoute(route)  // RouteResponse를 ViewModel에 저장

        viewModel.fetchPlaceInfo(roomId, "CLASSROOM") { placeInfo ->
            placeInfo?.let {
                // 캐시에서 BuildingItem 가져오기
                val buildingItem = BuildingCache.get(it.buildingId)

                if (buildingItem != null) {
                    val selectedBuildingName = buildingItem.name
                    val selectedBuildingAboveFloor = buildingItem.floor ?: 0
                    val selectedBuildingUnderFloor = buildingItem.underFloor

                    val selectedRoomFloor = it.floor
                    val selectedRoomMask = it.maskIndex

                    val innerMapFragment = InnerMapFragment.newInstanceFromSearch(
                        selectedBuildingName, selectedBuildingAboveFloor, selectedBuildingUnderFloor,
                        it.buildingId, selectedRoomFloor, selectedRoomMask, true
                    )

                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.add(R.id.main_container, innerMapFragment)
                    transaction.addToBackStack("InnerMapFragment")
                    transaction.commit()
                } else {
                    Log.d("navigateToInnerMapFragment", "BuildingItem not found in cache for buildingId: ${it.buildingId}")
                }
            } ?: run {
                Log.d("navigateToInnerMapFragment", "Failed to fetch place info.")
            }
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
