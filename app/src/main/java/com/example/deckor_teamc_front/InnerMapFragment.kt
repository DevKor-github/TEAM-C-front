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
import androidx.activity.addCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.example.deckor_teamc_front.databinding.InnerMapContainerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    private val binding get() = _binding!!

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedBuildingName = it.getString("selectedBuildingName")
            selectedBuildingAboveFloor = it.getInt("selectedBuildingAboveFloor")
            selectedBuildingUnderFloor = it.getInt("selectedBuildingUnderFloor")
            selectedBuildingId = it.getInt("selectedBuildingId")

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
            binding.includedModal.root.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)

        customScrollView = binding.customScrollView

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)
        observeViewModel()

        if (hasDirection) {
            try {
                searchedRoute = DirectionSearchRouteDataHolder.splitedRoute
                Log.e("InnerMapFragment", "searchedRoute is set successfully ${searchedRoute}")
            } catch (e: UninitializedPropertyAccessException) {
                Log.e("InnerMapFragment", "splitedRoute is not initialized: ${e.message}")
            }
        }

        val routeView = binding.includedMap.routeView

        drawRouteForCurrentFloor(routeView, searchedRoute, innermapCurrentFloor)


        Log.e("InnerMapFragment", "${innermapCurrentFloor} Floor")
        onFloorSelected(innermapCurrentFloor)

        val layerName = colorMap[searchedMask] // 선택된 마스크를 레이어명으로 변환
        if (layerName != null) {
            Log.d("InnerMapFragment", "Filename for id $searchedMask: $layerName")
            replaceInnermap(layerName) // 검색에서 진입 할 시 장소의 색상을 변경하는 로직
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
            // 스크롤을 토글하는 로직 추가
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
        Log.e("innermapfragment", "Now on $floor")
        if (floor == 0) return

        innermapCurrentFloor = floor
        fetchData()
        readJsonAndUpdateColorMap()
        replaceInnermap()
        replaceInnermapMask()
        updateTouchHandler()
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
        // `initInnermap` 함수에서 수정된 SVG 콘텐츠를 가져옴
        val modifiedSvgContent = initInnermap()

        if (modifiedSvgContent != null) {
            try {
                // groupIdToChange가 null이 아니면 해당 그룹의 fill 색상을 변경
                val remodifiedSvgContent = groupIdToChange?.let {
                    changeElementFillColor(modifiedSvgContent, it)
                } ?: modifiedSvgContent

                val svg = SVG.getFromString(remodifiedSvgContent)

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


    private fun changeElementFillColor(svgContent: String, elementId: String): String {
        // 특정 그룹 요소 안에 있는 자식 요소들의 스타일을 변경
        val regex = Regex("""(<g[^>]*id="$elementId"[^>]*>.*?</g>)""", RegexOption.DOT_MATCHES_ALL)
        return svgContent.replace(regex) { matchResult ->
            var groupContent = matchResult.value

            // 그룹 내부에서 일반 fill 속성은 #F85C5C로 변경
            groupContent = groupContent.replace(Regex("""fill="#[0-9A-Fa-f]{3,6}"""")) { _ ->
                """fill="#F85C5C""""
            }

            // 그룹 내부에서 <text> 태그의 fill 속성은 #FFFFFF로 변경
            groupContent = groupContent.replace(Regex("""<text[^>]*fill="#[0-9A-Fa-f]{3,6}"""")) { matchResult ->
                matchResult.value.replace(
                    Regex("""fill="#[0-9A-Fa-f]{3,6}""""),
                    """fill="#FFFFFF""""
                )
            }

            // 그룹 내부에서 <style> 태그의 fill 속성을 #F85C5C로 변경
            groupContent = groupContent.replace(Regex("""<style[^>]*>.*?<\/style>""", RegexOption.DOT_MATCHES_ALL)) { matchResult ->
                var styleContent = matchResult.value

                // <style> 태그 내에서 fill 속성만 변경 (폰트 패밀리는 변경하지 않음)
                styleContent = styleContent.replace(Regex("""fill\s*:\s*#[0-9A-Fa-f]{3,6}""")) { _ ->
                    """fill: #FFFFFF"""
                }

                styleContent
            }

            groupContent
        }
    }



    private fun updateTouchHandler() {
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
                id = selectedBuildingId,
                replaceInnermapCallback = { groupId ->
                    replaceInnermap(groupId)
                }
            )
            imageView.setOnTouchListener(touchHandler)
        } catch (e: Exception) {
            Log.e("InnerMapFragment", "Error creating touch handler: ${e.message}")
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
        innermapCurrentFloor: Int
    ) {
        searchedRoute?.let { routeResponse ->
            // 현재 층과 일치하는 경로만 필터링
            val matchingRoutes = routeResponse.path.filter { it.floor == innermapCurrentFloor }

            // 필터링된 경로에서 좌표를 추출하여 튜플로 저장
            val coordinates = matchingRoutes.flatMap { route ->
                route.route.map { Pair(it[0].toFloat(), it[1].toFloat()) }
            }

            // 디버깅을 위한 출력
            Log.d("RouteDebug", "Coordinates for floor $innermapCurrentFloor: $coordinates")

            // 루트 좌표를 설정하여 그리기 (비율 조정 포함)
            routeView.setRouteCoordinates(coordinates)
        } ?: run {
            // 예외 처리 또는 로그
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

}