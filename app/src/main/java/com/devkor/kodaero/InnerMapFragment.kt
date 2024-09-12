package com.devkor.kodaero

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.devkor.kodaero.databinding.InnerMapContainerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.otaliastudios.zoom.OverPanRangeProvider
import com.otaliastudios.zoom.ZoomEngine
import java.io.IOException
import java.io.InputStream

class InnerMapFragment : Fragment(), CustomScrollView.OnFloorSelectedListener {
    companion object {
        @JvmStatic
        fun newInstance(
            selectedBuildingName: String,
            selectedBuildingAboveFloor: Int,
            selectedBuildingUnderFloor: Int,
            selectedBuildingId: Int
        ) =
            InnerMapFragment().apply {
                arguments = Bundle().apply {
                    putString("selectedBuildingName", selectedBuildingName)
                    putInt("selectedBuildingAboveFloor", selectedBuildingAboveFloor)
                    putInt("selectedBuildingUnderFloor", selectedBuildingUnderFloor)
                    putInt("selectedBuildingId", selectedBuildingId)
                }
            }

        @JvmStatic
        fun newInstanceFromSearch(
            selectedBuildingName: String,
            selectedBuildingAboveFloor: Int,
            selectedBuildingUnderFloor: Int,
            selectedBuildingId: Int,
            selectedRoomFloor: Int,
            selectedRoomMask: Int,
            hasDirection: Boolean
        ) =
            InnerMapFragment().apply {
                arguments = Bundle().apply {
                    putString("selectedBuildingName", selectedBuildingName)
                    putInt("selectedBuildingAboveFloor", selectedBuildingAboveFloor)
                    putInt("selectedBuildingUnderFloor", selectedBuildingUnderFloor)
                    putInt("selectedBuildingId", selectedBuildingId)
                    putInt("selectedRoomFloor", selectedRoomFloor)
                    putInt("selectedRoomMask", selectedRoomMask)
                    putBoolean("hasDirection", hasDirection)
                }
            }
    }

    private var _binding: InnerMapContainerBinding? = null
    val binding get() = _binding!!

    private lateinit var viewModel: FetchDataViewModel

    private var selectedBuildingId: Int = 0
    private var selectedBuildingName: String? = null
    private var selectedBuildingAboveFloor: Int? = null
    private var selectedBuildingUnderFloor: Int? = null

    private var innermapCurrentFloor: Int = 1

    // 초기 colorMap
    private var colorMap = mutableMapOf<Int, String>()

    private var isScrollVisible = true

    private var floorRoomList: List<RoomList> = emptyList()

    private lateinit var modalView: CoordinatorLayout

    private lateinit var standardBottomSheet: FrameLayout
    private lateinit var standardBottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var customScrollView: CustomScrollView
    private lateinit var customScrollViewLayout: FrameLayout

    private var searchedFloor: Int = 1
    private var searchedMask: Int = 0

    private var searchedRoute: RouteResponse? = null

    private var hasDirection: Boolean = false

    private lateinit var routeView: RouteView

    private var currentRouteIndex = 0
    private var routeResponse: RouteResponse? = null

    private var pendingRouteInfo: String? = null

    private var currentBuildingId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedBuildingName = it.getString("selectedBuildingName")
            selectedBuildingAboveFloor = it.getInt("selectedBuildingAboveFloor")
            selectedBuildingUnderFloor = it.getInt("selectedBuildingUnderFloor")
            selectedBuildingId = it.getInt("selectedBuildingId")

            // Initialize currentBuildingId with selectedBuildingId
            currentBuildingId = selectedBuildingId

            searchedFloor = it.getInt("selectedRoomFloor")
            searchedMask = it.getInt("selectedRoomMask")

            hasDirection = it.getBoolean("hasDirection")

