package com.devkor.kodaero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.devkor.kodaero.databinding.ActivityMainBinding
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager


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

        val directionsFragment = supportFragmentManager.findFragmentByTag("DirectionFragment") as? GetDirectionsFragment

        val backStackCount = fragmentManager.backStackEntryCount

        for (i in 0 until backStackCount) {
            val backStackEntry = fragmentManager.getBackStackEntryAt(i)
            Log.d("BackStackFragment", "Fragment name: ${backStackEntry.name}, ID: ${backStackEntry.id}")
        }

        val fragments = fragmentManager.fragments
        for (fragment in fragments) {
            fragment?.let {
                Log.d("BackStackFragment", "Fragment tag: ${fragment.tag}, Fragment: ${fragment::class.java.simpleName}")
            }
        }

        when (currentFragment) {
            is HomeFragment -> {
                if (currentFragment.isBottomSheetExpanded()) {
                    currentFragment.closeModal()
                    Log.e("MainActivity", "expanded")
                    return
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
                                .replace(R.id.main_container, HomeFragment(), "HomeFragment")
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
                    if (currentFragment !is FavoritesFragment) {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.main_container, FavoritesFragment(),"FavoritesFragment")
                            .addToBackStack("FavoritesFragment")
                            .commit()
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

}