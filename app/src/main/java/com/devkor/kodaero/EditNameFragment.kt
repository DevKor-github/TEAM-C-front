package com.devkor.kodaero

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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
        binding.editNameText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) {
                    binding.editNameButton.setBackgroundResource(R.drawable.rounded_rec_red)
                } else {
                    binding.editNameButton.setBackgroundResource(R.drawable.rounded_rec_gray)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

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

            binding.editNameLayout.visibility = View.GONE
            binding.editNameComplete.visibility = View.VISIBLE

            viewModel.editUserName(newUsername)

            viewModel.editUserNameResult.observe(viewLifecycleOwner, Observer { isSuccess ->
                if (isSuccess) {
                    var isUpdated = false

                    val handler = Handler(Looper.getMainLooper())
                    val checkUpdateRunnable = object : Runnable {
                        override fun run() {
                            if (isUpdated) return

                            checkTokensAndFetchUserInfo()

                            viewModel.userInfo.observe(viewLifecycleOwner, Observer { userInfo ->
                                if (userInfo?.username == newUsername) {
                                    isUpdated = true

                                    TokenManager.saveUserInfo(userInfo)

                                    val fragment = requireActivity().supportFragmentManager
                                        .findFragmentByTag("MypageFragment") as? MypageFragment

                                    fragment?.updateUserInfo(userInfo)

                                    handler.postDelayed({
                                        requireActivity().supportFragmentManager.popBackStack()
                                    }, 500)
                                } else {
                                    handler.postDelayed(this, 500)
                                }
                            })
                        }
                    }

                    handler.post(checkUpdateRunnable)
                } else {
                    Toast.makeText(requireContext(), "닉네임 수정 실패", Toast.LENGTH_SHORT).show()
                }
            })
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

    private fun checkTokensAndFetchUserInfo() {
        val accessToken = TokenManager.getAccessToken()
        val refreshToken = TokenManager.getRefreshToken()

        if (accessToken != null && refreshToken != null) {
            viewModel.fetchUserInfo()

            viewModel.userInfo.observe(viewLifecycleOwner, Observer { userInfo ->
                if (userInfo != null) {
                    TokenManager.saveUserInfo(userInfo)
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
