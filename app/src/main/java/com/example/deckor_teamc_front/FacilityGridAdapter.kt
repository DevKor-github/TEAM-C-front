package com.example.deckor_teamc_front

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FacilityGridAdapter(private val facilities: List<BuildingDetailMainFacilityList>) :
    RecyclerView.Adapter<FacilityGridAdapter.ViewHolder>() {

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
    }

    override fun getItemCount(): Int = facilities.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val facilityImage: ImageView = view.findViewById(R.id.building_detail_facility_image)
        val facilityName: TextView = view.findViewById(R.id.building_detail_facility_name)
    }
}
