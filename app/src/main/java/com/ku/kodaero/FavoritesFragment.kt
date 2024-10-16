package com.ku.kodaero

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ku.kodaero.databinding.LayoutErrorBinding

class FavoritesFragment : Fragment() {

    // ViewBinding 변수 선언
    private var _binding: LayoutErrorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewBinding 인플레이트
        _binding = LayoutErrorBinding.inflate(inflater, container, false)
        val view = binding.root

        // 텍스트를 동적으로 수정
        binding.textView.text = "앗, 아직 개발 중이에요!\n" + "조금만 기다려 주세요 :("
        binding.errorBackButton.setOnClickListener {
            val mainActivity = activity as MainActivity
            mainActivity.binding.bottomNavigationView.selectedItemId = R.id.fragment_home

            requireActivity().supportFragmentManager.popBackStack("HomeFragment", 0)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val mainActivity = activity as MainActivity
        mainActivity.binding.bottomNavigationView.selectedItemId = R.id.fragment_home

        _binding = null
    }
}
