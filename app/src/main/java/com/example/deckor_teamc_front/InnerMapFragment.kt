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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InnerMapContainerBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)
        observeViewModel()
        fetchData()

        closeModal()
        setupSvgFiles()
        return binding.root
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

    private fun setupSvgFiles() {
        val includedLayout = binding.includedMap.root
        val container: FrameLayout = includedLayout.findViewById(R.id.frameLayoutContainer)
        val svgFileNames = mutableListOf<String>()

        try {
            val assetManager = requireContext().assets
            val svgFiles = assetManager.list("1floor")?.sortedWith(compareBy {
                when (it) {
                    "1floor/FloorBackground.svg" -> 0
                    "1floor/Border.svg" -> 2
                    else -> 1
                }
            }) ?: return

            val viewsToAdd = mutableListOf<FrameLayout>()

            for (fileName in svgFiles) {
                if (fileName.endsWith(".svg")) {
                    svgFileNames.add(fileName)

                    val svgInputStream = assetManager.open("1floor/$fileName")
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
                folderName = "1floor",
                viewModel = viewModel,
                lifecycleOwner = viewLifecycleOwner
            )
            container.setOnTouchListener(touchHandler)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
