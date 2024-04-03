package com.example.deckor_teamc_front

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.deckor_teamc_front.databinding.FragmentHomeBinding
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

    private lateinit var marker1: Marker
    private lateinit var marker2: Marker

    private var isImageOneDisplayed = true
    private var areMarkersVisible = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        marker1 = Marker().apply {
            position = LatLng(37.586868,127.0313414)
            map = naverMap
            icon = OverlayImage.fromResource(R.drawable.spot)
        }

        marker2 = Marker().apply {
            position = LatLng(37.5843837,127.0274333)
            map = naverMap
            icon = OverlayImage.fromResource(R.drawable.spot)
        }

        var isBottomSheetVisible = false
        val bottomSheet = binding.bottomsheet1

        marker1.setOnClickListener {
            toggleBottomSheet(bottomSheet, isBottomSheetVisible).also {
                isBottomSheetVisible = !isBottomSheetVisible
            }
            true
        }

        marker2.setOnClickListener {
            toggleBottomSheet(bottomSheet, isBottomSheetVisible).also {
                isBottomSheetVisible = !isBottomSheetVisible
            }
            true
        }
    }

    private fun hideMarkers() {
        // 마커를 숨김
        marker1.map = null
        marker2.map = null
    }

    private fun showMarkers() {
        // 마커를 다시 보여줌
        marker1.map = naverMap
        marker2.map = naverMap
    }

    private fun toggleBottomSheet(view: View, isVisible: Boolean) {
        if (isVisible) {
            val slideDown = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
            view.startAnimation(slideDown)
            view.visibility = View.GONE
        } else {
            val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
            view.startAnimation(slideUp)
            view.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
