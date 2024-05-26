package com.example.deckor_teamc_front

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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

        binding.backToHomeButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.deleteTextButton.setOnClickListener {
            binding.searchBar.setText("")
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.searchListRecyclerview.layoutManager = layoutManager

        adapter = SearchListAdapter(emptyList()) { buildingItem ->
            if (buildingItem.placeType == "BUILDING") {
                binding.searchBar.setText("[${buildingItem.name}] ")
            } else {
                binding.searchBar.setText("${buildingItem.name} ")
            }
            moveCursorToEnd(binding.searchBar)
        }

        binding.searchListRecyclerview.adapter = adapter

        viewModel.buildingItems.observe(viewLifecycleOwner, Observer { buildingItems ->
            adapter.setBuildingList(buildingItems)
        })

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                if (searchText.isNotBlank()) {
                    viewModel.searchBuildings(searchText)
                } else {
                    adapter.setBuildingList(emptyList())
                }
                binding.deleteTextButton.visibility = if (searchText.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Add this block to open the keyboard when the fragment is created
        binding.searchBar.requestFocus()
        binding.searchBar.postDelayed({
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchBar, InputMethodManager.SHOW_IMPLICIT)
        }, 100)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun moveCursorToEnd(editText: EditText) {
        editText.setSelection(editText.text.length)
    }
}
