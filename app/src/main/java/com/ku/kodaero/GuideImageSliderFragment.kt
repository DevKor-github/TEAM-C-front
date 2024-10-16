package com.ku.kodaero

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

class GuideImageSliderFragment : Fragment() {

    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guide_image_slider, container, false)
        viewPager = view.findViewById(R.id.viewPager)

        Log.e("fefsfwfe","")

        val images = listOf(
            R.drawable.image_guide_page_1,
            R.drawable.image_guide_page_2,
            R.drawable.image_guide_page_3
        )

        val adapter = ImageSliderAdapter(images)
        viewPager.adapter = adapter

        return view
    }
}
