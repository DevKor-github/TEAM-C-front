package com.example.deckor_teamc_front

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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

        // MainActivity의 onMapReady 코드를 여기에 추가
        val marker1 = Marker()
        marker1.position = LatLng(37.586868,127.0313414)
        marker1.map = naverMap
        marker1.icon = OverlayImage.fromResource(R.drawable.spot)

        val marker2 = Marker()
        marker2.position = LatLng(37.5843837,127.0274333)
        marker2.map = naverMap
        marker2.icon = OverlayImage.fromResource(R.drawable.spot)

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
