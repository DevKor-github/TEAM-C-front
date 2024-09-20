package com.devkor.kodaero

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.devkor.kodaero.databinding.ActivityMainBinding
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager


class MainActivity : AppCompatActivity() {
    val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var backPressedTime: Long = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setBottomNavigationView()

        // 상태 바 텍스트와 아이콘을 어두운 색상으로 설정
        setStatusBarTextColor(isLightText = false)

        // 상태 바 투명화 및 콘텐츠 확장 설정
        window.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            statusBarColor = Color.TRANSPARENT
        }

        // 앱 초기 실행 시 홈화면으로 설정
        if (savedInstanceState == null) {
            binding.bottomNavigationView.selectedItemId = R.id.fragment_home
        }


    }

    override fun onBackPressed() {
        val fragmentManager: FragmentManager = supportFragmentManager
        // 현재 보여지고 있는 Fragment를 가져옵니다.
        val currentFragment = fragmentManager.findFragmentById(R.id.main_container)

        val homeFragment = fragmentManager.findFragmentById(R.id.home_container) as? HomeFragment

        val directionsFragment = supportFragmentManager.findFragmentByTag("DirectionFragment") as? GetDirectionsFragment


        when (currentFragment) {
            null -> {
                // HomeFragment인 경우는 이곳에 정의
                if (homeFragment != null) {
                    if (homeFragment.isBottomSheetExpanded()) {
                        homeFragment.closeModal()
                        Log.e("MainActivity", "expanded")
                        return
                    }
                }
            }

            is InnerMapFragment -> {
                if (currentFragment.isBottomSheetExpanded()) {
                    currentFragment.closeModal()
                    Log.e("MainActivity", "expanded")
                    return
                }
                Log.e("MainActivity", "innermapabcedf")
                if (directionsFragment != null && directionsFragment.currentRouteIndex != 0) {
                    Log.e("MainActivity", "{${directionsFragment}}")
                    directionsFragment.resetRouteStep()
                    fragmentManager.popBackStack("DirectionFragment", 0)
                    return
                }
            }

            is GetDirectionsFragment -> {
                fragmentManager.popBackStack("HomeFragment", 0)
                return
            }

            is PinSearchFragment -> {
                val pinSearchFragment = currentFragment as PinSearchFragment
                if (pinSearchFragment.hasSelectedMarkers()) {
                    pinSearchFragment.unselectAllMarkers()
                    return
                }
            }

            is MypageFragment -> {
                if (currentFragment.isLogoutConfirmationVisible()) {
                    currentFragment.hideLogoutConfirmation()
                    return
                }
            }

            is SuggestionFragment -> {
                if (currentFragment.isSuggestionSummitConfirmationVisible()) {
                    currentFragment.hideSuggestionSummitConfirmation()
                    return
                }
            }

            else -> {
                Log.e("MainActivity", "Doesn't have modal")
            }
        }

        // 백스택에 아무것도 없는지 확인
        if (fragmentManager.backStackEntryCount == 1) {
            // 마지막 뒤로가기 버튼 클릭 후 1초 이내에 다시 클릭 시 종료
            if (backPressedTime + 1000 > System.currentTimeMillis()) {
                finish() // 액티비티 종료
                // super.onBackPressed()
                return
            } else {
                Toast.makeText(this, "한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
            }
            backPressedTime = System.currentTimeMillis()
        } else {
            // 백스택에 항목이 있으면 일반적인 뒤로가기 동작 수행
            fragmentManager.popBackStack()
        }

    }

    fun setBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.main_container)

            when (item.itemId) {
                R.id.fragment_home -> {
                    if (currentFragment !is HomeFragment) {
                        // 백 스택에 HomeFragment가 있는지 확인
                        val fragmentInBackStack = supportFragmentManager.findFragmentByTag("HomeFragment")
                        if (fragmentInBackStack != null) {
                            // HomeFragment가 백 스택에 있는 경우, 그곳으로 이동
                            supportFragmentManager.popBackStack("HomeFragment", 0)
                        } else {
                            // HomeFragment가 백 스택에 없는 경우, 새로 추가
                            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.home_container, HomeFragment(), "HomeFragment")
                                .addToBackStack("HomeFragment")
                                .commit()
                        }
                        item.setIcon(R.drawable.home_button)
                    }
                    true
                }

                R.id.fragment_bus -> {
                    if (currentFragment !is BusFragment) {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.main_container, BusFragment(),"BusFragment")
                            .addToBackStack("BusFragment")
                            .commit()
                        item.setIcon(R.drawable.bus_button)
                    }
                    true
                }

                R.id.fragment_community -> {
                    if (currentFragment !is CommunityFragment) {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.main_container, CommunityFragment(),"CommunityFragment")
                            .addToBackStack("CommunityFragment")
                            .commit()
                        item.setIcon(R.drawable.community_button)
                    }
                    true
                }
                R.id.fragment_favorites -> {
                    if (currentFragment !is HomeFragment && currentFragment != null) {
                        // 백 스택에 HomeFragment가 있는지 확인
                        val fragmentInBackStack = supportFragmentManager.findFragmentByTag("HomeFragment")
                        if (fragmentInBackStack != null) {
                            // HomeFragment가 백 스택에 있는 경우, 그곳으로 이동
                            supportFragmentManager.popBackStack("HomeFragment", 0)
                        } else {
                            // HomeFragment가 백 스택에 없는 경우, 새로 추가
                            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.home_container, HomeFragment(), "HomeFragment")
                                .addToBackStack("HomeFragment")
                                .commit()
                        }

                        // 프래그먼트 전환 후 모달 열기
                        supportFragmentManager.addOnBackStackChangedListener(object : FragmentManager.OnBackStackChangedListener {
                            override fun onBackStackChanged() {
                                val newFragment = supportFragmentManager.findFragmentByTag("HomeFragment")
                                if (newFragment is HomeFragment) {
                                    // 모달 열기 함수 호출
                                    (newFragment as HomeFragment).openFavoriteModal()
                                    // 리스너 제거
                                    supportFragmentManager.removeOnBackStackChangedListener(this)
                                }
                            }
                        })

                        item.setIcon(R.drawable.favorites_button)
                    }

                    else{
                        val newFragment = supportFragmentManager.findFragmentByTag("HomeFragment")
                        (newFragment as HomeFragment).openFavoriteModal()
                        item.setIcon(R.drawable.favorites_button)
                    }
                    true
                }

                R.id.fragment_mypage -> {
                    if (currentFragment !is MypageFragment) {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.main_container, MypageFragment(),"MypageFragment")
                            .addToBackStack("MypageFragment")
                            .commit()
                        item.setIcon(R.drawable.mypage_button)
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun setStatusBarTextColor(isLightText: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wic = window.insetsController
            if (wic != null) {
                if (isLightText) {
                    // 상태 바 텍스트와 아이콘을 밝은 색상으로 설정
                    wic.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
                } else {
                    // 상태 바 텍스트와 아이콘을 어두운 색상으로 설정
                    wic.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }
            }
        } else {
            // Android 10 이하에서는 systemUiVisibility를 사용
            if (isLightText) {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

}