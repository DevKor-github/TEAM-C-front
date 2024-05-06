package com.example.deckor_teamc_front

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import com.example.deckor_teamc_front.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setBottomNavigationView()

        // 앱 초기 실행 시 홈화면으로 설정
        if (savedInstanceState == null) {
            binding.bottomNavigationView.selectedItemId = R.id.fragment_home
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // 프래그먼트로 터치 이벤트 전달
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is SearchBuildingFragment) {
                fragment.onTouchEvent(ev)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun setBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, HomeFragment()).commit()
                    item.setIcon(R.drawable.home_button)
                    true
                }
                R.id.fragment_bus -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, BusFragment()).commit()
                    item.setIcon(R.drawable.bus_button)
                    true
                }
                R.id.fragment_community -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, CommunityFragment()).commit()
                    item.setIcon(R.drawable.community_button)
                    true
                }
                R.id.fragment_favorites -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, FavoritesFragment()).commit()
                    item.setIcon(R.drawable.favorites_button)
                    true
                }
                R.id.fragment_mypage -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, MypageFragment()).commit()
                    item.setIcon(R.drawable.mypage_button)
                    true
                }
                else -> false
            }
        }
    }
}