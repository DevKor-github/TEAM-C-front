package com.example.deckor_teamc_front

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    fun setBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, HomeFragment()).commit()
                    true
                }
                R.id.fragment_bus -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, BusFragment()).commit()
                    true
                }
                R.id.fragment_community -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, CommunityFragment()).commit()
                    true
                }
                R.id.fragment_favorites -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, FavoritesFragment()).commit()
                    true
                }
                R.id.fragment_mypage -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, MypageFragment()).commit()
                    true
                }
                else -> false
            }
        }
    }
}