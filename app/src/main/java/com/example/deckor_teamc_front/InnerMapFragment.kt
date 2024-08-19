package com.example.deckor_teamc_front

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
import com.example.deckor_teamc_front.databinding.InnerMapContainerBinding
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
                customScrollView.setFloors(-underFloor, aboveFloor, innermapCurrentFloor)
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
        if(initInnermap() != null){
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
    // 심연이니 건드리지 마시오
    private fun changeElementFillColor(svgContent: String, elementId: String): String {
        val groupRegex = Regex("""(<g[^>]*id="$elementId"[^>]*>.*?</g>)""", RegexOption.DOT_MATCHES_ALL)

        return svgContent.replace(groupRegex) { groupMatchResult ->
            var groupContent = groupMatchResult.value

            // 기존 fill 속성 변경, fill="#424142"는 제외
            groupContent = groupContent.replace(Regex("""fill="#[0-9A-Fa-f]{3,6}"""")) { matchResult ->
                if (matchResult.value == """fill="#424142"""") {
                    matchResult.value  // 그대로 유지(애기능 생활관 예외처리)
                } else {
                    """fill="#F85C5C""""  // 다른 색상은 변경
                }
            }

            // 기존 글씨 fill 속성 변경
            groupContent = groupContent.replace(Regex("""<text[^>]*fill="#[0-9A-Fa-f]{3,6}"""")) { matchResult ->
                matchResult.value.replace(
                    Regex("""fill="#[0-9A-Fa-f]{3,6}""""),
                    """fill="#FFFFFF""""
                )
            }

            // fill="#424142"를 모두 fill="#FFFFFF"로 변경(애기능 생활관 예외처리)
            groupContent = groupContent.replace("""fill="#424142"""", """fill="#FFFFFF"""")

            groupContent



        // 1. <text> 및 <tspan> 태그 처리
            val textAndTspanRegex = Regex("""<(text|tspan)[^>]*>.*?</(text|tspan)>""", RegexOption.DOT_MATCHES_ALL)
            groupContent = groupContent.replace(textAndTspanRegex) { elementMatch ->
                var elementContent = elementMatch.value

                // 클래스 추출 및 스타일 적용
                val classRegex = Regex("""class="([^"]+)"""")
                val classMatch = classRegex.find(elementContent)

                if (classMatch != null) {
                    val originalClass = classMatch.groupValues[1]
                    val styleRegex = Regex("""<style\b[^>]*>(.*?)</style>""", RegexOption.DOT_MATCHES_ALL)
                    val styleMatch = styleRegex.find(svgContent)

                    val combinedStyles = mutableMapOf<String, String>()

                    if (styleMatch != null) {
                        val styleContent = styleMatch.groupValues[1]

                        // .cls-3 등 클래스를 포함하는 모든 스타일 블록을 찾는 정규식
                        val classStyleRegex = Regex("""\.([^\s,{}]+(?:\s*,\s*[^\s,{}]+)*)\s*\{([^}]+)\}""")
                        val classStyleMatches = classStyleRegex.findAll(styleContent)

                        // 해당 클래스에 대한 스타일 추출
                        for (match in classStyleMatches) {
                            val classNames = match.groupValues[1].split(",").map { it.trim().removePrefix(".") }
                            val styleProperties = match.groupValues[2].trim()

                            if (classNames.contains(originalClass)) {
                                val propertiesList = styleProperties.split(";").map { it.trim() }.filter { it.isNotEmpty() }

                                for (property in propertiesList) {
                                    if (combinedStyles.containsKey(originalClass)) {
                                        combinedStyles[originalClass] = combinedStyles[originalClass] + " " + property + ";"
                                    } else {
                                        combinedStyles[originalClass] = property + ";"
                                    }
                                }
                            }
                        }
                    }

                    // 병합된 스타일을 인라인 속성으로 text 및 tspan 요소에 적용
                    val inlineStyle = combinedStyles.flatMap { (attrName, attrValue) ->
                        attrValue.split(";").map { it.trim() }.filter { it.isNotEmpty() }.map { property ->
                            val (key, value) = property.split(":").map { it.trim() }
                            key to value
                        }
                    }.joinToString(" ") { (key, value) ->
                        """$key="$value""""
                    }

                    // fill 속성을 흰색으로 변경하고 인라인 스타일에 추가
                    elementContent = elementContent
                        .replace(classRegex, "")
                        .replace("<${elementMatch.groupValues[1]}", """<${elementMatch.groupValues[1]} $inlineStyle fill="#FFFFFF"""")
                }
                elementContent
            }

            // 2. <rect> 및 <polygon> 태그 처리
            val shapeRegex = Regex("""<(rect|polygon)[^>]*>""")
            groupContent = groupContent.replace(shapeRegex) { shapeMatch ->
                var shapeContent = shapeMatch.value

                val classRegex = Regex("""class="([^"]+)"""")
                val classMatch = classRegex.find(shapeContent)

                if (classMatch != null) {
                    val originalClass = classMatch.groupValues[1]
                    val styleRegex = Regex("""<style\b[^>]*>(.*?)</style>""", RegexOption.DOT_MATCHES_ALL)
                    val styleMatch = styleRegex.find(svgContent)

                    val combinedStyles = mutableMapOf<String, String>()

                    if (styleMatch != null) {
                        val styleContent = styleMatch.groupValues[1]

                        // .cls-6을 포함하는 모든 스타일 블록을 찾는 정규식
                        val classStyleRegex = Regex("""\.([^\s,{}]+(?:\s*,\s*[^\s,{}]+)*)\s*\{([^}]+)\}""")
                        val classStyleMatches = classStyleRegex.findAll(styleContent)

                        // 해당 클래스에 대한 스타일 추출
                        for (match in classStyleMatches) {
                            val classNames = match.groupValues[1].split(",").map { it.trim().removePrefix(".") }
                            val styleProperties = match.groupValues[2].trim()

                            if (classNames.contains(originalClass)) {
                                val propertiesList = styleProperties.split(";").map { it.trim() }.filter { it.isNotEmpty() }

                                for (property in propertiesList) {
                                    if (combinedStyles.containsKey(originalClass)) {
                                        combinedStyles[originalClass] = combinedStyles[originalClass] + " " + property + ";"
                                    } else {
                                        combinedStyles[originalClass] = property + ";"
                                    }
                                }
                            }
                        }
                    }

                    // 병합된 스타일을 인라인 속성으로 rect 및 polygon 요소에 적용
                    val inlineStyle = combinedStyles.flatMap { (attrName, attrValue) ->
                        attrValue.split(";").map { it.trim() }.filter { it.isNotEmpty() }.map { property ->
                            val (key, value) = property.split(":").map { it.trim() }
                            key to value
                        }
                    }.joinToString(" ") { (key, value) ->
                        """$key="$value""""
                    }

                    // fill 속성을 지정된 색상으로 변경하고 인라인 스타일에 추가
                    shapeContent = shapeContent
                        .replace(classRegex, "")
                        .replace("<${shapeMatch.groupValues[1]}", """<${shapeMatch.groupValues[1]} $inlineStyle fill="#F85C5C"""")
                }
                shapeContent
            }

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
        customScrollView.visibility = View.VISIBLE
        isScrollVisible = true
    }

    private fun hideScroll() {
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

    private fun updateTextTagContent(svgContent: String, targetFontFamily: String, targetFillColor: String = "#424242"): String {
        var updatedContent = svgContent

        // <text> 태그 내의 모든 내용을 찾아서 처리
        val textTagContentRegex = Regex("""<text[^>]*>.*?</text>""", RegexOption.DOT_MATCHES_ALL)
        updatedContent = textTagContentRegex.replace(updatedContent) { matchResult ->
            var tagContent = matchResult.value

            // font-family 속성 변경
            tagContent = tagContent.replace(Regex("""font-family\s*=\s*["'][^"']*["']"""), """font-family="$targetFontFamily"""")

            // fill 속성 변경
            tagContent = tagContent.replace(Regex("""fill\s*=\s*["']#[0-9a-fA-F]{3,6}["']"""), """fill="$targetFillColor"""")

            tagContent
        }

        return updatedContent
    }
    private fun updateStyleTagContent(svgContent: String, targetFontFamily: String, targetFillColor: String = "#424242"): String {
        // <style> 태그의 내용 찾기
        val styleTagRegex = Regex("""<style[^>]*>(.*?)</style>""", RegexOption.DOT_MATCHES_ALL)

        return styleTagRegex.replace(svgContent) { matchResult ->
            val styleContent = matchResult.groups[1]?.value ?: ""
            // 각 CSS 규칙을 찾아서 처리
            val updatedStyleContent = styleContent.replace(Regex("""([^{]+)\{([^}]+)\}""")) { cssMatch ->
                val selectors = cssMatch.groups[1]?.value?.trim() ?: ""
                var properties = cssMatch.groups[2]?.value ?: ""

                // font-family 속성이 있는 블록만 처리
                if (properties.contains(Regex("""font-family\s*:"""))) {
                    // font-family 변경
                    properties = properties.replace(Regex("""font-family\s*:\s*[^;]+;"""), """font-family: $targetFontFamily;""")

                    // fill 속성 변경
                    properties = properties.replace(Regex("""fill\s*:\s*[^;]+;"""), """fill: $targetFillColor;""")
                }

                "$selectors {\n$properties\n}"
            }

            "<style>\n$updatedStyleContent\n</style>"
        }
    }

    private fun updateSvgFont(svgContent: String, targetFontFamily: String, targetFillColor: String = "#424242"): String {
        var updatedContent = svgContent

        // 1. <text> 태그의 속성 변경
        updatedContent = updateTextTagContent(updatedContent, targetFontFamily, targetFillColor)

        // 2. <style> 태그의 속성 변경
        updatedContent = updateStyleTagContent(updatedContent, targetFontFamily, targetFillColor)

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

}