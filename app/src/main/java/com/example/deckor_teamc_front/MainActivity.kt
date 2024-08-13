package com.example.deckor_teamc_front

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.deckor_teamc_front.databinding.ActivityMainBinding
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior


class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var backPressedTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setBottomNavigationView()

        // 앱 초기 실행 시 홈화면으로 설정
        if (savedInstanceState == null) {
            binding.bottomNavigationView.selectedItemId = R.id.fragment_home
        }

    }

    override fun onBackPressed() {
        val fragmentManager: FragmentManager = supportFragmentManager
        // 현재 보여지고 있는 Fragment를 가져옵니다.
        val currentFragment = fragmentManager.findFragmentById(R.id.main_container)

        if (currentFragment is HomeFragment) {
            if (currentFragment.isBottomSheetExpanded()) {
                currentFragment.closeModal()
                Log.e("MainActivity", "expanded")
                return
            }
        } else if (currentFragment is InnerMapFragment) {
            if (currentFragment.isBottomSheetExpanded()) {
                currentFragment.closeModal()
                Log.e("MainActivity", "expanded")
                return
            }
        } else {
            Log.e("MainActivity", "Doesn't have modal")
        }

        // 백스택에 아무것도 없는지 확인
        if (fragmentManager.backStackEntryCount == 0) {
            // 마지막 뒤로가기 버튼 클릭 후 2초 이내에 다시 클릭 시 종료
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed()
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
            when (item.itemId) {
                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, HomeFragment(), "HomeFragment").commit()
                    item.setIcon(R.drawable.home_button)
                    true
                }

                R.id.fragment_bus -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, BusFragment()).commit()
                    item.setIcon(R.drawable.bus_button)
                    true
                }

                R.id.fragment_community -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, CommunityFragment()).commit()
                    item.setIcon(R.drawable.community_button)
                    true
                }

                R.id.fragment_favorites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, FavoritesFragment()).commit()
                    item.setIcon(R.drawable.favorites_button)
                    true
                }

                R.id.fragment_mypage -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, MypageFragment()).commit()
                    item.setIcon(R.drawable.mypage_button)

                    true
                }

                else -> false
            }
        }
    }
}