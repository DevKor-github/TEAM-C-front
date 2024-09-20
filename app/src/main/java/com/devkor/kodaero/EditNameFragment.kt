package com.devkor.kodaero

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.devkor.kodaero.databinding.FragmentEditNameBinding

class EditNameFragment : Fragment() {

    private var _binding: FragmentEditNameBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FetchDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditNameBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)

        binding.backToMypageButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().supportFragmentManager.popBackStack()
        }

        setupListeners()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username = arguments?.getString("username")
        binding.editNameText.hint = username ?: "닉네임을 입력하세요"
    }

    private fun setupListeners() {
        binding.editNameButton.setOnClickListener {
            val enteredText = binding.editNameText.text.toString()
            hideKeyboard()

            if (enteredText.isBlank()) {
                Toast.makeText(requireContext(), "수정할 닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                binding.editNameBlackLayout.visibility = View.VISIBLE
                binding.editNameLayout.visibility = View.VISIBLE
                binding.editNameLayoutText.text = enteredText
            }
        }

        binding.editNameYesButton.setOnClickListener {
            val newUsername = binding.editNameLayoutText.text.toString()

            viewModel.editUserName(newUsername)

            TokenManager.clearTokensAndUserInfo()

            val splashIntent = Intent(requireActivity(), SplashActivity::class.java)
            startActivity(splashIntent)

            requireActivity().finish()
        }

        binding.editNameNoButton.setOnClickListener {
            binding.editNameBlackLayout.visibility = View.GONE
            binding.editNameLayout.visibility = View.GONE
        }
    }

    fun isEditNameConfirmationVisible(): Boolean {
        return binding.editNameLayout.visibility == View.VISIBLE && binding.editNameBlackLayout.visibility == View.VISIBLE
    }

    fun hideEditNameConfirmationVisible() {
        binding.editNameBlackLayout.visibility = View.GONE
        binding.editNameLayout.visibility = View.GONE
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.editNameText.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
