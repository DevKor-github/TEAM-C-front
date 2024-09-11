package com.devkor.kodaero

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devkor.kodaero.databinding.FragmentMypageBinding

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        val view = binding.root

        // Load user information from TokenManager
        val userInfo = TokenManager.getUserInfo()

        userInfo?.let {
            binding.username.text = it.username

            // Set provider-specific icon
            when (it.provider) {
                "KAKAO" -> binding.userLogin.setImageResource(R.drawable.login_kakao)
                "GOOGLE" -> binding.userLogin.setImageResource(R.drawable.login_google)
                "NAVER" -> binding.userLogin.setImageResource(R.drawable.login_naver)
            }

            // Set user level
            binding.userLevel.text = it.level

            // Set level text based on user's level
            binding.userLevelText.text = when (it.level) {
                "LEVEL1" -> "갓난호랑이"
                "LEVEL2" -> "아기호랑이"
                "LEVEL3" -> "사춘기호랑이"
                "LEVEL4" -> "MZ호랑이"
                "LEVEL5" -> "어른호랑이"
                else -> "호랑이"
            }
        }

        // Handle introduction button click to navigate to TeamIntroductionFragment
        binding.introductionButton.setOnClickListener {
            navigateToTeamIntroductionFragment()
        }

        // Handle logout button click to show confirmation layout
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }

        // Handle "Yes" button in the logout confirmation
        binding.logoutYesButton.setOnClickListener {
            handleLogout()
        }

        // Handle "No" button in the logout confirmation to hide the confirmation layout
        binding.logoutNoButton.setOnClickListener {
            hideLogoutConfirmation()
        }

        return view
    }

    private fun navigateToTeamIntroductionFragment() {
        val teamIntroductionFragment = TeamIntroductionFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(R.id.main_container, teamIntroductionFragment)
        transaction.addToBackStack("TeamIntroductionFragment")
        transaction.commit()
    }

    private fun showLogoutConfirmation() {
        // Show the logout confirmation layout and the background overlay
        binding.logoutLayout.visibility = View.VISIBLE
        binding.logoutBlackLayout.visibility = View.VISIBLE
    }

    private fun hideLogoutConfirmation() {
        // Hide the logout confirmation layout and the background overlay
        binding.logoutLayout.visibility = View.GONE
        binding.logoutBlackLayout.visibility = View.GONE
    }

    private fun handleLogout() {
        // Clear tokens and user info from TokenManager
        TokenManager.clearTokensAndUserInfo()

        // Finish MainActivity and navigate to LoginActivity
        val loginIntent = Intent(requireActivity(), LoginActivity::class.java)
        startActivity(loginIntent)

        // Finish the current MainActivity to avoid going back to it
        requireActivity().finish()
    }

    override fun onPause() {
        super.onPause()

        // Set the bottom navigation state before the fragment is paused
        val mainActivity = activity as? MainActivity
        mainActivity?.binding?.bottomNavigationView?.selectedItemId = R.id.fragment_home
    }
}
