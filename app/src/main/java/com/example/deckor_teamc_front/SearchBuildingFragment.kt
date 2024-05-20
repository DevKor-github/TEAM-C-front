package com.example.deckor_teamc_front

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deckor_teamc_front.databinding.FragmentSearchBuildingBinding

class SearchBuildingFragment : Fragment() {

    private var _binding: FragmentSearchBuildingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchBuildingViewModel by viewModels()
    private lateinit var adapter: SearchListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBuildingBinding.inflate(inflater, container, false)
        val view = binding.root

        // 이전 화면으로 돌아가는 버튼 클릭 리스너
        binding.backToHomeButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // 검색 텍스트 삭제 버튼 표시/숨김 처리
        binding.deleteTextButton.setOnClickListener {
            binding.searchBar.setText("")
        }

        // RecyclerView 설정
        val layoutManager = LinearLayoutManager(requireContext())
        binding.searchListRecyclerview.layoutManager = layoutManager

        adapter = SearchListAdapter(emptyList()) { buildingItem ->
            if (buildingItem.placeType == "BUILDING") {
                binding.searchBar.setText("[${buildingItem.name}] ")  // 태그 형식으로 설정
            } else {
                binding.searchBar.setText("${buildingItem.name} ")  // 일반 형식
            }
            moveCursorToEnd(binding.searchBar)
        }

        binding.searchListRecyclerview.adapter = adapter

        // ViewModel의 데이터 관찰
        viewModel.buildingItems.observe(viewLifecycleOwner, Observer { buildingItems ->
            adapter.setBuildingList(buildingItems)
        })

        binding.searchBar.addTextChangedListener { editable ->
            val searchText = editable.toString().trim()
            val filteredList = if (searchText.isNotBlank()) {
                filterBuildingList(searchText, viewModel.buildingItems.value ?: emptyList())
            } else {
                emptyList() // 검색어가 비어 있을 때 빈 목록 반환
            }
            adapter.setBuildingList(filteredList)
            binding.deleteTextButton.visibility = if (searchText.isNotEmpty()) View.VISIBLE else View.GONE
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun filterBuildingList(searchText: String, originalList: List<BuildingItem>): List<BuildingItem> {
        // 검색 텍스트에서 대괄호 [] 제거
        val cleanedSearchText = searchText.replace(Regex("\\[|\\]"), "")

        // 제거된 텍스트를 사용하여 목록 필터링
        return originalList.filter { building ->
            building.name.contains(cleanedSearchText, ignoreCase = true)
        }
    }

    private fun moveCursorToEnd(editText: EditText) {
        editText.setSelection(editText.text.length)
    }
}
