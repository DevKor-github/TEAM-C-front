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
import com.example.deckor_teamc_front.databinding.FragmentGetDirectionsBinding
import com.naver.maps.geometry.LatLng

class GetDirectionsFragment : Fragment() {

    private var _binding: FragmentGetDirectionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var originalBuildingList: List<BuildingItem>
    private lateinit var adapter: GetDirectionsAdapter

    private var selectedStartLatLng: LatLng? = null
    private var selectedArrivalLatLng: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGetDirectionsBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.backToHomeButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.deleteTextButton1.setOnClickListener {
            binding.searchStartingPointBar.text.clear()
            selectedStartLatLng = null
        }

        binding.deleteTextButton2.setOnClickListener {
            binding.searchArrivalPointBar.text.clear()
            selectedArrivalLatLng = null
        }

        binding.switchButton.setOnClickListener {
            val startingPointText = binding.searchStartingPointBar.text.toString()
            val arrivalPointText = binding.searchArrivalPointBar.text.toString()
            binding.searchStartingPointBar.setText(arrivalPointText)
            binding.searchArrivalPointBar.setText(startingPointText)

            // Swap the LatLng values as well
            val tempLatLng = selectedStartLatLng
            selectedStartLatLng = selectedArrivalLatLng
            selectedArrivalLatLng = tempLatLng

            checkAndNavigate()
        }

        // RecyclerView 설정
        val layoutManager = LinearLayoutManager(requireContext())
        binding.getDirectionsListRecyclerview.layoutManager = layoutManager

        // Initialize the adapter with an item click listener
        adapter = GetDirectionsAdapter(emptyList()) { buildingItem ->
            onBuildingItemSelected(buildingItem)
        }
        binding.getDirectionsListRecyclerview.adapter = adapter

        // Initialize building list
        originalBuildingList = listOf(
            BuildingItem("고려대학교 서울캠퍼스애기능생활관", "서울 성북구 안암로 73-15", "503m", LatLng(37.5844, 127.0274)),
            BuildingItem("고려대학교 서울캠퍼스우당교양관", "서울 성북구 고려대로 104 105", "479m", LatLng(37.5869, 127.0314)),
        )

        binding.searchStartingPointBar.addTextChangedListener { editable ->
            val searchText = editable.toString().trim()
            val filteredList = if (searchText.isNotBlank()) {
                filterBuildingList(searchText)
            } else {
                emptyList() // 검색어가 비어 있을 때 빈 목록 반환
            }
            adapter.setBuildingList(filteredList)
            binding.deleteTextButton1.visibility = if (searchText.isNotEmpty()) View.VISIBLE else View.GONE
        }

        binding.searchArrivalPointBar.addTextChangedListener { editable ->
            val searchText = editable.toString().trim()
            val filteredList = if (searchText.isNotBlank()) {
                filterBuildingList(searchText)
            } else {
                emptyList() // 검색어가 비어 있을 때 빈 목록 반환
            }
            adapter.setBuildingList(filteredList)
            binding.deleteTextButton2.visibility = if (searchText.isNotEmpty()) View.VISIBLE else View.GONE
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

    private fun onBuildingItemSelected(buildingItem: BuildingItem) {
        // Here, you can set the selected item's name to the currently focused EditText
        val currentFocus = requireActivity().currentFocus
        if (currentFocus == binding.searchStartingPointBar) {
            binding.searchStartingPointBar.setText(buildingItem.name)
            selectedStartLatLng = buildingItem.location
        } else if (currentFocus == binding.searchArrivalPointBar) {
            binding.searchArrivalPointBar.setText(buildingItem.name)
            selectedArrivalLatLng = buildingItem.location
        }
        checkAndNavigate()
    }

    private fun checkAndNavigate() {
        if (selectedStartLatLng != null && selectedArrivalLatLng != null) {
            navigateToHomeFragment()
        }
    }

    private fun navigateToHomeFragment() {
        val homeFragment = HomeFragment()
        val bundle = Bundle().apply {
            putParcelable("start_lat_lng", selectedStartLatLng)
            putParcelable("arrival_lat_lng", selectedArrivalLatLng)
            putBoolean("stop_tracking", true)
        }
        homeFragment.arguments = bundle

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_container, homeFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        return true
    }
}
