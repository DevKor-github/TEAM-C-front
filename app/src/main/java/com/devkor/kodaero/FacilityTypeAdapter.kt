package com.devkor.kodaero

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView

class FacilityTypeAdapter(private val facilityTypes: List<String>) : RecyclerView.Adapter<FacilityTypeAdapter.FacilityTypeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityTypeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_building_detail_types, parent, false)
        return FacilityTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacilityTypeViewHolder, position: Int) {
        val facilityType = facilityTypes[position]
        val iconName = "icon_${facilityType.lowercase()}" // 아이콘 이름
        val resourceId = holder.itemView.context.resources.getIdentifier(iconName, "drawable", holder.itemView.context.packageName)
        holder.imageButton.setImageResource(resourceId)
    }

    override fun getItemCount(): Int {
        return facilityTypes.size
    }

    class FacilityTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageButton: ImageButton = itemView.findViewById(R.id.building_detail_type_item)
    }
}
