package com.example.deckor_teamc_front

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.deckor_teamc_front.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

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

    private var startLatLng: LatLng? = null
    private var arrivalLatLng: LatLng? = null
    private var stopTracking = false
    private var cameraPosition: LatLng? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
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
        }

        if (!hasPermission()) {
            requestPermissions(PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            initMapView()
        }

        val pinOnoffButton: ImageButton = binding.pinOnoffButton
        pinOnoffButton.setOnClickListener {
            if (areMarkersVisible) {
                hideMarkers()
            } else {
                showMarkers()
            }
            areMarkersVisible = !areMarkersVisible

            if (isImageOneDisplayed) {
                pinOnoffButton.setImageResource(R.drawable.pin_on_button)
            } else {
                pinOnoffButton.setImageResource(R.drawable.pin_off_button)
            }
            isImageOneDisplayed = !isImageOneDisplayed
        }

        startLatLng = arguments?.getParcelable("start_lat_lng")
        arrivalLatLng = arguments?.getParcelable("arrival_lat_lng")
        stopTracking = arguments?.getBoolean("stop_tracking") ?: false
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

        if (stopTracking) {
            naverMap.locationTrackingMode = LocationTrackingMode.None
        } else {
            naverMap.locationTrackingMode = LocationTrackingMode.Follow
        }

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
        var selectedBuilding = 0

        standardBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> { }
                    BottomSheetBehavior.STATE_EXPANDED -> { }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        selectedBuilding = 0
                    }
                    else -> { }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        val innerMapButton = includedLayout.findViewById<Button>(R.id.modal_innermap_button)
        val layout1 = (binding.root).findViewById<ConstraintLayout>(R.id.fragment_home)

        marker1 = Marker().apply {
            position = LatLng(37.586868, 127.0313414)
            map = naverMap
            icon = OverlayImage.fromResource(R.drawable.spot)
        }

        marker2 = Marker().apply {
            position = LatLng(37.5843837, 127.0274333)
            map = naverMap
            icon = OverlayImage.fromResource(R.drawable.spot)
        }

        marker1.setOnClickListener {
            if (selectedBuilding != 1) {
                buildingName.text = getString(R.string.building_name2)
                standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            selectedBuilding = 1
            true
        }

        marker2.setOnClickListener {
            if (selectedBuilding != 2) {
                buildingName.text = getString(R.string.building_name)
                standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            selectedBuilding = 2
            true
        }

        innerMapButton.setOnClickListener {
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

            val layoutInflater = LayoutInflater.from(requireContext())
            val layout2 = layoutInflater.inflate(R.layout.inner_maps, null)

            val layout3 = layout1.findViewById<FrameLayout>(R.id.map_view)
            val parent = layout3.parent as ViewGroup
            val index = parent.indexOfChild(layout3)
            parent.removeViewAt(index)
            parent.addView(layout2, index, layout3.layoutParams)
        }

        if (startLatLng != null && arrivalLatLng != null) {
            fetchAndDrawPath(startLatLng!!, arrivalLatLng!!)
        }
    }

    private fun hideMarkers() {
        marker1.map = null
        marker2.map = null
    }

    private fun showMarkers() {
        marker1.map = naverMap
        marker2.map = naverMap
    }

    private fun fetchAndDrawPath(startLatLng: LatLng, arrivalLatLng: LatLng) {
        val client = OkHttpClient()
        val url = "http://3.34.68.172:8080/api/outer-route?startLong=${startLatLng.longitude}&startLat=${startLatLng.latitude}&endLong=${arrivalLatLng.longitude}&endLat=${arrivalLatLng.latitude}"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        try {
                            val jsonResponse = JSONObject(it)
                            val route = jsonResponse.getJSONObject("data").getJSONArray("route")
                            val pathCoords = mutableListOf<LatLng>()

                            for (i in 0 until route.length()) {
                                val point = route.getJSONArray(i)
                                val lat = point.getString(0).toDouble()
                                val lng = point.getString(1).toDouble()
                                pathCoords.add(LatLng(lat, lng))
                            }

                            pathCoords.add(0, startLatLng)
                            pathCoords.add(arrivalLatLng)

                            activity?.runOnUiThread {
                                drawPath(pathCoords)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    response.body?.let {
                        val errorResponse = it.string()
                        println("Error response: $errorResponse")
                    }
                }
            }
        })
    }

    private fun drawPath(pathCoords: List<LatLng>) {
        val path = PathOverlay()
        path.coords = pathCoords
        path.color = ContextCompat.getColor(requireContext(), R.color.red)
        path.map = naverMap
        path.outlineWidth = 0
        path.width = 20

        val midPoint = pathCoords[pathCoords.size / 2]
        val cameraUpdate = CameraUpdate.scrollAndZoomTo(midPoint, 15.0)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun navigateToSearchBuildingFragment() {
        val searchBuildingFragment = SearchBuildingFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(R.id.main_container, searchBuildingFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun navigateToGetDirectionsFragment() {
        val getDirectionsFragment = GetDirectionsFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_container, getDirectionsFragment)
        transaction.addToBackStack(null)
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
}
