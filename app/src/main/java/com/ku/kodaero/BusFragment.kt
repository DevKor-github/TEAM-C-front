package com.ku.kodaero

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ku.kodaero.databinding.FragmentBusBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class BusFragment : Fragment(), OnMapReadyCallback {

    private lateinit var _binding: FragmentBusBinding
    private lateinit var naverMap: NaverMap
    private lateinit var busStopRepository: BusStopRepository

    private val initCameraPosition: LatLng = LatLng(37.59, 127.03)

    private val markers = mutableListOf<Marker>()
    private var clickedMarker: Marker? = null

    private var areMarkersVisible = true
    private var isImageOneDisplayed = true

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private var selectedBuildingName: String? = null
    private var selectedBuildingId: Int? = null

    private val pathOverlays = mutableListOf<PathOverlay>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        busStopRepository = BusStopRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusBinding.inflate(inflater, container, false)
        closeModal()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map_view, it).commit()
            }
        mapFragment.getMapAsync(this)

        setupBottomSheet()

        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding.searchButton.setOnClickListener {
            navigateToSearchBuildingFragment()
            closeModal()
        }

        _binding.searchRouteButton.setOnClickListener {
            navigateToGetDirectionsFragment()
            closeModal()
        }

        _binding.pinOnoffButton.setOnClickListener {
            if (areMarkersVisible) {
                hideMarkers()
                hideBusRoutes()
            } else {
                areMarkersVisible = !areMarkersVisible
                showMarkers()
                showBusRoutes()
                areMarkersVisible = !areMarkersVisible
            }
            areMarkersVisible = !areMarkersVisible

            if (isImageOneDisplayed) {
                _binding.pinOnoffButton.setImageResource(R.drawable.pin_on_button)
            } else {
                _binding.pinOnoffButton.setImageResource(R.drawable.pin_off_button)
            }
            isImageOneDisplayed = !isImageOneDisplayed

            closeModal()
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(_binding.busModal.standardBottomSheet)
        bottomSheetBehavior.peekHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 200f, resources.displayMetrics
        ).toInt()

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.peekHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, resources.displayMetrics).toInt()
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    clickedMarker?.let { resetMarker(it) }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Handle slide if needed
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(map: NaverMap) {
        naverMap = map

        naverMap.uiSettings.isLocationButtonEnabled = false
        naverMap.uiSettings.isZoomControlEnabled = false

        // LocationSource 설정
        val locationSource = FusedLocationSource(this, 1)
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow

        val cameraUpdate = CameraUpdate.scrollAndZoomTo(initCameraPosition, 14.3)
        naverMap.moveCamera(cameraUpdate)

        // 지도 제약 설정
        val latLngBounds = LatLngBounds(
            LatLng(37.57977937432624, 127.02283666260797), // 남서쪽 (SW) 좌표
            LatLng(37.60021922232589, 127.03716333739249)  // 북동쪽 (NE) 좌표
        )
        naverMap.setExtent(latLngBounds)

        // 줌 레벨 제한 설정
        naverMap.minZoom = 14.0

        displayBusStops()
        displayBusRoutes()

        naverMap.setOnMapClickListener { _, _ -> closeModal() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayBusStops() {
        val busStops = busStopRepository.getBusStops()
        busStops?.forEach { busStop ->
            val marker = Marker().apply {
                position = LatLng(busStop.location.latitude, busStop.location.longitude)
                icon = OverlayImage.fromResource(R.drawable.icon_shuttle_bus)
                map = naverMap
            }

            markers.add(marker)

            marker.setOnClickListener {
                if (clickedMarker == marker) {
                    resetMarker(marker)
                } else {
                    clickedMarker?.let { resetMarker(it) }
                    marker.icon = OverlayImage.fromBitmap(createCustomDrawableWithText("shuttle_bus", R.drawable.pin_shuttle_bus, busStop.name))
                    clickedMarker = marker

                    val cameraUpdate = CameraUpdate
                        .scrollAndZoomTo(marker.position, 15.0)
                        .animate(CameraAnimation.Fly)
                    naverMap.moveCamera(cameraUpdate)

                    showModal(busStop)
                }
                true
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayBusRoutes() {
        val busStops = busStopRepository.getBusStops()

        val combinedPathLatLngs = mutableListOf<LatLng>()

        busStops?.forEach { busStop ->
            combinedPathLatLngs.addAll(busStop.getPathNodeAsLatLng())
        }

        val path = PathOverlay().apply {
            this.coords = combinedPathLatLngs
            this.color = ContextCompat.getColor(requireContext(), R.color.shuttle_bus_path)
            this.width = 20
            this.outlineWidth = 5
            this.outlineColor = ContextCompat.getColor(requireContext(), R.color.red)
            this.map = naverMap
        }
        pathOverlays.add(path)
    }

    private fun hideBusRoutes() {
        pathOverlays.forEach { it.map = null }
    }

    private fun showBusRoutes() {
        pathOverlays.forEach { it.map = naverMap }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showModal(busStop: BusStop) {
        selectedBuildingName = busStop.name
        selectedBuildingId = busStop.place_id

        _binding.busModal.modalSheetBuildingName.text = busStop.name
        _binding.busModal.busModalStopName.text = busStop.name
        updateRecyclerViews(busStop)
        updateNextBusArrival(busStop)

        _binding.busModal.modalDepartButton.setOnClickListener {
            selectedBuildingName?.let { name ->
                selectedBuildingId?.let { id ->
                    putBuildingDirectionsFragment(true, name, "PLACE", id)
                }
            }
            closeModal()
        }

        _binding.busModal.modalArriveButton.setOnClickListener {
            selectedBuildingName?.let { name ->
                selectedBuildingId?.let { id ->
                    putBuildingDirectionsFragment(false, name, "PLACE", id)
                }
            }
            closeModal()
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun updateRecyclerViews(busStop: BusStop) {
        val hoursRange = 8..17
        hoursRange.forEach { hour ->
            val minutes = busStop.departure_times
                .filter { it.startsWith(String.format("%02d:", hour)) }
                .map { it.substring(3, 5) } // Extract minutes

            val recyclerViewId = resources.getIdentifier("bus_modal_timetable_${hour}_minute", "id", requireContext().packageName)
            val recyclerView = _binding.busModal.root.findViewById<RecyclerView>(recyclerViewId)

            recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = BusTimeAdapter(minutes)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateNextBusArrival(busStop: BusStop) {
        val currentTime = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

        val nextBusTimes = busStop.departure_times
            .map { LocalTime.parse(it, formatter) }
            .filter { it.isAfter(currentTime) }
            .take(2)

        val nextBusTextView: TextView = _binding.busModal.modalSheetNextArriveTime
        val nextNextBusTextView: TextView = _binding.busModal.modalSheetNextNextArriveTime

        if (nextBusTimes.isNotEmpty()) {
            nextBusTextView.text = "${nextBusTimes[0].format(DateTimeFormatter.ofPattern("HH:mm"))}"
        } else {
            nextBusTextView.text = "운행 종료"
            _binding.busModal.modalSheetNextArriveTimeText.visibility = View.GONE
        }

        if (nextBusTimes.size > 1) {
            nextNextBusTextView.text = "${nextBusTimes[1].format(DateTimeFormatter.ofPattern("HH:mm"))}"
        } else {
            nextNextBusTextView.text = ""
            _binding.busModal.modalSheetNextNextArriveTimeText.visibility = View.GONE
        }
    }

    private fun createCustomDrawableWithText(facilityType: String, drawableResId: Int, text: String): Bitmap {
        val originalBitmap = getDrawableAsBitmap(drawableResId)

        val paintStroke = Paint().apply {
            color = ContextCompat.getColor(requireContext(), android.R.color.white)
            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics)
            typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 3f
            textAlign = Paint.Align.CENTER
        }

        val colorResId = resources.getIdentifier(facilityType, "color", requireContext().packageName)
        val paintFill = Paint().apply {
            color = ContextCompat.getColor(requireContext(), colorResId)
            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics)
            typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
            isAntiAlias = true
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
        }

        val textWidth = paintFill.measureText(text)
        val bitmapWidth = maxOf(originalBitmap.width.toFloat(), textWidth).toInt()
        val bitmapWithText = Bitmap.createBitmap(
            bitmapWidth, originalBitmap.height + 10, Bitmap.Config.ARGB_8888
        ).apply { density = originalBitmap.density }

        val canvas = Canvas(bitmapWithText)
        canvas.drawBitmap(originalBitmap, (bitmapWidth - originalBitmap.width) / 2f, 30f, null)
        val x = bitmapWidth / 2f
        canvas.drawText(text, x, paintFill.textSize, paintStroke)
        canvas.drawText(text, x, paintFill.textSize, paintFill)

        return bitmapWithText
    }

    private fun getDrawableAsBitmap(drawableResId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableResId)!!
        return Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
    }

    private fun resetMarker(marker: Marker) {
        marker.icon = OverlayImage.fromResource(R.drawable.icon_shuttle_bus)
        clickedMarker = null
        closeModal()
    }

    fun isBottomSheetExpanded(): Boolean {
        val includedLayout = _binding.busModal.root
        val standardBottomSheet = includedLayout.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        return standardBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN
    }

    fun closeModal() {
        val includedLayout = _binding.busModal.root
        val standardBottomSheet = includedLayout.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
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

    private fun hideMarkers() {
        markers.forEach { it.map = null }
    }

    private fun showMarkers() {
         markers.forEach { it.map = naverMap }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val mainActivity = activity as? MainActivity
        mainActivity?.binding?.bottomNavigationView?.selectedItemId = R.id.fragment_home
    }
}
