package com.devkor.kodaero

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView

class FacilityTypeAdapter(facilityTypes: List<String>, private val context: Context) : RecyclerView.Adapter<FacilityTypeAdapter.FacilityTypeViewHolder>() {

    private val filteredFacilityTypes = facilityTypes.filter { facilityType ->
        val iconName = "icon_${facilityType.lowercase()}"
        val resourceId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
        resourceId != 0 // Only include items with a valid drawable resource
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityTypeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_building_detail_types, parent, false)
        return FacilityTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacilityTypeViewHolder, position: Int) {
        val facilityType = filteredFacilityTypes[position]
        val iconName = "icon_${facilityType.lowercase()}" // Icon name
        val resourceId = holder.itemView.context.resources.getIdentifier(iconName, "drawable", holder.itemView.context.packageName)

        holder.imageButton.setImageResource(resourceId)
    }

    override fun getItemCount(): Int {
        return filteredFacilityTypes.size
    }

    class FacilityTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageButton: ImageButton = itemView.findViewById(R.id.building_detail_type_item)
    }
}
