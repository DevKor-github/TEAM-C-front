package com.devkor.kodaero

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devkor.kodaero.databinding.FragmentBuildingDetailBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BuildingDetailFragment : Fragment() {

    private var _binding: FragmentBuildingDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FetchDataViewModel
    private var selectedBuildingId: Int? = null

    private lateinit var selectedBuildingName: String
    private var selectedBuildingAboveFloor: Int? = null
    private var selectedBuildingUnderFloor: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedBuildingId = it.getInt("buildingId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBuildingDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeModal()
        selectedBuildingId?.let { updateBookmarkButton(it) }

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)

        binding.buildingDetailOperatingTimeLayout.visibility = View.GONE

        binding.showOperatingTimeLayout.setOnClickListener {
            val isVisible = binding.buildingDetailOperatingTimeLayout.visibility == View.VISIBLE
            if (isVisible) {
                binding.buildingDetailOperatingTimeLayout.visibility = View.GONE
                binding.showOperatingTimeButton.setImageResource(R.drawable.button_show_operating_time)
            } else {
                binding.buildingDetailOperatingTimeLayout.visibility = View.VISIBLE
                binding.showOperatingTimeButton.setImageResource(R.drawable.button_hide_operating_time)
            }
        }

        binding.buildingDetailFacilityTypes.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.buildingDetailFacilitiesGridview.layoutManager = GridLayoutManager(context, 2)

        selectedBuildingId?.let { id ->
            val building = getBuildingInfo(id) ?: return

            selectedBuildingName = building.name
            selectedBuildingAboveFloor = building.floor
            selectedBuildingUnderFloor = building.underFloor

            viewModel.fetchBuildingDetail(id)
            viewModel.buildingDetail.observe(viewLifecycleOwner, Observer { buildingDetail ->
                binding.buildingDetailName.text = buildingDetail?.name
                binding.buildingDetailAddress.text = buildingDetail?.address
                binding.buildingDetailDeadline.text = buildingDetail?.nextBuildingTime
                binding.buildingDetailOperatingTime1.text = buildingDetail?.weekdayOperatingTime
                binding.buildingDetailOperatingTime2.text = buildingDetail?.saturdayOperatingTime
                binding.buildingDetailOperatingTime3.text = buildingDetail?.sundayOperatingTime

                if (buildingDetail?.operating == true) {
                    binding.buildingDetailOperatingStatus.text = "운영 중"
                    binding.buildingDetailDeadlineText.text = "에 운영 종료"
                } else {
                    binding.buildingDetailOperatingStatus.text = "운영 종료"
                    binding.buildingDetailDeadlineText.text = "에 운영 시작"
                }

                buildingDetail?.imageUrl?.let { url ->
                    Glide.with(this)
                        .load(url)
                        .into(binding.buildingDetailImage)
                }

                val displayMetrics = resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val itemMargin = 20 * resources.displayMetrics.density
                val itemWidth = (screenWidth / 2) - (2 * itemMargin.toInt())

                val facilityGridAdapter = FacilityGridAdapter(
                    facilities = buildingDetail?.mainFacilityList ?: emptyList(),
                    itemWidth = itemWidth,
                    onItemClick = { placeId ->
                        navigateToInnerMapFragment(placeId)
                    }
                )
                binding.buildingDetailFacilitiesGridview.adapter = facilityGridAdapter

                binding.buildingDetailTmiName.text = buildingDetail?.name
                binding.buildingDetailTmi.text = buildingDetail?.details?.replace("\\n", "\n")

                val adapter =
                    buildingDetail?.existTypes?.let { FacilityTypeAdapter(it, requireContext()) }
                binding.buildingDetailFacilityTypes.adapter = adapter

                setActiveButton(
                    binding.buildingDetailFacilityButton,
                    binding.buildingDetailTmiButton
                )
            })
        }

        binding.modalInnermapButton.setOnClickListener {
            navigateToInnerMapFragment()
        }

        val prefix = "고려대학교 서울캠퍼스"

        binding.modalDepartButton.setOnClickListener {
            val cleanedBuildingName = selectedBuildingName.removePrefix(prefix).trim()
            putBuildingDirectionsFragment(true, cleanedBuildingName, "BUILDING", selectedBuildingId)
        }

        binding.modalArriveButton.setOnClickListener {
            val cleanedBuildingName = selectedBuildingName.removePrefix(prefix).trim()
            putBuildingDirectionsFragment(
                false,
                cleanedBuildingName,
                "BUILDING",
                selectedBuildingId
            )
        }

        binding.buildingDetailFacilityButton.setOnClickListener {
            setActiveButton(binding.buildingDetailFacilityButton, binding.buildingDetailTmiButton)
            showLayout(binding.buildingDetailFacilitiesGridview, binding.buildingDetailTmiLayout)
        }

        binding.buildingDetailTmiButton.setOnClickListener {
            setActiveButton(binding.buildingDetailTmiButton, binding.buildingDetailFacilityButton)
            showLayout(binding.buildingDetailTmiLayout, binding.buildingDetailFacilitiesGridview)
        }

        binding.buildingDetailBookmarkedButton.setOnClickListener {
            openBookMarkModal(selectedBuildingId!!)
        }
    }

    private fun setActiveButton(activeButton: Button, inactiveButton: Button) {
        activeButton.setBackgroundColor(resources.getColor(R.color.red))
        activeButton.setTextColor(resources.getColor(R.color.bright_gray))

        inactiveButton.setBackgroundColor(resources.getColor(R.color.white))
        inactiveButton.setTextColor(resources.getColor(R.color.black))
    }

    private fun showLayout(visibleLayout: View, invisibleLayout: View) {
        visibleLayout.visibility = View.VISIBLE
        invisibleLayout.visibility = View.GONE
    }

    private fun navigateToInnerMapFragment() {
        if (selectedBuildingAboveFloor != null && selectedBuildingUnderFloor != null && selectedBuildingId != null && selectedBuildingName != null) {
            val innerMapFragment = InnerMapFragment.newInstance(
                selectedBuildingName,
                selectedBuildingAboveFloor!!,
                selectedBuildingUnderFloor!!,
                selectedBuildingId!!
            )

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(R.id.main_container, innerMapFragment)
            transaction.addToBackStack("InnerMapFragment")
            transaction.commit()
        } else {
            Log.e("navigateToInnerMapFragment", "Missing one or more required arguments")
        }
    }

    private fun navigateToInnerMapFragment(roomId: Int) {
        viewModel.fetchPlaceInfo(roomId) { placeInfo ->
            placeInfo?.let {
                val buildingItem = BuildingCache.get(it.buildingId)

                if (buildingItem != null) {
                    val selectedBuildingName = buildingItem.name
                    val selectedBuildingAboveFloor = buildingItem.floor ?: 0
                    val selectedBuildingUnderFloor = buildingItem.underFloor

                    val selectedRoomFloor = it.floor
                    val selectedRoomMask = it.maskIndex

                    val innerMapFragment = InnerMapFragment.newInstanceFromSearch(
                        selectedBuildingName,
                        selectedBuildingAboveFloor,
                        selectedBuildingUnderFloor,
                        it.buildingId,
                        selectedRoomFloor,
                        selectedRoomMask,
                        false
                    )

                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.add(R.id.main_container, innerMapFragment)
                    transaction.addToBackStack("InnerMapFragment")
                    transaction.commit()
                } else {
                    Log.d(
                        "navigateToInnerMapFragment",
                        "BuildingItem not found in cache for buildingId: ${it.buildingId}"
                    )
                }
            } ?: run {
                Log.d("navigateToInnerMapFragment", "Failed to fetch place info.")
            }
        }
    }

    private fun getBuildingInfo(buildingId: Int): BuildingItem? {
        val building = BuildingCache.get(buildingId)

        if (building != null) {
            return building
        } else {
            return null
        }
    }

    private fun putBuildingDirectionsFragment(
        isStartingPoint: Boolean,
        buildingName: String,
        placeType: String,
        id: Int?
    ) {
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
            ?.add(R.id.main_container, getDirectionsFragment, "DirectionFragment")
            ?.addToBackStack("DirectionFragment")
            ?.commit()
    }

    companion object {
        @JvmStatic
        fun newInstance(buildingId: Int) = BuildingDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("buildingId", buildingId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openBookMarkModal(buildingId: Int) {
        val bookMarkAddingModal = binding.bookmarkModal.standardBottomSheet
        val bookMarkAddingModalCloseButton =
            binding.bookmarkModal.root.findViewById<ImageButton>(R.id.modal_sheet_close_button)
        val saveBookmarkButton =
            binding.bookmarkModal.root.findViewById<Button>(R.id.save_bookmark_button)
        val bookMarkRecyclerView =
            binding.bookmarkModal.root.findViewById<RecyclerView>(R.id.modal_sheet_bookmark_types)
        val bottomSheetBehavior = BottomSheetBehavior.from(bookMarkAddingModal)

        bookMarkAddingModalCloseButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior.isDraggable = false

        bookMarkRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = CategoryAdapter(emptyList())
        bookMarkRecyclerView.adapter = adapter

        val categoryViewModel: CategoryViewModel by viewModels()
        categoryViewModel.categories.observe(viewLifecycleOwner) { items ->
            adapter.updateItems(items)
        }

        adapter.onAddButtonClick = {
            val dialog = AddCategoryDialog(requireContext(), categoryViewModel)
            dialog.show()
        }

        categoryViewModel.fetchCategories(buildingId)

        saveBookmarkButton.setOnClickListener {
            val selectedCategories = adapter.getSelectedCategories()
            val bookmarkManager = BookmarkManager(requireContext(), RetrofitClient.instance)
            bookmarkManager.addBookmarks(selectedCategories, "BUILDING", buildingId, "")
            viewLifecycleOwner.lifecycleScope.launch {
                delay(100)
                updateBookmarkButton(buildingId)
            }
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        viewLifecycleOwner.lifecycleScope.launch {
            delay(300)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun closeModal() {
        val bookMarkModal = binding.bookmarkModal.root
        val bookMarkBottomSheet = bookMarkModal.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val bookMarkBottomSheetBehavior = BottomSheetBehavior.from(bookMarkBottomSheet)
        bookMarkBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun updateBookmarkButton(buildingId: Int) {
        val bookmarkedButton = binding.buildingDetailBookmarkedButton
        val categoryViewModel: CategoryViewModel by viewModels()  // 프래그먼트 전용 ViewModel

        viewLifecycleOwner.lifecycleScope.launch {
            val categories = categoryViewModel.fetchCategoriesBuildingSync(buildingId)
            categories?.let {
                val hasBookmarkedItem = it.any { it.bookmarked }
                if (hasBookmarkedItem) {
                    Log.d("CategoryCheck", "There is at least one bookmarked category.")
                    bookmarkedButton.setImageResource(R.drawable.button_bookmarked_on)
                } else {
                    Log.d("CategoryCheck", "No bookmarked categories found.")
                    bookmarkedButton.setImageResource(R.drawable.button_bookmarked_off)
                }
                // 버튼을 다시 그리도록 강제
                bookmarkedButton.invalidate()
                bookmarkedButton.requestLayout()
            } ?: run {
                Log.e("CategoryCheck", "Failed to fetch categories or no categories found.")
            }
        }
    }

}