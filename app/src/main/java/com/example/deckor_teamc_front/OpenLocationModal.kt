package com.example.deckor_teamc_front

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.launch

fun openLocationModal(activity: FragmentActivity, place: BuildingSearchItem) {

    try {
        // FragmentManager를 통해 HomeFragment를 찾거나 새로 생성
        val fragmentManager = activity.supportFragmentManager
        val homeFragment = fragmentManager.findFragmentByTag(HomeFragment::class.java.simpleName) as HomeFragment?
            ?: HomeFragment()

        // HomeFragment가 이미 존재하면 popBackStack을 통해 해당 프래그먼트로 이동
        if (homeFragment.isAdded) {
            fragmentManager.popBackStack(HomeFragment::class.java.simpleName, 0)
        } else {
            // 존재하지 않으면 HomeFragment를 추가
            fragmentManager.beginTransaction()
                .replace(R.id.main_container, homeFragment, HomeFragment::class.java.simpleName)
                .addToBackStack(HomeFragment::class.java.simpleName)
                .commit()
            fragmentManager.executePendingTransactions()
        }

        // 카메라 위치 이동 및 모달 열기 작업
        homeFragment.viewLifecycleOwner.lifecycleScope.launch {
            homeFragment.moveCameraToPosition(LatLng(place.latitude ?: 0.0, place.longitude ?: 0.0))
            homeFragment.view?.post {
                val includedLayout = homeFragment.view?.findViewById<View>(R.id.includedLayout)
                if (includedLayout == null) {
                    Log.e("openLocationModal", "Included layout not found in HomeFragment layout")
                    return@post
                }

                val nameTextView = includedLayout.findViewById<TextView>(R.id.building_name)
                val typeTextView = includedLayout.findViewById<TextView>(R.id.building_class)
                val addressTextView = includedLayout.findViewById<TextView>(R.id.building_address)

                if (nameTextView == null || typeTextView == null || addressTextView == null) {
                    Log.e("openLocationModal", "One of the TextViews not found")
                    return@post
                }

                nameTextView.text = place.name
                typeTextView.text = place.placeType
                addressTextView.text = place.address
            }
        }
    } catch (e: Exception) {
        Log.e("openLocationModal", "Error in openLocationModal", e)
    }
}