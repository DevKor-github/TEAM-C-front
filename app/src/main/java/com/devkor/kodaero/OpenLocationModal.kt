package com.devkor.kodaero

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun openLocationModal(activity: FragmentActivity, place: BuildingSearchItem) {

    try {
        // FragmentManager를 통해 HomeFragment를 찾거나 새로 생성
        val fragmentManager = activity.supportFragmentManager
        val homeFragment = fragmentManager.findFragmentByTag("HomeFragment") as HomeFragment?
            ?: HomeFragment()



        // HomeFragment가 이미 존재하면 popBackStack을 통해 해당 프래그먼트로 이동
        if (homeFragment.isAdded) {
            Log.d("openLocationModal", "HomeFragment already exists")
            fragmentManager.popBackStack("HomeFragment", 0)
        } else {
            // 존재하지 않으면 HomeFragment를 추가
            fragmentManager.beginTransaction()
                .replace(R.id.main_container, homeFragment, HomeFragment::class.java.simpleName)
                .addToBackStack("HomeFragment")
                .commit()
            fragmentManager.executePendingTransactions()
        }

        // 카메라 위치 이동 및 모달 열기 작업
        homeFragment.viewLifecycleOwner.lifecycleScope.launch {
            homeFragment.moveCameraToPosition(LatLng(place.latitude ?: 38.0, place.longitude ?: 127.0))
            homeFragment.view?.post {
                val includedLayout = homeFragment.view?.findViewById<View>(R.id.includedLayout)
                if (includedLayout == null) {
                    Log.e("openLocationModal", "Included layout not found in HomeFragment layout")
                    return@post
                }
            }
            delay(100)
            place.id?.let { homeFragment.openBuildingModal(it) }
        }
    } catch (e: Exception) {
        Log.e("openLocationModal", "Error in openLocationModal", e)
    }
}