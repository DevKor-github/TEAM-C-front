package com.example.deckor_teamc_front

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deckor_teamc_front.databinding.FragmentSearchBuildingBinding

class SearchBuildingFragment : Fragment() {

    private var _binding: FragmentSearchBuildingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FetchDataViewModel by viewModels()
    private lateinit var adapter: SearchListAdapter

    private var taggedBuildingId : Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBuildingBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.customEditTextLayout.backToHomeButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.customEditTextLayout.clearButton.setOnClickListener {
            binding.customEditTextLayout.editText.setText("")
            binding.customEditTextLayout.tagContainer.removeAllViews()
            binding.customEditTextLayout.editText.hint = "학교 건물을 검색해 주세요"
            taggedBuildingId = null
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.searchListRecyclerview.layoutManager = layoutManager

        adapter = SearchListAdapter(emptyList()) { buildingItem ->
            if (buildingItem.placeType == "TAG") {
                // 건물 태그를 선택했을 때 태그 추가
                addTag(buildingItem.name)
                binding.customEditTextLayout.editText.setText("")
                binding.customEditTextLayout.editText.hint = "건물 내 장소를 입력하세요"
            } else if (buildingItem.placeType == "BUILDING") {
                // 건물을 선택했을 때 OpenModal 함수 호출
                openLocationModal(requireActivity(), buildingItem)
                Log.e("d","ddddddddddd")
            } else if (buildingItem.placeType == "CLASSROOM") {
                // 장소을 선택했을 때 OpenModal 함수 호출
                navigateToInnerMapFragment(buildingItem.id)
            }
            else Log.e("SearchBuildingFragment","No mating type")
            taggedBuildingId = buildingItem.id
        }

        binding.searchListRecyclerview.adapter = adapter

        viewModel.buildingSearchItems.observe(viewLifecycleOwner, Observer { buildingItems ->
            adapter.setBuildingList(buildingItems)
        })

        binding.customEditTextLayout.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim().replace("${Constants.TAG_SUFFIX} ", "")
                if (searchText.isNotBlank()) {
                    viewModel.searchBuildings(searchText, taggedBuildingId)
                } else {
                    adapter.setBuildingList(emptyList())
                }
                binding.customEditTextLayout.clearButton.visibility = if (searchText.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Open keyboard when the fragment starts
        binding.customEditTextLayout.editText.requestFocus()
        binding.customEditTextLayout.editText.postDelayed({
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.customEditTextLayout.editText, InputMethodManager.SHOW_IMPLICIT)
        }, 100)

        return view
    }

    override fun onDestroyView() {
        taggedBuildingId = null
        super.onDestroyView()
        _binding = null
    }

    private fun addTag(tag: String) {
        val tagContainer = binding.customEditTextLayout.tagContainer
        val tagView = LayoutInflater.from(context).inflate(R.layout.tag_item, tagContainer, false) as LinearLayout
        val tagText = tagView.findViewById<TextView>(R.id.tag_text)
        val removeButton = tagView.findViewById<ImageButton>(R.id.remove_button)
        tagText.text = tag.replace(" ${Constants.TAG_SUFFIX}", "")

        removeButton.setOnClickListener {
            tagContainer.removeView(tagView)
            taggedBuildingId = null
            // 태그가 모두 삭제되었는지 확인하고 힌트를 초기화하는 코드 추가
            if (tagContainer.childCount == 0) {
                binding.customEditTextLayout.editText.hint = "학교 건물을 검색해 주세요"
            }
        }

        tagContainer.addView(tagView)
    }

    private fun navigateToInnerMapFragment(roomId: Int) {
        viewModel.fetchPlaceInfo(roomId, "CLASSROOM") { placeInfo ->
            placeInfo?.let {
                // 캐시에서 BuildingItem 가져오기
                val buildingItem = BuildingCache.get(it.buildingId)

                if (buildingItem != null) {
                    // 캐시된 BuildingItem의 정보를 사용
                    val selectedBuildingName = buildingItem.name
                    val selectedBuildingAboveFloor = buildingItem.floor ?: 0
                    val selectedBuildingUnderFloor = buildingItem.underFloor

                    val selectedRoomFloor = it.floor
                    val selectedRoomMask = it.maskIndex

                    val innerMapFragment = InnerMapFragment.newInstanceFromSearch(
                        selectedBuildingName, selectedBuildingAboveFloor, selectedBuildingUnderFloor,
                        it.buildingId, selectedRoomFloor, selectedRoomMask
                    )

                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    requireActivity().supportFragmentManager.popBackStack() // SearchFragment 제거
                    transaction.add(R.id.main_container, innerMapFragment)
                    transaction.addToBackStack("InnerMapFragment")
                    transaction.commit()
                } else {
                    // 캐시에 BuildingItem이 없는 경우 디버그 로그 출력
                    Log.d("navigateToInnerMapFragment", "BuildingItem not found in cache for buildingId: ${it.buildingId}")
                }
            } ?: run {
                // placeInfo가 null인 경우 오류 처리
                Log.d("navigateToInnerMapFragment", "Failed to fetch place info.")
            }
        }
    }

}
