package com.devkor.kodaero

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devkor.kodaero.databinding.FragmentBusBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.overlay.OverlayImage
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

        _binding.searchButton.setOnClickListener {
            navigateToSearchBuildingFragment()
            closeModal()
        }

        _binding.searchRouteButton.setOnClickListener {
            navigateToGetDirectionsFragment()
            closeModal()
        }

        setHorizontalScrollViewButtonListeners()

        _binding.pinOnoffButton.setOnClickListener {
            if (areMarkersVisible) {
                hideMarkers()
            } else {
                areMarkersVisible = !areMarkersVisible
                showMarkers()
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

        return _binding.root
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

        val cameraUpdate = CameraUpdate.scrollAndZoomTo(initCameraPosition, 14.3)
        naverMap.moveCamera(cameraUpdate)

        displayBusStops()

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
    private fun showModal(busStop: BusStop) {
        _binding.busModal.modalSheetBuildingName.text = busStop.name
        _binding.busModal.busModalStopName.text = busStop.name
        updateRecyclerViews(busStop)
        updateNextBusArrival(busStop)
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

    private fun setHorizontalScrollViewButtonListeners() {
        val buttonIds = listOf(
            _binding.cafeButton,
            _binding.cafeteriaButton,
            _binding.convenienceStoreButton,
            _binding.readingRoomButton,
            _binding.studyRoomButton,
            _binding.bookReturnMachineButton,
            _binding.loungeButton,
            _binding.waterPurifierButton,
            _binding.vendingMachineButton,
            _binding.printerButton,
            _binding.tumblerWasherButton,
            _binding.onestopAutoMachineButton,
            _binding.bankButton,
            _binding.smokingBoothButton,
            _binding.showerRoomButton,
            _binding.gymButton,
            _binding.sleepingRoomButton
        )

        for (button in buttonIds) {
            button.setOnClickListener {
                val idString = resources.getResourceEntryName(it.id)
                val keyword = idString.replace("_button", "").uppercase()
                navigateToPinSearchFragment(keyword)
            }
        }
    }

    private fun navigateToPinSearchFragment(keyword: String) {
        val currentCameraPosition = getCurrentCameraPosition()
        val currentZoomLevel = getCurrentZoomLevel()

        val pinSearchFragment = PinSearchFragment.newInstance(keyword, 0, currentCameraPosition, currentZoomLevel)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_container, pinSearchFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun getCurrentCameraPosition(): LatLng {
        return naverMap.cameraPosition.target
    }

    fun getCurrentZoomLevel(): Double {
        return naverMap.cameraPosition.zoom
    }

    private fun hideMarkers() {
        markers.forEach { it.map = null }
    }

    private fun showMarkers() {
         markers.forEach { it.map = naverMap }
    }

    override fun onPause() {
        super.onPause()

        val mainActivity = activity as? MainActivity
        mainActivity?.binding?.bottomNavigationView?.selectedItemId = R.id.fragment_home
    }
}
