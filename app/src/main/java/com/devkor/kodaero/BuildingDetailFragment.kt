package com.devkor.kodaero

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BuildingDetailFragment : Fragment() {

    private lateinit var viewModel: FetchDataViewModel
    private var selectedBuildingId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedBuildingId = it.getInt("buildingId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_building_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)

        val buildingNameTextView = view.findViewById<TextView>(R.id.building_detail_name)
        val buildingAddressTextView = view.findViewById<TextView>(R.id.building_detail_address)
        val buildingDeadlineTextView = view.findViewById<TextView>(R.id.building_detail_deadline)
        val buildingOperatingStatusTextView = view.findViewById<TextView>(R.id.building_detail_operating_status)
        val facilityRecyclerView = view.findViewById<RecyclerView>(R.id.building_detail_facility_types)
        val buildingImageView = view.findViewById<ImageView>(R.id.building_detail_image)
        val operatingTimeLayout = view.findViewById<LinearLayout>(R.id.building_detail_operating_time_layout)
        val showOperatingTimeButton = view.findViewById<ImageButton>(R.id.show_operating_time_button)
        val facilityButton = view.findViewById<Button>(R.id.building_detail_facility_button)
        val communityButton = view.findViewById<Button>(R.id.building_detail_community_button)
        val tmiButton = view.findViewById<Button>(R.id.building_detail_tmi_button)

        val facilityGridView = view.findViewById<RecyclerView>(R.id.building_detail_facilities_gridview)
        val communityLayout = view.findViewById<ScrollView>(R.id.building_detail_community_layout)
        val tmiLayout = view.findViewById<ScrollView>(R.id.building_detail_tmi_layout)
        val tmiNameTextView = view.findViewById<TextView>(R.id.building_detail_tmi_name)
        val tmiDetailTextView = view.findViewById<TextView>(R.id.building_detail_tmi)

        // 기본적으로 운영 시간을 숨김
        operatingTimeLayout.visibility = View.GONE

        // 운영 시간 버튼 클릭 시 토글
        showOperatingTimeButton.setOnClickListener {
            val isVisible = operatingTimeLayout.visibility == View.VISIBLE
            if (isVisible) {
                operatingTimeLayout.visibility = View.GONE
                showOperatingTimeButton.setImageResource(R.drawable.button_show_operating_time)
            } else {
                operatingTimeLayout.visibility = View.VISIBLE
                showOperatingTimeButton.setImageResource(R.drawable.button_hide_operating_time)
            }
        }

        // 리사이클러뷰 설정
        facilityRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        facilityGridView.layoutManager = GridLayoutManager(context, 2)

        selectedBuildingId?.let { id ->
            viewModel.fetchBuildingDetail(id)
            viewModel.buildingDetail.observe(viewLifecycleOwner, Observer { buildingDetail ->
                buildingNameTextView.text = buildingDetail?.name
                buildingAddressTextView.text = buildingDetail?.address
                buildingDeadlineTextView.text = buildingDetail?.nextBuildingTime

                if (buildingDetail?.operating == true) {
                    buildingOperatingStatusTextView.text = "운영 중"
                } else {
                    buildingOperatingStatusTextView.text = "운영 종료"
                }

                // Load the image using Glide
                buildingDetail?.imageUrl?.let { url ->
                    Glide.with(this)
                        .load(url)
                        .into(buildingImageView)
                }

                // Set up facility grid view adapter
                val facilityGridAdapter = FacilityGridAdapter(buildingDetail?.mainFacilityList ?: emptyList())
                facilityGridView.adapter = facilityGridAdapter

                // Set TMI details
                tmiNameTextView.text = buildingDetail?.name
                tmiDetailTextView.text = buildingDetail?.details?.replace("\\n", "\n")

                // Set up horizontal facility types
                val adapter = buildingDetail?.existTypes?.let { FacilityTypeAdapter(it) }
                facilityRecyclerView.adapter = adapter

                // Set the initial active button and display
                setActiveButton(facilityButton, communityButton, tmiButton)
            })
        }

        // 버튼 클릭 시 활성화 상태 변경 및 레이아웃 전환
        facilityButton.setOnClickListener {
            setActiveButton(facilityButton, communityButton, tmiButton)
            showLayout(facilityGridView, communityLayout, tmiLayout)
        }

        communityButton.setOnClickListener {
            setActiveButton(communityButton, facilityButton, tmiButton)
            showLayout(communityLayout, facilityGridView, tmiLayout)
        }

        tmiButton.setOnClickListener {
            setActiveButton(tmiButton, facilityButton, communityButton)
            showLayout(tmiLayout, facilityGridView, communityLayout)
        }
    }

    private fun setActiveButton(activeButton: Button, inactiveButton1: Button, inactiveButton2: Button) {
        activeButton.setBackgroundColor(resources.getColor(R.color.red))
        activeButton.setTextColor(resources.getColor(R.color.bright_gray))

        inactiveButton1.setBackgroundColor(resources.getColor(R.color.white))
        inactiveButton1.setTextColor(resources.getColor(R.color.black))

        inactiveButton2.setBackgroundColor(resources.getColor(R.color.white))
        inactiveButton2.setTextColor(resources.getColor(R.color.black))
    }

    private fun showLayout(visibleLayout: View, vararg invisibleLayouts: View) {
        visibleLayout.visibility = View.VISIBLE
        invisibleLayouts.forEach { it.visibility = View.GONE }
    }

    companion object {
        @JvmStatic
        fun newInstance(buildingId: Int) = BuildingDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("buildingId", buildingId)
            }
        }
    }
}

