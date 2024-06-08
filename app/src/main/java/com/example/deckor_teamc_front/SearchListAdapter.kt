package com.example.deckor_teamc_front

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchListAdapter(
    private var buildingList: List<BuildingSearchItem>,
    private val itemClick: (BuildingSearchItem) -> Unit  // 클릭 리스너를 위한 함수 타입 매개변수 추가
) : RecyclerView.Adapter<SearchListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val building = buildingList[position]
        holder.itemView.setOnClickListener { itemClick(building) }  // 클릭 리스너 설정
        holder.bind(building)
    }

    override fun getItemCount(): Int {
        return buildingList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val buildingNameTextView: TextView = itemView.findViewById(R.id.building_name)
        private val buildingAddressTextView: TextView = itemView.findViewById(R.id.building_address)

        fun bind(building: BuildingSearchItem) {
            buildingNameTextView.text = building.name
            buildingAddressTextView.text = building.address
        }
    }

    // 새로운 건물 목록을 설정하여 RecyclerView를 업데이트하는 메서드
    fun setBuildingList(newList: List<BuildingSearchItem>) {
        buildingList = newList
        notifyDataSetChanged()
    }
}
