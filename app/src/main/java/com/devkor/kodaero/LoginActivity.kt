package com.devkor.kodaero

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    // Initialize ViewModel using Kotlin's viewModels delegate
    private val viewModel: FetchDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        KakaoSdk.init(this, "8f0c8f0fbe9c7aef1bb90ba2f1b4fae3")

        // Initialize TokenManager
        TokenManager.init(this)

        val googleLoginButton: ImageButton = findViewById(R.id.google_login_button)
        val naverLoginButton: ImageButton = findViewById(R.id.naver_login_button)
        val kakaoLoginButton: ImageButton = findViewById(R.id.kakao_login_button)


        setStatusBarTextColor(isLightText = false)

        window.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            statusBarColor = Color.TRANSPARENT
        }

        googleLoginButton.setOnClickListener {

        }

        naverLoginButton.setOnClickListener {

        }

        kakaoLoginButton.setOnClickListener {
            startKakaoLogin()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.userTokens.observe(this, Observer { userTokens ->
            userTokens?.let {
                Log.d(TAG, "AccessToken: ${it.accessToken}")
                Log.d(TAG, "RefreshToken: ${it.refreshToken}")

                // Store tokens in TokenManager
                TokenManager.saveTokens(it.accessToken, it.refreshToken)

                // After saving tokens, fetch user info
                viewModel.fetchUserInfo()

                // Observe user info and save it in TokenManager
                viewModel.userInfo.observe(this, Observer { userInfo ->
                    if (userInfo != null) {
                        // Save the user info in TokenManager
                        TokenManager.saveUserInfo(userInfo)

                        // Navigate to MainActivity
                        navigateToMainActivity()
                    } else {
                        Log.e(TAG, "Failed to fetch user info")
                    }
                })
            } ?: run {
                Log.e(TAG, "Failed to fetch tokens")
            }
        })
    }

    private val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "로그인 실패 $error")
        } else if (token != null) {
            Log.e(TAG, "로그인 성공")
            fetchUserInfoAndToken()
        }
    }

    private fun startKakaoLogin() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            // 카카오톡 로그인
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "로그인 실패 $error")
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    } else {
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = mCallback)
                    }
                } else if (token != null) {
                    Log.e(TAG, "로그인 성공")
                    fetchUserInfoAndToken()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = mCallback)
        }
    }

    // 사용자 정보를 요청하고 getUserToken을 실행하는 함수
    private fun fetchUserInfoAndToken() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패 $error")
            } else if (user != null) {
                val kakaoEmail = user.kakaoAccount?.email
                Log.e(TAG, "사용자 정보 요청 성공, 이메일 : $kakaoEmail")

                // 이메일이 있는 경우에 getUserToken 호출
                if (!kakaoEmail.isNullOrEmpty()) {
                    getUserToken("KAKAO", kakaoEmail, "")
                } else {
                    Log.e(TAG, "카카오 이메일을 찾을 수 없음.")
                }
            }
        }
    }

    // getUserToken 호출하여 ViewModel로 전달
    private fun getUserToken(provider: String, email: String, token: String) {
        viewModel.getUserTokens(provider, email, token)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setStatusBarTextColor(isLightText: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wic = window.insetsController
            if (wic != null) {
                if (isLightText) {
                    wic.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
                } else {
                    wic.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }
            }
        } else {
            if (isLightText) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}
