package com.devkor.kodaero

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.devkor.kodaero.databinding.FragmentKoyeonBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class KoyeonFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentKoyeonBinding? = null
    private val binding get() = _binding!!

    private lateinit var naverMap: NaverMap
    private lateinit var viewModel: FetchDataViewModel


    // 콜백이 이미 등록되었는지 여부를 추적하는 변수
    private var isBottomSheetCallbackRegistered = false

    // 바텀시트 상태 추적 변수
    private var isInitialExpand = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKoyeonBinding.inflate(inflater, container, false)
        closeModal()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeModal()

        binding.backToHomeButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)
        initMapView()

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


    private fun initMapView() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map_view, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        // 초기 카메라 위치 설정
        val cameraZoomUpdate = CameraUpdate.zoomTo(16.3)
        val cameraScrollUpdate = CameraUpdate.scrollTo(LatLng(37.58485, 127.0295378))
        naverMap.moveCamera(cameraZoomUpdate)
        naverMap.moveCamera(cameraScrollUpdate)
        naverMap.uiSettings.isZoomControlEnabled = false

        // 지도 제약 설정
        val latLngBounds = LatLngBounds(
            LatLng(37.57977937432624, 127.02283666260797), // 남서쪽 좌표
            LatLng(37.60021922232589, 127.03716333739249)  // 북동쪽 좌표
        )
        naverMap.setExtent(latLngBounds)

        // 줌 레벨 제한 설정
        naverMap.minZoom = 14.0

        // LocationSource 설정
        val locationSource = FusedLocationSource(this, 1)
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow


        viewLifecycleOwner.lifecycleScope.launch {
            val pubs = withContext(Dispatchers.IO) {
                viewModel.fetchPubs()
            }

            pubs?.forEach { pub ->
                val marker = Marker().apply {
                    position = LatLng(pub.latitude, pub.longitude)
                    icon = OverlayImage.fromResource(R.drawable.pin_koyeon)
                    map = naverMap
                }
                marker.setOnClickListener {
                    closeModal()
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(100)
                        openKoyeonModal(pub.id)
                    }
                    true
                }
            }
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openKoyeonModal(pubId: Int) {
        Log.e("FetchPubInfo","$pubId")
        viewLifecycleOwner.lifecycleScope.launch {
            val pub = withContext(Dispatchers.IO) {
                viewModel.fetchPubInfo(pubId)
            }

            if (pub == null) {
                Log.e("openKoyeonModal", "Pub not found.")
                return@launch
            }

            // Update modal with the pub information
            val pubName = binding.modalSheetKoyeon.root.findViewById<TextView>(R.id.name)
            val pubAddress = binding.modalSheetKoyeon.root.findViewById<TextView>(R.id.address)
            val standardBottomSheet = binding.modalSheetKoyeon.root.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
            val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
            val menuButton = binding.modalSheetKoyeon.menuButton
            val pubSponsor = binding.modalSheetKoyeon.root.findViewById<TextView>(R.id.sponser)




            // Set the pub details
            pubName.text = pub.name
            pubAddress.text = pub.address ?: "주소 정보 없음"
            pubSponsor.text = pub.sponsor

            menuButton.setOnClickListener {
                closeModal()
                navigateToPubDetailFragment(pub)
            }


            binding.modalSheetKoyeon.modalDepartButton.setOnClickListener{
                putBuildingDirectionsFragment(
                    isStartingPoint = true,
                    buildingName = pub.name, // 여기에 실제 건물 이름을 설정하세요
                    placeType = "COORD",
                    id = -1, // 여기에 실제 건물 ID를 설정하세요
                    lat = pub.latitude,
                    lng = pub.longitude
                )
            }
            binding.modalSheetKoyeon.modalArriveButton.setOnClickListener {
                putBuildingDirectionsFragment(
                    isStartingPoint = false,
                    buildingName = pub.name, // 여기에 실제 건물 이름을 설정하세요
                    placeType = "COORD",
                    id = -1, // 여기에 실제 건물 ID를 설정하세요
                    lat = pub.latitude,
                    lng = pub.longitude
                )
            }

            isInitialExpand = true

            if (!isBottomSheetCallbackRegistered) {
                standardBottomSheetBehavior.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED && !isInitialExpand) {
                            // Perform actions when the sheet is fully expanded
                        }

                        // Reset the initial expand flag when the sheet is collapsed or hidden
                        if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN) {
                            isInitialExpand = true
                        } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            isInitialExpand = false
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        // Optional: Handle actions during the slide, if needed
                    }
                })

                isBottomSheetCallbackRegistered = true
            }

            // Expand the bottom sheet to show the pub details
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }


    private fun closeModal(){
        val standardBottomSheet = binding.modalSheetKoyeon.root.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        standardBottomSheetBehavior.state=BottomSheetBehavior.STATE_HIDDEN
    }

    private fun navigateToPubDetailFragment(pubDetail: PubDetail) {
        val koyeonMenuFragment = KoyeonMenuFragment.newInstance(pubDetail)
        parentFragmentManager.beginTransaction()
            .add(R.id.main_container, koyeonMenuFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun putBuildingDirectionsFragment(isStartingPoint: Boolean, buildingName: String, placeType: String, id: Int, lat: Double, lng: Double) {
        setFragmentResult("requestKey", Bundle().apply {
            putString("buildingName", buildingName)
            putBoolean("isStartingPoint", isStartingPoint)
            putString("placeType", placeType)
            putInt("id", id)
            putDouble("lat", lat)
            putDouble("lng", lng)
        })

        val getDirectionsFragment = GetDirectionsFragment()
        val activity = context as? FragmentActivity
        activity?.supportFragmentManager?.beginTransaction()
            ?.add(R.id.main_container, getDirectionsFragment, "DirectionFragment")
            ?.addToBackStack("DirectionFragment")
            ?.commit()
    }

}
