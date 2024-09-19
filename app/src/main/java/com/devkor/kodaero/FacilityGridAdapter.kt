package com.devkor.kodaero

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FacilityGridAdapter(
    private val facilities: List<BuildingDetailMainFacilityList>,
    private val onItemClick: (Int) -> Unit  // placeId를 전달할 수 있도록 수정
) : RecyclerView.Adapter<FacilityGridAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_building_detail_facilities, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val facility = facilities[position]
        holder.facilityName.text = facility.name
        if (facility.imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(facility.imageUrl)
                .into(holder.facilityImage)
        } else {
            holder.facilityImage.setImageDrawable(null)
        }

        // 아이템이 클릭되었을 때 placeId를 전달
        holder.itemView.setOnClickListener {
            onItemClick(facility.placeId)  // 클릭 시 해당 placeId를 콜백으로 전달
        }
    }

    override fun getItemCount(): Int = facilities.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val facilityImage: ImageView = view.findViewById(R.id.building_detail_facility_image)
        val facilityName: TextView = view.findViewById(R.id.building_detail_facility_name)
    }
}

