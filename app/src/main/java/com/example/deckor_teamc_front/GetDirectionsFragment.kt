package com.example.deckor_teamc_front

import android.os.Bundle
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
    private var startingPointId: Int? = null
    private var arrivalPointId: Int? = null

    private lateinit var mapView: MapView
    private var naverMap: NaverMap? = null
    private var pendingRouteResponse: RouteResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGetDirectionsBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        val isStartingPointAssigned = arguments?.getBoolean("isStartingPoint") ?: true
        val buildingName = arguments?.getString("buildingName")

        if (isStartingPointAssigned) {
            startingPointHint = buildingName
            if (buildingName != null) {
                binding.searchStartingPointBar.text = buildingName
            }
        } else {
            arrivalPointHint = buildingName
            if (buildingName != null) {
                binding.searchArrivalPointBar.text = buildingName
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().supportFragmentManager.popBackStack("HomeFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        })

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
            requireActivity().supportFragmentManager.popBackStack("HomeFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
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

        viewModel.routeResponse.observe(viewLifecycleOwner) { routeResponse ->
            if (routeResponse != null) {
                binding.getDirectionsMapLayout.visibility = View.VISIBLE
                if (naverMap != null) {
                    drawRoute(routeResponse)
                    displayDuration(routeResponse.duration)
                } else {
                    pendingRouteResponse = routeResponse
                }
            }
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

        val coords = routeResponse.path.filter { !it.inOut }
            .flatMap { it.route }
            .map { LatLng(it[0], it[1]) }

        if (coords.isNotEmpty()) {
            val path = PathOverlay()
            path.coords = coords
            path.color = ContextCompat.getColor(requireContext(), R.color.red)
//            path.patternImage = OverlayImage.fromResource(R.drawable.button_switch)
//            path.patternInterval = 50
            path.width = 20
            path.outlineWidth = 5
            path.outlineColor = ContextCompat.getColor(requireContext(), R.color.red)
            path.map = naverMap

            val startLatLng = coords.first()
            val endLatLng = coords.last()

            val startMarker = Marker().apply {
                position = startLatLng
                icon = OverlayImage.fromResource(R.drawable.icon_start_point)
                map = naverMap
            }

            val endMarker = Marker().apply {
                position = endLatLng
                icon = OverlayImage.fromResource(R.drawable.icon_arrive_point)
                map = naverMap
            }

            val bounds = LatLngBounds.Builder()
                .include(startLatLng)
                .include(endLatLng)
                .build()

            val cameraUpdate = CameraUpdate.fitBounds(bounds, 200)
            naverMap?.moveCamera(cameraUpdate)
        }
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
