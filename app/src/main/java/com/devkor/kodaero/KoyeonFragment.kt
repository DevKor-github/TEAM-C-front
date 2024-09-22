package com.devkor.kodaero

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.devkor.kodaero.databinding.FragmentKoyeonBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KoyeonFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentKoyeonBinding? = null
    private val binding get() = _binding!!

    private lateinit var naverMap: NaverMap
    private lateinit var viewModel: FetchDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKoyeonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToHomeButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)
        initMapView()

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


        viewLifecycleOwner.lifecycleScope.launch {


            withContext(Dispatchers.IO) {
                viewModel.fetchPubs()
            }?.forEach { pub ->
                addMarker(LatLng(pub.latitude, pub.longitude), pub.name)
            }

        }
    }


    private fun addMarker(position: LatLng, name: String) {
        Marker().apply {
            this.position = position
            this.icon = OverlayImage.fromResource(R.drawable.pin_koyeon)
            this.map = naverMap
            this.captionText = name
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
