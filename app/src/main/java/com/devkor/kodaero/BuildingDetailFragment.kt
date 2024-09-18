package com.devkor.kodaero

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BuildingDetailFragment : Fragment() {

    private lateinit var viewModel: FetchDataViewModel
    private var selectedBuildingId: Int? = null

    private lateinit var selectedBuildingName: String
    private var selectedBuildingAboveFloor: Int? = null
    private var selectedBuildingUnderFloor: Int? = null

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
        val buildingDeadlineTextTextView = view.findViewById<TextView>(R.id.building_detail_deadline_text)
        val buildingOperatingStatusTextView = view.findViewById<TextView>(R.id.building_detail_operating_status)
        val facilityRecyclerView = view.findViewById<RecyclerView>(R.id.building_detail_facility_types)
        val buildingImageView = view.findViewById<ImageView>(R.id.building_detail_image)
        val operatingTimeLayout = view.findViewById<LinearLayout>(R.id.building_detail_operating_time_layout)
        val operatingTime1 = view.findViewById<TextView>(R.id.building_detail_operating_time_1)
        val operatingTime2 = view.findViewById<TextView>(R.id.building_detail_operating_time_2)
        val operatingTime3 = view.findViewById<TextView>(R.id.building_detail_operating_time_3)
        val showOperatingTimeLayout = view.findViewById<RelativeLayout>(R.id.show_operating_time_layout)
        val showOperatingTimeButton = view.findViewById<ImageButton>(R.id.show_operating_time_button)
        val innerMapButton = view.findViewById<Button>(R.id.modal_innermap_button)
        val modalDepartButton = view.findViewById<Button>(R.id.modal_depart_button)
        val modalArriveButton = view.findViewById<Button>(R.id.modal_arrive_button)
        val facilityButton = view.findViewById<Button>(R.id.building_detail_facility_button)
        val tmiButton = view.findViewById<Button>(R.id.building_detail_tmi_button)

        val facilityGridView = view.findViewById<RecyclerView>(R.id.building_detail_facilities_gridview)
        val tmiLayout = view.findViewById<RelativeLayout>(R.id.building_detail_tmi_layout)
        val tmiNameTextView = view.findViewById<TextView>(R.id.building_detail_tmi_name)
        val tmiDetailTextView = view.findViewById<TextView>(R.id.building_detail_tmi)

        operatingTimeLayout.visibility = View.GONE

        showOperatingTimeLayout.setOnClickListener {
            val isVisible = operatingTimeLayout.visibility == View.VISIBLE
            if (isVisible) {
                operatingTimeLayout.visibility = View.GONE
                showOperatingTimeButton.setImageResource(R.drawable.button_show_operating_time)
            } else {
                operatingTimeLayout.visibility = View.VISIBLE
                showOperatingTimeButton.setImageResource(R.drawable.button_hide_operating_time)
            }
        }

        facilityRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        facilityGridView.layoutManager = GridLayoutManager(context, 2)

        selectedBuildingId?.let { id ->
            val building = getBuildingInfo(id) ?: return

            selectedBuildingName = building.name
            selectedBuildingAboveFloor = building.floor
            selectedBuildingUnderFloor = building.underFloor

            viewModel.fetchBuildingDetail(id)
            viewModel.buildingDetail.observe(viewLifecycleOwner, Observer { buildingDetail ->
                buildingNameTextView.text = buildingDetail?.name
                buildingAddressTextView.text = buildingDetail?.address
                buildingDeadlineTextView.text = buildingDetail?.nextBuildingTime
                operatingTime1.text = buildingDetail?.weekdayOperatingTime
                operatingTime2.text = buildingDetail?.saturdayOperatingTime
                operatingTime3.text = buildingDetail?.sundayOperatingTime

                if (buildingDetail?.operating == true) {
                    buildingOperatingStatusTextView.text = "운영 중"
                    buildingDeadlineTextTextView.text = "에 운영 종료"
                } else {
                    buildingOperatingStatusTextView.text = "운영 종료"
                    buildingDeadlineTextTextView.text = "에 운영 시작"
                }

                buildingDetail?.imageUrl?.let { url ->
                    Glide.with(this)
                        .load(url)
                        .into(buildingImageView)
                }

                val facilityGridAdapter = FacilityGridAdapter(buildingDetail?.mainFacilityList ?: emptyList())
                facilityGridView.adapter = facilityGridAdapter

                tmiNameTextView.text = buildingDetail?.name
                tmiDetailTextView.text = buildingDetail?.details?.replace("\\n", "\n")

                val adapter = buildingDetail?.existTypes?.let { FacilityTypeAdapter(it, requireContext()) }
                facilityRecyclerView.adapter = adapter

                setActiveButton(facilityButton, tmiButton)
            })
        }

        innerMapButton.setOnClickListener {
            navigateToInnerMapFragment()
        }

        val prefix = "고려대학교 서울캠퍼스"

        modalDepartButton.setOnClickListener {
            val cleanedBuildingName = selectedBuildingName.removePrefix(prefix).trim()
            putBuildingDirectionsFragment(true, cleanedBuildingName, "BUILDING", selectedBuildingId)
        }

        modalArriveButton.setOnClickListener {
            val cleanedBuildingName = selectedBuildingName.removePrefix(prefix).trim()
            putBuildingDirectionsFragment(false, cleanedBuildingName, "BUILDING", selectedBuildingId)
        }

        facilityButton.setOnClickListener {
            setActiveButton(facilityButton, tmiButton)
            showLayout(facilityGridView, tmiLayout)
        }

        tmiButton.setOnClickListener {
            setActiveButton(tmiButton, facilityButton)
            showLayout(tmiLayout, facilityGridView)
        }
    }

    private fun setActiveButton(activeButton: Button, inactiveButton: Button) {
        activeButton.setBackgroundColor(resources.getColor(R.color.red))
        activeButton.setTextColor(resources.getColor(R.color.bright_gray))

        inactiveButton.setBackgroundColor(resources.getColor(R.color.white))
        inactiveButton.setTextColor(resources.getColor(R.color.black))
    }

    private fun showLayout(visibleLayout: View, invisibleLayout: View) {
        visibleLayout.visibility = View.VISIBLE
        invisibleLayout.visibility = View.GONE
    }

    private fun navigateToInnerMapFragment() {
        if (selectedBuildingAboveFloor != null && selectedBuildingUnderFloor != null && selectedBuildingId != null && selectedBuildingName != null) {
            val innerMapFragment = InnerMapFragment.newInstance(
                selectedBuildingName!!,
                selectedBuildingAboveFloor!!,
                selectedBuildingUnderFloor!!,
                selectedBuildingId!!
            )

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(R.id.main_container, innerMapFragment)
            transaction.addToBackStack("InnerMapFragment")
            transaction.commit()
        } else {
            Log.e("navigateToInnerMapFragment", "Missing one or more required arguments")
        }
    }

    private fun getBuildingInfo(buildingId: Int): BuildingItem? {
        val building = BuildingCache.get(buildingId)

        if (building != null) {
            return building
        } else {
            return null
        }
    }

    private fun putBuildingDirectionsFragment(isStartingPoint: Boolean, buildingName: String, placeType: String, id: Int?) {
        val getDirectionsFragment = GetDirectionsFragment().apply {
            arguments = Bundle().apply {
                putBoolean("isStartingPoint", isStartingPoint)
                putString("buildingName", buildingName)
                putString("placeType", placeType)
                if (id != null) {
                    putInt("placeId", id)
                }
            }
        }
        val activity = context as? FragmentActivity
        activity?.supportFragmentManager?.beginTransaction()
            ?.add(R.id.main_container, getDirectionsFragment,"DirectionFragment")
            ?.addToBackStack("DirectionFragment")
            ?.commit()
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

