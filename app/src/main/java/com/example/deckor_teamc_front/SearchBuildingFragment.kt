package com.example.deckor_teamc_front

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deckor_teamc_front.databinding.FragmentSearchBuildingBinding

class SearchBuildingFragment : Fragment() {

    private var _binding: FragmentSearchBuildingBinding? = null
    private val binding get() = _binding!!

    private lateinit var originalBuildingList: List<BuildingItem>
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

        adapter = SearchListAdapter(emptyList()) // 빈 목록으로 초기화
        binding.searchListRecyclerview.adapter = adapter

        // RecyclerView에 어댑터 설정 및 데이터 바인딩
        originalBuildingList = listOf(
            BuildingItem("고려대학교 서울캠퍼스애기능생활관", "서울 성북구 안암로 73-15", "503m"),
            BuildingItem("고려대학교 서울캠퍼스애기능생활관 학생식당", "서울 성북구 안암로 73-15", "503m"),
            BuildingItem("고려대학교 서울캠퍼스애기능생활관 101호", "서울 성북구 안암로 73-15", "503m"),
            BuildingItem("고려대학교 서울캠퍼스애기능생활관 102호", "서울 성북구 안암로 73-15", "503m"),
            BuildingItem("고려대학교 서울캠퍼스애기능생활관 103호", "서울 성북구 안암로 73-15", "503m"),
        )

        binding.searchBar.addTextChangedListener { editable ->
            val searchText = editable.toString().trim()
            val filteredList = if (searchText.isNotBlank()) {
                filterBuildingList(searchText)
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

    private fun filterBuildingList(searchText: String): List<BuildingItem> {
        return originalBuildingList.filter { building ->
            building.name.contains(searchText, true)
        }
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        return true
    }
}
