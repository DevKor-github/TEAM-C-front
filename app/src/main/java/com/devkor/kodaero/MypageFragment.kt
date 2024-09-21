package com.devkor.kodaero

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devkor.kodaero.databinding.FragmentMypageBinding
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.talk.TalkApiClient

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        val view = binding.root

        val userInfo = arguments?.getParcelable<UserInfo>("userInfo") ?: TokenManager.getUserInfo()

        userInfo?.let {
            binding.username.text = it.username

            when (it.provider) {
                "KAKAO" -> binding.userLogin.setImageResource(R.drawable.login_kakao)
                "GOOGLE" -> binding.userLogin.setImageResource(R.drawable.login_google)
                "NAVER" -> binding.userLogin.setImageResource(R.drawable.login_naver)
            }

            binding.userLevel.text = it.level

            binding.userLevelText.text = when (it.level) {
                "1" -> "갓난호랑이"
                "2" -> "아기호랑이"
                "3" -> "사춘기호랑이"
                "4" -> "MZ호랑이"
                "5" -> "어른호랑이"
                else -> "호랑이"
            }
        }

        binding.userImage.setOnClickListener {
            navigateToEditNameFragment()
        }

        binding.suggestionButton.setOnClickListener {
            navigateToSuggestionFragment()
        }

        binding.introductionButton.setOnClickListener {
            navigateToTeamIntroductionFragment()
        }

        binding.kodaeroKakaoButton.setOnClickListener {
            navigateToKakao()
        }

        binding.kodaeroInstarButton.setOnClickListener {
            navigateToInstar()
        }

        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.logoutYesButton.setOnClickListener {
            handleLogout()
        }

        binding.logoutNoButton.setOnClickListener {
            hideLogoutConfirmation()
        }

        return view
    }

    private fun navigateToEditNameFragment() {
        val editNameFragment = EditNameFragment()

        val bundle = Bundle()
        bundle.putString("username", binding.username.text.toString())
        editNameFragment.arguments = bundle

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(R.id.main_container, editNameFragment)
        transaction.addToBackStack("EditNameFragment")
        transaction.commit()
    }

    private fun navigateToSuggestionFragment() {
        val suggestionFragment = SuggestionFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(R.id.main_container, suggestionFragment)
        transaction.addToBackStack("SuggestionFragment")
        transaction.commit()
    }

    private fun navigateToTeamIntroductionFragment() {
        val teamIntroductionFragment = TeamIntroductionFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(R.id.main_container, teamIntroductionFragment)
        transaction.addToBackStack("TeamIntroductionFragment")
        transaction.commit()
    }

    private fun navigateToKakao() {
        val url = TalkApiClient.instance.addChannelUrl("_lzuhn")
        context?.let { KakaoCustomTabsClient.openWithDefault(it, url) }
    }

    private fun navigateToInstar() {
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/kodaero_ku/"))
        startActivity(intent)
    }

    private fun showLogoutConfirmation() {
        binding.logoutLayout.visibility = View.VISIBLE
        binding.logoutBlackLayout.visibility = View.VISIBLE
    }

    fun hideLogoutConfirmation() {
        binding.logoutLayout.visibility = View.GONE
        binding.logoutBlackLayout.visibility = View.GONE
    }

    fun isLogoutConfirmationVisible(): Boolean {
        return binding.logoutLayout.visibility == View.VISIBLE && binding.logoutBlackLayout.visibility == View.VISIBLE
    }

    private fun handleLogout() {
        TokenManager.clearTokensAndUserInfo()

        val loginIntent = Intent(requireActivity(), LoginActivity::class.java)
        startActivity(loginIntent)

        requireActivity().finish()
    }

    override fun onPause() {
        super.onPause()

        val mainActivity = activity as? MainActivity
        mainActivity?.binding?.bottomNavigationView?.selectedItemId = R.id.fragment_home
    }

}
