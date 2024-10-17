package com.ku.kodaero

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ku.kodaero.databinding.FragmentSearchBuildingBinding
import kotlinx.coroutines.*

class GetDirectionsSearchBuildingFragment : Fragment() {

    private var _binding: FragmentSearchBuildingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FetchDataViewModel by viewModels()
    private lateinit var adapter: SearchListAdapter

    private var taggedBuildingId: Int? = null
    private var isStartingPoint: Boolean = false

    private lateinit var locationHelper: LocationHelper

    private var isToastVisible = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBuildingBinding.inflate(inflater, container, false)
        val view = binding.root



        isStartingPoint = arguments?.getBoolean("isStartingPoint") ?: false

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
            if (buildingItem.locationType == "TAG") {
                addTag(buildingItem.name)
                binding.customEditTextLayout.editText.setText("")
                binding.customEditTextLayout.editText.hint = "건물 내 장소를 입력하세요"
            } else {
                buildingItem.id?.let {
                    returnToGetDirectionsFragment(buildingItem.name, buildingItem.locationType,
                        it, 0.0, 0.0)
                }

            }
            taggedBuildingId = buildingItem.id
        }

        binding.searchListRecyclerview.adapter = adapter

        viewModel.buildingSearchItems.observe(viewLifecycleOwner, Observer { buildingItems ->
            adapter.setBuildingList(buildingItems)
        })

        binding.customEditTextLayout.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                if (searchText.isNotBlank()) {
                    viewModel.searchBuildings(searchText, taggedBuildingId)
                } else {
                    adapter.setBuildingList(emptyList())
                }
                binding.customEditTextLayout.clearButton.visibility = if (searchText.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.customEditTextLayout.editText.requestFocus()
        binding.customEditTextLayout.editText.postDelayed({
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.customEditTextLayout.editText, InputMethodManager.SHOW_IMPLICIT)
        }, 100)

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.setOnClickListener {
            if (binding.customEditTextLayout.editText.hasFocus()) {
                hideKeyboard(binding.customEditTextLayout.editText)
                binding.customEditTextLayout.editText.clearFocus()
            }
        }

        binding.searchListRecyclerview.setOnTouchListener { _, _ ->
            if (binding.customEditTextLayout.editText.hasFocus()) {
                hideKeyboard(binding.customEditTextLayout.editText)
                binding.customEditTextLayout.editText.clearFocus()
            }
            false
        }
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
            if (tagContainer.childCount == 0) {
                binding.customEditTextLayout.editText.hint = "학교 건물을 검색해 주세요"
            }
        }

        tagContainer.addView(tagView)
    }

    private fun returnToGetDirectionsFragment(buildingName: String, placeType: String, id: Int, lat: Double, lng: Double) {
        setFragmentResult("requestKey", Bundle().apply {
            putString("buildingName", buildingName)
            putBoolean("isStartingPoint", isStartingPoint)
            putString("placeType", placeType)
            putInt("id", id)
            putDouble("lat", lat)
            putDouble("lng", lng)
        })
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
