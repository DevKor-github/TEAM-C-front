package com.example.deckor_teamc_front

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import android.widget.TextView
import com.example.deckor_teamc_front.databinding.InnerMapContainerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGImageView
import java.io.IOException

class InnerMapFragment : Fragment() {
    private var _binding: InnerMapContainerBinding? = null
    private val binding get() = _binding!!

    private val originalViews = mutableListOf<FrameLayout>()

    private lateinit var viewModel: FetchDataViewModel
    private var selectedBuildingId: Int = 1 // 기본값 설정 또는 실제 값으로 대체

    private var selectedBuildingName: String? = null
    private var selectedBuildingFloor: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedBuildingName = it.getString("selectedBuildingName")

            selectedBuildingFloor = it.getInt("selectedBuildingFloor")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InnerMapContainerBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)
        observeViewModel()
        fetchData()

        closeModal()
        setupSvgFiles(1)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val buildingNameTextView = view?.findViewById<TextView>(R.id.building_name)
        buildingNameTextView?.text = "고려대학교 서울캠퍼스 " + selectedBuildingName


        val customScrollView = view?.findViewById<CustomScrollView>(R.id.customScrollView)
        selectedBuildingFloor?.let { customScrollView?.setMaxNumber(it) } // 최대 숫자를 설정 (예: 7)

        // Use the selectedBuildingName as needed
        Log.d("InnerMapFragment", "Selected Building Name: $selectedBuildingName")


    }

    companion object {
        @JvmStatic
        fun newInstance(selectedBuildingName: String, selectedBuildingFloor: Int) =
            InnerMapFragment().apply {
                arguments = Bundle().apply {
                    putString("selectedBuildingName", selectedBuildingName)
                    putInt("selectedBuildingFloor", selectedBuildingFloor)
                }
            }
    }


    private fun fetchData() {
        viewModel.fetchRoomList(selectedBuildingId)
    }

    private fun observeViewModel() {
        viewModel.roomList.observe(viewLifecycleOwner, Observer { roomList ->
            if (roomList.isNotEmpty()) {
                // roomList를 로그로 출력
                for (room in roomList) {
                    Log.d("InnerMapFragment", "Room: $room")
                }
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
        val standardBottomSheet = includedLayout.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setupSvgFiles(floor: Int) {
        val includedLayout = binding.includedMap.root
        val container: FrameLayout = includedLayout.findViewById(R.id.frameLayoutContainer)
        val svgFileNames = mutableListOf<String>()

        try {
            val assetManager = requireContext().assets
            val folderName = "$selectedBuildingName/$floor"
            val svgFiles = assetManager.list(folderName)?.sortedWith(compareBy {
                when (it) {
                    "FloorBackground.svg" -> 0
                    "Border.svg" -> 2
                    else -> 1
                }
            }) ?: return

            val viewsToAdd = mutableListOf<FrameLayout>()

            for (fileName in svgFiles) {
                if (fileName.endsWith(".svg")) {
                    svgFileNames.add(fileName)

                    val svgInputStream = assetManager.open("$folderName/$fileName")
                    val svg = SVG.getFromInputStream(svgInputStream)

                    val svgImageView = SVGImageView(requireContext())
                    svgImageView.setSVG(svg)

                    // Create a new FrameLayout for each SVG
                    val svgFrameLayout = FrameLayout(requireContext())
                    svgFrameLayout.layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )

                    // Add SVGImageView to the new FrameLayout
                    svgFrameLayout.addView(svgImageView)

                    // Add the new FrameLayout to the main container
                    container.addView(svgFrameLayout)

                    // Save the original state
                    viewsToAdd.add(svgFrameLayout)
                }
            }

            // Save the initial state of the container
            originalViews.addAll(viewsToAdd)

            // TouchHandler 인스턴스를 초기화하고 터치 리스너로 설정
            val touchHandler = InnerMapTouchHandler(
                context = requireContext(),
                container = container,
                svgFileNames = svgFileNames,
                folderName = folderName,
                viewModel = viewModel,
                lifecycleOwner = viewLifecycleOwner
            )
            container.setOnTouchListener(touchHandler)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}
