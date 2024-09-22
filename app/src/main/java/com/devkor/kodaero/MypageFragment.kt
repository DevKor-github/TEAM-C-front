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
            // 로그아웃 확인 다이얼로그 호출

            context?.let { it1 ->
                LogoutConfirmationDialog(it1) {
                    handleLogout() // 로그아웃 처리 함수 호출
                }
            }?.show()
        }


        binding.guideButton.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(R.id.main_container, GuideImageSliderFragment())
            transaction.addToBackStack(null)  // Optional: Adds the transaction to the back stack
            transaction.commit()
        }

        // 버튼 클릭 리스너 설정
        binding.developerButton.setOnClickListener {
            // 다이얼로그 호출

            // 다이얼로그 표시
            context?.let { it1 ->
                DeveloperAuthDialog(it1) { isVerified ->
                    if (isVerified) {
                        // 암호 검증 성공 시 실행할 코드
                        MainActivity.isNodeMaskBuild = true
                        // 필요한 다른 처리 로직을 여기에 추가
                    } else {
                        // 실패 시 추가 처리가 필요하면 여기에 작성
                    }
                }
            }?.show()
        }



        return view
    }

    fun updateUserInfo(userInfo: UserInfo) {
        binding.username.text = userInfo.username
        binding.userLevel.text = userInfo.level
        binding.userLevelText.text = when (userInfo.level) {
            "1" -> "갓난호랑이"
            "2" -> "아기호랑이"
            "3" -> "사춘기호랑이"
            "4" -> "MZ호랑이"
            "5" -> "어른호랑이"
            else -> "호랑이"
        }
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
