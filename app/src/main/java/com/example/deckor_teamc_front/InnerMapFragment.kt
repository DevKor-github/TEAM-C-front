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
import java.io.IOException
import java.io.InputStream

class InnerMapFragment : Fragment(), CustomScrollView.OnFloorSelectedListener {
    companion object {
        @JvmStatic
        fun newInstance(selectedBuildingName: String, selectedBuildingAboveFloor: Int, selectedBuildingUnderFloor: Int, selectedBuildingId: Int) =
            InnerMapFragment().apply {
                arguments = Bundle().apply {
                    putString("selectedBuildingName", selectedBuildingName)
                    putInt("selectedBuildingAboveFloor", selectedBuildingAboveFloor)
                    putInt("selectedBuildingUnderFloor", selectedBuildingUnderFloor)
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
    private var selectedBuildingAboveFloor: Int? = null
    private var selectedBuildingUnderFloor: Int? = null

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
            selectedBuildingAboveFloor = it.getInt("selectedBuildingAboveFloor")
            selectedBuildingUnderFloor = it.getInt("selectedBuildingUnderFloor")
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

        selectedBuildingAboveFloor?.let { aboveFloor ->
            selectedBuildingUnderFloor?.let { underFloor ->
                customScrollView.setFloors(-underFloor, aboveFloor)
            }
        }
        customScrollView.setOnFloorSelectedListener(this)

        Log.d("InnerMapFragment", "Selected Building Name: $selectedBuildingName")
        Log.d("InnerMapFragment", "Selected Building Above Floor: $selectedBuildingAboveFloor")
        Log.d("InnerMapFragment", "Selected Building Under Floor: $selectedBuildingUnderFloor")
        Log.d("InnerMapFragment", "Selected Building Id: $selectedBuildingId")

        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        standardBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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

            val inputStream: InputStream = assetManager.open("${selectedBuildingId}/${innermapCurrentFloor}/data.json")
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
        innermapCurrentFloor = floor
        fetchData()
        readJsonAndUpdateColorMap()
        replaceInnermap()
        replaceInnermapMask()
        updateTouchHandler()
        Log.d("InnerMapFragment", "Floor selected: $floor, innermapCurrentFloor updated: $innermapCurrentFloor")
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


    private fun replaceInnermap() {
        val resourceName = "${selectedBuildingId}/${innermapCurrentFloor}/inner_map.svg"
        try {
            // 리소스가 assets 폴더에 있는지 확인하고 로드
            val assetManager = requireContext().assets
            val inputStream: InputStream = assetManager.open(resourceName)
            val svg = SVG.getFromInputStream(inputStream)
            val drawable = PictureDrawable(svg.renderToPicture())

            // ImageView에 SVG 렌더링
            binding.includedMap.innermap.setImageDrawable(drawable)
            Log.d("InnerMapFragment", "Resource replaced with: $resourceName")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("InnerMapFragment", "Resource not found: $resourceName")
        } catch (e: SVGParseException) {
            e.printStackTrace()
            Log.e("InnerMapFragment", "Error parsing SVG: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("InnerMapFragment", "Unexpected error: ${e.message}")
        }
    }

    private fun updateTouchHandler() {
        Log.e("e","$floorRoomList")
        val imageView: ImageView = binding.includedMap.innermapMask
        val filePath = "${selectedBuildingId}/${innermapCurrentFloor}/final_overlay.png"
        val assetManager = context?.assets

        val bitmap: Bitmap

            val inputStream = assetManager?.open(filePath)
            bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()



        try {
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
        catch (e: Exception ) {
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


}