            if (searchedFloor != 0) innermapCurrentFloor = searchedFloor
        }
        // JSON 파일에서 데이터 읽어오기
        readJsonAndUpdateColorMap()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InnerMapContainerBinding.inflate(inflater, container, false)

        modalView = binding.includedModal.root

        standardBottomSheet =
            binding.includedModal.root.findViewById(R.id.standard_bottom_sheet)
        standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)

        customScrollView = binding.customScrollView
        customScrollViewLayout = binding.customScrollViewLayout

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)
        observeViewModel()

        routeView = binding.includedMap.routeView

        if (selectedBuildingId != 0 && innermapCurrentFloor != 0) {
            // Valid values
            initInnermap()
            replaceInnermapMask()
        } else {
            Log.e("InnerMapFragment", "Invalid buildingId or floor: $selectedBuildingId, $innermapCurrentFloor")
        }

        if (hasDirection) {
            try {
                searchedRoute = DirectionSearchRouteDataHolder.splitedRoute
                Log.e("InnerMapFragment", "searchedRoute is set successfully ${searchedRoute}")
            } catch (e: UninitializedPropertyAccessException) {
                Log.e("InnerMapFragment", "splitedRoute is not initialized: ${e.message}")
            }
        }

        drawRouteForCurrentFloor(routeView, searchedRoute, currentBuildingId, innermapCurrentFloor)

        Log.e("InnerMapFragment", "${innermapCurrentFloor} Floor")
        onFloorSelected(innermapCurrentFloor)

        val layerName = colorMap[searchedMask] // 선택된 마스크를 레이어명으로 변환
        if (layerName != null && !hasDirection) {
            Log.d("InnerMapFragment", "Filename for id $searchedMask: $layerName")
            replaceInnermap(layerName) // 검색에서 진입 할 시 장소의 색상을 변경하는 로직

            val touchHandler = InnerMapTouchHandler(
                context = requireContext(),
                imageView = ImageView(context), // 임시 이미지
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), // 임시 비트맵
                colorMap = colorMap,
                innerMapBinding = binding,
                floor = innermapCurrentFloor,
                buildingId = selectedBuildingId,
                replaceInnermapCallback = { groupId ->
                    replaceInnermap(groupId)
                }
            )
            touchHandler.openInnermapModal(searchedMask)
        }


        val buildingNameTextView = binding.buildingName
        buildingNameTextView.text = selectedBuildingName

        selectedBuildingAboveFloor?.let { aboveFloor ->
            selectedBuildingUnderFloor?.let { underFloor ->
                if(!hasDirection) {
                    customScrollView.setFloors(-underFloor, aboveFloor, innermapCurrentFloor)
                }
                else(hideScroll())
            }
        }
        customScrollView.setOnFloorSelectedListener(this)

        Log.d("InnerMapFragment", "Selected Building Name: $selectedBuildingName")
        Log.d("InnerMapFragment", "Selected Building Above Floor: $selectedBuildingAboveFloor")
        Log.d("InnerMapFragment", "Selected Building Under Floor: $selectedBuildingUnderFloor")
        Log.d("InnerMapFragment", "Selected Building Id: $selectedBuildingId")

        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        standardBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {}
                    BottomSheetBehavior.STATE_EXPANDED -> hideScroll()
                    BottomSheetBehavior.STATE_HIDDEN -> showScroll()
                    BottomSheetBehavior.STATE_SETTLING -> hideScroll()
                    else -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val pinOnoffButton: ImageButton = binding.pinOnoffButton
        pinOnoffButton.setOnClickListener {
            // Toggle scroll visibility
            if (isScrollVisible) {
                hideScroll()
                pinOnoffButton.setImageResource(R.drawable.pin_on_button)
            } else {
                showScroll()
                pinOnoffButton.setImageResource(R.drawable.pin_off_button)
            }
        }
        binding.backToHomeButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Check if hasDirection is true
        if (hasDirection) {
            customScrollView.isClickable = false
            customScrollView.isFocusable = false
            customScrollView.isEnabled = false

            // Set get_directions_route_guide_layout to visible
            binding.getDirectionsGuideLayout.visibility = View.VISIBLE
        } else {
            customScrollView.isClickable = true
            customScrollView.isFocusable = true
            customScrollView.isEnabled = true

            // Set get_directions_route_guide_layout to gone (or invisible if needed)
            binding.getDirectionsGuideLayout.visibility = View.GONE
        }

        enableVerticalOverScroll()
    }

    // Set route info to the UI
    fun setRouteInfo(info: String, routeIndex: Int) {
        if (_binding != null) {
            // UI가 준비된 경우 직접 접근
            binding.getDirectionsGuideInout.text = "실내"
            binding.getDirectionsGuideInfo.text = info

            // Set the guide number based on routeIndex
            binding.getDirectionsGuideNumber.text = (routeIndex + 1).toString()

            when {
                info.contains("층으로", ignoreCase = true) -> {
                    binding.toNextGuideButton.setImageResource(R.drawable.move_floor_button)
                }
                info.contains("나가세요", ignoreCase = true) -> {
                    binding.toNextGuideButton.setImageResource(R.drawable.move_outside_button)
                }
                info.contains("들어가세요", ignoreCase = true) -> {
                    binding.toNextGuideButton.setImageResource(R.drawable.move_inside_button)
                }
            }

            if (info == "도착") {
                binding.toNextGuideButton.visibility = View.GONE
            } else {
                binding.toNextGuideButton.visibility = View.VISIBLE
            }
        } else {
            // UI가 준비되지 않은 경우 정보를 저장
            pendingRouteInfo = info
        }
    }

    private fun fetchData() {

        Log.d("aaaant", "Room: $selectedBuildingId $innermapCurrentFloor")
        selectedBuildingId?.let {
            innermapCurrentFloor?.let { it1 ->
                viewModel.fetchRoomList(
                    it,
                    it1
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.roomList.observe(viewLifecycleOwner, Observer { roomList ->
            if (roomList.isNotEmpty()) {
                // roomList를 로그로 출력
                Log.d("InnerMapFragment", "Room: $roomList")
                floorRoomList = roomList
                updateFacilityButtonsVisibility(floorRoomList)
                updateTouchHandler()
            } else {
                Log.e("InnerMapFragment", "Room list is empty")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        // 화면이 꺼질 때 실행하고 싶은 함수 호출
        closeModal()
    }

    // BottomSheet의 확장 상태를 확인하는 메서드
    fun isBottomSheetExpanded(): Boolean {
        return ::standardBottomSheetBehavior.isInitialized &&
                standardBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN
    }

    fun closeModal() {
        val includedLayout = binding.includedModal.root
        val standardBottomSheet =
            includedLayout.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun readJsonAndUpdateColorMap() {
        try {
            val assetManager = requireContext().assets
            Log.d("InnerMapFragment", "Asset Manager Initialized")

            val inputStream: InputStream =
                assetManager.open("${selectedBuildingId}/${innermapCurrentFloor}/data.json")
            Log.d("InnerMapFragment", "Opened data.json file")

            val jsonString = inputStream.bufferedReader().use { it.readText() }
            Log.d("InnerMapFragment", "JSON data: $jsonString")

            val areaDataType = object : TypeToken<List<List<Any>>>() {}.type
            val areas: List<List<Any>> = Gson().fromJson(jsonString, areaDataType)
            Log.d("InnerMapFragment", "Parsed JSON data successfully")

            areas.forEach { area ->
                val svgFile = area[0] as String
                val id = (area[1] as Double).toInt() // Gson이 숫자를 Double로 파싱하기 때문에 Int로 변환
                colorMap[id] = svgFile
                Log.d("InnerMapFragment", "Added to colorMap: id=$id, svgFile=$svgFile")
            }

        } catch (e: IOException) {
            Log.e("InnerMapFragment", "Error reading JSON file: ${e.message}")
        } catch (e: Exception) {
            Log.e("InnerMapFragment", "Error parsing JSON file: ${e.message}")
        }
    }

    override fun onFloorSelected(floor: Int) {
        Log.e("innermapfragment", "Now on $floor in building $currentBuildingId")
        if (floor == 0) return

        innermapCurrentFloor = floor
        fetchData()
        readJsonAndUpdateColorMap()
        replaceInnermap()
        replaceInnermapMask()
        updateTouchHandler()

        // Draw the route for the current building and floor
        drawRouteForCurrentFloor(routeView, searchedRoute, currentBuildingId, innermapCurrentFloor)

        Log.d(
            "InnerMapFragment",
            "Floor selected: $floor, innermapCurrentFloor updated: $innermapCurrentFloor"
        )
    }


    private fun replaceInnermapMask() {
        val resourceName = "${selectedBuildingId}/${innermapCurrentFloor}/final_overlay.png"
        try {
            // 리소스가 assets 폴더에 있는지 확인하고 로드
            val assetManager = requireContext().assets
            val inputStream: InputStream = assetManager.open(resourceName)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            binding.includedMap.innermapMask.setImageBitmap(bitmap)
            Log.d("InnerMapFragment", "Bitmap replaced with resource: $resourceName")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("InnerMapFragment", "Resource not found: $resourceName")
        }
    }
    private fun initInnermap(): String? {
        val resourceName = "${selectedBuildingId}/${innermapCurrentFloor}/inner_map.svg"

        return try {
            // 리소스가 assets 폴더에 있는지 확인하고 로드
            val assetManager = requireContext().assets
            val inputStream: InputStream = assetManager.open(resourceName)
            val svgContent = inputStream.bufferedReader().use { it.readText() }

            // **여기서 SVGExternalFileResolver를 설정합니다.** 폰트 임포트
            val fontResolver = SVGFontResolver(requireContext())
            SVG.registerExternalFileResolver(fontResolver)

            // SVG 내용에서 글꼴과 색상 기본값으로 변경
            val modifiedSvgContent = updateSvgFont(svgContent, "Pretendard", "#424242")

            // 변경된 SVG 콘텐츠 반환
            modifiedSvgContent
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("InnerMapFragment", "Resource not found: $resourceName")
            null
        } catch (e: SVGParseException) {
            e.printStackTrace()
            Log.e("InnerMapFragment", "Error parsing SVG: ${e.message}")
            null
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("InnerMapFragment", "Unexpected error: ${e.message}")
            null
        }
    }

    fun replaceInnermap(groupIdToChange: String? = null) {
        if (initInnermap() != null){
            // `initInnermap` 함수에서 수정된 SVG 콘텐츠를 가져옴
            val modifiedSvgContent = initInnermap()
            var svg = SVG.getFromString(modifiedSvgContent)
            if (modifiedSvgContent != null) {
                try {
                    // groupIdToChange가 border나 other이 아니면 해당 그룹의 fill 색상을 변경
                    if (groupIdToChange != null) {
                        if (!groupIdToChange.contains(Regex(".*(border|other).*", RegexOption.IGNORE_CASE))) {
                            val remodifiedSvgContent = groupIdToChange?.let {
                                changeElementFillColor(modifiedSvgContent, it)
                            } ?: modifiedSvgContent

                            svg = SVG.getFromString(remodifiedSvgContent)
                        }
                    }

                    // SVG를 PictureDrawable로 렌더링하여 ImageView에 설정
                    val drawable = PictureDrawable(svg.renderToPicture())
                    binding.includedMap.innermap.setImageDrawable(drawable)
                    Log.d("InnerMapFragment", "Resource replaced successfully")
                } catch (e: SVGParseException) {
                    e.printStackTrace()
                    Log.e("InnerMapFragment", "Error parsing SVG: ${e.message}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("InnerMapFragment", "Unexpected error: ${e.message}")
                }
            } else {
                Log.e("InnerMapFragment", "Failed to initialize innermap")
            }
        }
        try{
            drawRouteForCurrentFloor(routeView, searchedRoute, currentBuildingId, innermapCurrentFloor)
        }
        catch (e: Exception){
            Log.e("InnerMapFragment", "Route is null")
        }
    }

    // SVG 강의 실 클릭 시 색변화 글씨 변화 주는 함수
    // 리팩토링 완


    private fun changeElementFillColor(svgContent: String, elementId: String): String {
        val TAG = "SVG_DEBUG" // 로그 태그 설정
        val groupRegex = Regex("""(<g[^>]*id="$elementId"[^>]*>.*?</g>)""", RegexOption.DOT_MATCHES_ALL)

        return svgContent.replace(groupRegex) { groupMatchResult ->
            var groupContent = groupMatchResult.value

            Log.e(TAG, "Original Group Content:\n$groupContent")

            // rect 및 polygon 태그에 style 속성 추가
            groupContent = groupContent.replace(Regex("""<(rect|polygon|path|circle)([^/>]*)(/?)>""")) { matchResult ->
                val tag = matchResult.groupValues[1]
                var attributes = matchResult.groupValues[2]
                val closingSlash = matchResult.groupValues[3] // Optional closing slash

                // 애기능 예외처리: fill="#424142" -> fill="#FFFFFF"으로 변경, 다른 스타일 추가 안함
                if (attributes.contains("""fill="#424142"""")) {
                    attributes = attributes.replace("""fill="#424142"""", """fill="#FFFFFF"""")
                    val newTag = if (closingSlash.isNotEmpty()) {
                        """<$tag$attributes$closingSlash>"""
                    } else {
                        """<$tag$attributes>"""
                    }
                    Log.e(TAG, "Modified rect/polygon Tag (fill #424142 exception): $newTag")
                    newTag
                } else {
                    // 다른 경우 스타일 속성 추가
                    val newTag = if (closingSlash.isNotEmpty()) {
                        """<$tag$attributes style="fill:#F85C5C"$closingSlash>"""
                    } else {
                        """<$tag$attributes style="fill:#F85C5C">"""
                    }
                    Log.e(TAG, "Modified rect/polygon Tag: $newTag")
                    newTag
                }
            }

            // text 및 tspan 태그에 style 속성 추가
            groupContent = groupContent.replace(Regex("""<(text|tspan)([^/>]*)(/?)>""")) { matchResult ->
                val tag = matchResult.groupValues[1]
                val attributes = matchResult.groupValues[2]
                val closingSlash = matchResult.groupValues[3] // Optional closing slash
                val newTag = if (closingSlash.isNotEmpty()) {
                    // 슬래시 앞에 스타일 속성을 추가하고 슬래시와 함께 태그를 닫음
                    """<$tag$attributes style="fill:#FFFFFF"/>"""
                } else {
                    // 꺾쇠 앞에 스타일 속성을 추가하고 태그를 닫음
                    """<$tag$attributes style="fill:#FFFFFF">"""
                }
                Log.e(TAG, "Modified text/tspan Tag: $newTag")
                newTag

            }

            Log.e(TAG, "Final Group Content:\n$groupContent")
            groupContent
        }
    }

    private fun updateTouchHandler() {

        if (!hasDirection){
            Log.e("e", "$floorRoomList")
            val imageView: ImageView = binding.includedMap.innermapMask
            val filePath = "${selectedBuildingId}/${innermapCurrentFloor}/final_overlay.png"
            val assetManager = context?.assets

            var bitmap: Bitmap? = null
            var inputStream: InputStream? = null

            try {
                inputStream = assetManager?.open(filePath)
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                Log.e("InnerMapFragment", "Error loading bitmap from file: $filePath", e)
            } finally {
                inputStream?.close()
            }

            if (bitmap == null) {
                Log.e("InnerMapFragment", "Bitmap could not be loaded, skipping touch handler setup")
                return
            }

            try {
                val touchHandler = InnerMapTouchHandler(
                    context = requireContext(),
                    imageView = imageView,
                    bitmap = bitmap,
                    colorMap = colorMap,
                    innerMapBinding = binding,
                    floor = innermapCurrentFloor,
                    buildingId = selectedBuildingId,
                    replaceInnermapCallback = { groupId ->
                        replaceInnermap(groupId)
                    }
                )
                imageView.setOnTouchListener(touchHandler)
            } catch (e: Exception) {
                Log.e("InnerMapFragment", "Error creating touch handler: ${e.message}")
            }
        } else {
            return
        }
    }


    private fun showScroll() {
        customScrollViewLayout.visibility = View.VISIBLE
        customScrollView.visibility = View.VISIBLE
        isScrollVisible = true
    }

    private fun hideScroll() {
        customScrollViewLayout.visibility = View.GONE
        customScrollView.visibility = View.GONE
        isScrollVisible = false
    }

    private fun drawRouteForCurrentFloor(
        routeView: RouteView,
        searchedRoute: RouteResponse?,
        currentBuildingId: Int,
        innermapCurrentFloor: Int
    ) {
        searchedRoute?.let { routeResponse ->
            // Filter routes by the current building and floor
            val matchingRoutes = routeResponse.path.filter {
                it.floor == innermapCurrentFloor && it.buildingId == currentBuildingId
            }

            // Extract coordinates from the filtered routes
            val coordinates = matchingRoutes.flatMap { route ->
                route.route.map { Pair(it[0].toFloat(), it[1].toFloat()) }
            }

            // Debugging: log the coordinates
            Log.d("RouteDebug", "Coordinates for building $currentBuildingId, floor $innermapCurrentFloor: $coordinates")

            // Set the route coordinates for the RouteView
            routeView.setRouteCoordinates(coordinates)
        } ?: run {
            // Handle the case where searchedRoute is null
            Log.e("RouteDebug", "searchedRoute is null")
        }
    }

    private fun updateSvgFont(svgContent: String, fontFamily: String = "Pretendard", fillColor: String = "#424242"): String {


        // opacity 속성 제거 (다양한 표현 방식을 고려)
        val opaqueSvgContent = svgContent.replace(Regex("""\bopacity\s*[:=]\s*['"]?\s*\d*\.?\d+\s*['"]?;?""", RegexOption.IGNORE_CASE), "")

        val style = """style="font-family:'$fontFamily'; fill:$fillColor;""""
        // <text> 및 <tspan> 태그에 스타일 속성 추가
        val updatedContent = opaqueSvgContent.replace(Regex("""<(text|tspan)([^/>]*)(/?)>""")) { matchResult ->
            val tag = matchResult.groupValues[1]
            val attributes = matchResult.groupValues[2]
            val closingSlash = matchResult.groupValues[3] // Optional closing slash

            // 슬래시가 있는 경우와 없는 경우를 처리
            if (closingSlash.isNotEmpty()) {
                """<$tag$attributes $style/>"""
            } else {
                """<$tag$attributes $style>"""
            }
        }

        return updatedContent
    }


    private fun enableVerticalOverScroll() {
        binding.includedMap.zoomLayout.setOverScrollHorizontal(true)
        binding.includedMap.zoomLayout.setOverScrollVertical(true)
        binding.includedMap.zoomLayout.setOverPanRange(object : OverPanRangeProvider {
            override fun getOverPan(engine: ZoomEngine, horizontal: Boolean): Float {
                return if (horizontal) {
                    1000f // 가로 오버팬 범위
                } else {
                    1000f // 세로 오버팬 범위
                }
            }
        })

    }

    private fun updateFacilityButtonsVisibility(floorRoomList: List<RoomList>) {
        // facilityType에 따라 대응하는 버튼을 ViewBinding으로 맵핑
        val facilityButtonMap = mapOf(
            "CAFE" to _binding!!.cafeButton,
            "CAFETERIA" to _binding!!.cafeteriaButton,
            "CONVENIENCE_STORE" to _binding!!.convenienceStoreButton,
            "READING_ROOM" to _binding!!.readingRoomButton,
            "STUDY_ROOM" to _binding!!.studyRoomButton,
            "LOUNGE" to _binding!!.loungeButton,
            "WATER_PURIFIER" to _binding!!.waterPurifierButton,
            "PRINTER" to _binding!!.printerButton,
            "VENDING_MACHINE" to _binding!!.vendingMachineButton,
            "SMOKING_BOOTH" to _binding!!.smokingAreaButton,
            "SLEEPING_ROOM" to _binding!!.sleepingRoomButton,
            "BOOK_RETURN_MACHINE" to _binding!!.bookReturnMachineButton
        )

        // 버튼 모두 GONE으로 설정
        facilityButtonMap.values.forEach { button ->
            button.visibility = View.GONE
            Log.e("qqqqqqqqqqqqqqqqq","sssss")
        }

        // roomList의 facilityType에 해당하는 버튼만 VISIBLE로 설정
        Log.e("qqqqqqqqqqqqqqqqq","$floorRoomList")
        floorRoomList.map { it.placeType }  // 각 room의 facilityType을 추출
            .distinct()  // 중복된 facilityType을 제거
            .forEach { placeType ->
                Log.e("qqqqqqqqqqqqqqqqq","$placeType")
                facilityButtonMap[placeType]?.visibility = View.VISIBLE  // 해당하는 버튼을 VISIBLE로 설정
            }
    }


}