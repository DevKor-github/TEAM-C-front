package com.example.deckor_teamc_front

import android.graphics.BitmapFactory
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
import com.example.deckor_teamc_front.databinding.InnerMapContainerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.InputStream

class InnerMapFragment : Fragment(), CustomScrollView.OnFloorSelectedListener {
    companion object {
        @JvmStatic
        fun newInstance(selectedBuildingName: String, selectedBuildingTotalFloor: Int, selectedBuildingId: Int) =
            InnerMapFragment().apply {
                arguments = Bundle().apply {
                    putString("selectedBuildingName", selectedBuildingName)
                    putInt("selectedBuildingTotalFloor", selectedBuildingTotalFloor)
                    putInt("selectedBuildingId", selectedBuildingId)
                }
            }
    }

    private var _binding: InnerMapContainerBinding? = null
    private val binding get() = _binding!!

    private val originalViews = mutableListOf<FrameLayout>()

    private lateinit var viewModel: FetchDataViewModel

    private var selectedBuildingId: Int? = null
    private var selectedBuildingName: String? = null
    private var selectedBuildingTotalFloor: Int? = null

    private var innermapCurrentFloor: Int? = 1

    // 초기 colorMap
    private var colorMap = mutableMapOf<Int, String>()

    private var isScrollVisible = true

    private var floorRoomList: List<RoomList> = emptyList()

    private lateinit var modalView : CoordinatorLayout

    private lateinit var standardBottomSheet : FrameLayout
    private lateinit var standardBottomSheetBehavior : BottomSheetBehavior<FrameLayout>
    private lateinit var customScrollView : CustomScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedBuildingName = it.getString("selectedBuildingName")
            selectedBuildingTotalFloor = it.getInt("selectedBuildingTotalFloor")
            selectedBuildingId = it.getInt("selectedBuildingId")
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

        standardBottomSheet = binding.includedModal.root.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)

        customScrollView = binding.customScrollView

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)
        observeViewModel()
        fetchData()

        closeModal()

        replaceInnermap()
        replaceInnermapMask()
        updateTouchHandler()

        val buildingNameTextView = binding.buildingName
        buildingNameTextView.text = "고려대학교 서울캠퍼스 " + selectedBuildingName

        val customScrollView = binding.customScrollView
        selectedBuildingTotalFloor?.let { customScrollView.setMaxNumber(it) } // 최대 숫자를 설정 (예: 7)
        customScrollView.setOnFloorSelectedListener(this)

        // Use the selectedBuildingName as needed
        Log.d("InnerMapFragment", "Selected Building Name: $selectedBuildingName")
        Log.d("InnerMapFragment", "Selected Building Floor: $selectedBuildingTotalFloor")
        Log.d("InnerMapFragment", "Selected Building Id: $selectedBuildingId")


        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        standardBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        hideScroll()
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        showScroll()
                    }
                    BottomSheetBehavior.STATE_SETTLING-> {
                        hideScroll()
                    }
                    else -> {}
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
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
        selectedBuildingId?.let { innermapCurrentFloor?.let { it1 ->
            viewModel.fetchRoomList(it,
                it1
            )
        } }
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

    private fun closeModal() {
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

            val inputStream: InputStream = assetManager.open("innermap_mask_index.json")
            Log.d("InnerMapFragment", "Opened building_data.json file")

            val jsonString = inputStream.bufferedReader().use { it.readText() }
            Log.d("InnerMapFragment", "JSON data: $jsonString")

            val buildingDataType = object : TypeToken<Map<String, Map<String, List<List<Any>>>>>() {}.type
            val buildings: Map<String, Map<String, List<List<Any>>>> = Gson().fromJson(jsonString, buildingDataType)
            Log.d("InnerMapFragment", "Parsed JSON data successfully")

            if (selectedBuildingId != null && innermapCurrentFloor != null) {
                val buildingKey = "Building $selectedBuildingId"
                val floorKey = "Floor $innermapCurrentFloor"
                val buildingFloors = buildings[buildingKey]
                val areas = buildingFloors?.get(floorKey)
                Log.d("InnerMapFragment", "Areas for $buildingKey, $floorKey: $areas")

                areas?.forEach { area ->
                    val svgFile = area[0] as String
                    val id = (area[1] as Double).toInt() // Gson이 숫자를 Double로 파싱하기 때문에 Int로 변환
                    colorMap[id] = svgFile
                    Log.d("InnerMapFragment", "Added to colorMap: id=$id, svgFile=$svgFile")
                }
            } else {
                Log.e("InnerMapFragment", "Building ID or current floor is null")
            }
        } catch (e: IOException) {
            Log.e("InnerMapFragment", "Error reading JSON file: ${e.message}")
        } catch (e: Exception) {
            Log.e("InnerMapFragment", "Error parsing JSON file: ${e.message}")
        }
    }

    override fun onFloorSelected(floor: Int) {
        innermapCurrentFloor = floor
        fetchData()
        readJsonAndUpdateColorMap()
        replaceInnermap()
        replaceInnermapMask()
        updateTouchHandler()
        Log.d("InnerMapFragment", "Floor selected: $floor, innermapCurrentFloor updated: $innermapCurrentFloor")
    }

    private fun replaceInnermapMask() {
        val resourceName = "innermap_${selectedBuildingId}_${innermapCurrentFloor}_mask"
        val resourceId =
            resources.getIdentifier(resourceName, "drawable", requireContext().packageName)

        if (resourceId != 0) {
            val bitmap = BitmapFactory.decodeResource(resources, resourceId)
            binding.includedMap.innermapMask.setImageBitmap(bitmap)
            Log.d("InnerMapFragment", "Bitmap replaced with resource: $resourceName")
        } else {
            Log.e("InnerMapFragment", "Resource not found: $resourceName")
        }
    }


    private fun replaceInnermap() {
        val resourceName = "innermap_${selectedBuildingId}_${innermapCurrentFloor}"
        val resourceId =
            resources.getIdentifier(resourceName, "drawable", requireContext().packageName)

        if (resourceId != 0) {
            binding.includedMap.innermap.setImageResource(resourceId)
            Log.d("InnerMapFragment", "Resource replaced with: $resourceName")
        } else {
            Log.e("InnerMapFragment", "Resource not found: $resourceName")
        }

    }
    private fun updateTouchHandler() {
        Log.e("e","$floorRoomList")
        val imageView: ImageView = binding.includedMap.innermapMask
        val bitmap = BitmapFactory.decodeResource(resources, resources.getIdentifier("innermap_${selectedBuildingId}_${innermapCurrentFloor}_mask", "drawable", requireContext().packageName))
        val touchHandler = InnerMapTouchHandler(
            context = requireContext(),
            imageView = imageView,
            bitmap = bitmap,
            colorMap = colorMap,
            roomList = floorRoomList,
            innerMapBinding = binding

        )
        imageView.setOnTouchListener(touchHandler)
    }

    private fun showScroll() {
        customScrollView.visibility = View.VISIBLE
        isScrollVisible = true
    }

    private fun hideScroll() {
        customScrollView.visibility = View.GONE
        isScrollVisible = false
    }


}