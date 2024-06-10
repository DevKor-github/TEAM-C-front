package com.example.deckor_teamc_front

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener
import com.example.deckor_teamc_front.databinding.FragmentGetDirectionsBinding
import com.naver.maps.geometry.LatLng

class GetDirectionsFragment : Fragment() {

    private var _binding: FragmentGetDirectionsBinding? = null
    private val binding get() = _binding!!

    private var startingPointHint: String? = null
    private var arrivalPointHint: String? = null
    private var startingPointLatLng: LatLng? = null
    private var arrivalPointLatLng: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentGetDirectionsBinding.inflate(inflater, container, false)

        val isStartingPointAssgined = arguments?.getBoolean("isStartingPoint") ?: true
        val buildingName = arguments?.getString("buildingName")

        if (isStartingPointAssgined) {
            startingPointHint = buildingName
            if (buildingName != null) {
                setHintWithStyle(binding.searchStartingPointBar, buildingName)
            }
        } else {
            arrivalPointHint = buildingName
            if (buildingName != null) {
                setHintWithStyle(binding.searchArrivalPointBar, buildingName)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().supportFragmentManager.popBackStack("HomeFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener("requestKey") { key, bundle ->
            val result = bundle.getString("bundleKey")
            val isStartingPoint = bundle.getBoolean("isStartingPoint")
            val latitude = bundle.getDouble("latitude")
            val longitude = bundle.getDouble("longitude")

            if (isStartingPoint) {
                startingPointHint = result
                startingPointLatLng = LatLng(latitude, longitude)
                if (result != null) {
                    setHintWithStyle(binding.searchStartingPointBar, result)
                }
            } else {
                arrivalPointHint = result
                arrivalPointLatLng = LatLng(latitude, longitude)
                if (result != null) {
                    setHintWithStyle(binding.searchArrivalPointBar, result)
                }
            }

        }

        binding.searchStartingPointBar.setOnClickListener {
            navigateToGetDirectionsSearchBuildingFragment(true)
        }

        binding.searchArrivalPointBar.setOnClickListener {
            navigateToGetDirectionsSearchBuildingFragment(false)
        }

        binding.backToHomeButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack("HomeFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        binding.switchButton.setOnClickListener {
            switchHints()
        }

        // Restore hints if they were previously set
        startingPointHint?.let {
            setHintWithStyle(binding.searchStartingPointBar, it)
        }

        arrivalPointHint?.let {
            setHintWithStyle(binding.searchArrivalPointBar, it)
        }

    }

    private fun switchHints() {
        val tempHint = startingPointHint
        startingPointHint = arrivalPointHint
        arrivalPointHint = tempHint

        val tempLatLng = startingPointLatLng
        startingPointLatLng = arrivalPointLatLng
        arrivalPointLatLng = tempLatLng

        binding.searchStartingPointBar.hint = startingPointHint ?: "출발지를 입력해주세요"
        binding.searchArrivalPointBar.hint = arrivalPointHint ?: "도착지를 입력해주세요"

        if (startingPointHint != null) {
            setHintWithStyle(binding.searchStartingPointBar, startingPointHint!!)
        } else {
            binding.searchStartingPointBar.setHintTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
        }

        if (arrivalPointHint != null) {
            setHintWithStyle(binding.searchArrivalPointBar, arrivalPointHint!!)
        } else {
            binding.searchArrivalPointBar.setHintTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
        }
    }

    private fun setHintWithStyle(button: AppCompatButton, hint: String) {
        val spannableString = SpannableString(hint).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        button.hint = spannableString
        button.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun navigateToGetDirectionsSearchBuildingFragment(isStartingPoint: Boolean) {
        val getDirectionsSearchBuildingFragment = GetDirectionsSearchBuildingFragment()
        getDirectionsSearchBuildingFragment.arguments = Bundle().apply {
            putBoolean("isStartingPoint", isStartingPoint)
        }
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(R.id.main_container, getDirectionsSearchBuildingFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
