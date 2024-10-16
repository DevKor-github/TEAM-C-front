package com.ku.kodaero

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ku.kodaero.databinding.FragmentSecessionBinding

class SecessionFragment : Fragment() {

    private var _binding: FragmentSecessionBinding? = null
    private val binding get() = _binding!!
    private var secessionCheck = false

    private lateinit var viewModel: FetchDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecessionBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)

        updateCheckState()

        binding.backToMypageButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        binding.secessionCancelButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.secessionCheck.setOnClickListener {
            secessionCheck = !secessionCheck
            updateCheckState()
        }

        binding.secessionButton.setOnClickListener {
            if (secessionCheck) {
                context?.let { it1 ->
                    SecessionConfirmationDialog(it1) {
                        handleSecession()
                    }
                }?.show()
            } else {
                Toast.makeText(context, "탈퇴에 동의하셔야 합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun updateCheckState() {
        binding.secessionCheckIcon.visibility = if (secessionCheck) View.VISIBLE else View.GONE
    }

    private fun handleSecession() {
        viewModel.secessionUser()

        viewModel.secessionComplete.observe(viewLifecycleOwner) { isComplete ->
            if (isComplete) {
                TokenManager.clearTokensAndUserInfo()
                val splashIntent = Intent(requireActivity(), SplashActivity::class.java)
                startActivity(splashIntent)
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Secession failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
