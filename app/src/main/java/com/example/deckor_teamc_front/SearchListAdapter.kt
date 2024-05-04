package com.example.deckor_teamc_front

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchListAdapter(private var buildingList: List<BuildingItem>) : RecyclerView.Adapter<SearchListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val building = buildingList[position]
        holder.bind(building)
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

    // 새로운 건물 목록을 설정하여 RecyclerView를 업데이트하는 메서드
    fun setBuildingList(newList: List<BuildingItem>) {
        buildingList = newList
        notifyDataSetChanged()
    }
}

