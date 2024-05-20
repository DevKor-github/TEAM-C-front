package com.example.deckor_teamc_front

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GetDirectionsAdapter(
    private var buildingList: List<BuildingItem>,
    private val itemClickListener: (BuildingItem) -> Unit
) : RecyclerView.Adapter<GetDirectionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val building = buildingList[position]
        holder.bind(building)
        holder.itemView.setOnClickListener { itemClickListener(building) }
    }

    override fun getItemCount(): Int {
        return buildingList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val buildingNameTextView: TextView = itemView.findViewById(R.id.building_name)
        private val buildingAddressTextView: TextView = itemView.findViewById(R.id.building_address)
        private val buildingDistanceTextView: TextView = itemView.findViewById(R.id.building_distance)

        fun bind(building: BuildingItem) {
            buildingNameTextView.text = building.name
            buildingAddressTextView.text = building.address
            buildingDistanceTextView.text = building.distance
        }
    }

    fun setBuildingList(newList: List<BuildingItem>) {
        buildingList = newList
        notifyDataSetChanged()
    }
}
