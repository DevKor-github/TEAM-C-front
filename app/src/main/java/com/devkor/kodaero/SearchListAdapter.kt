package com.devkor.kodaero

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchListAdapter(
    private var buildingList: List<BuildingSearchItem>,
    private val itemClick: (BuildingSearchItem) -> Unit  // 클릭 리스너를 위한 함수 타입 매개변수 추가
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_BUILDING = 0
        private const val TYPE_TAG = 1
        private const val TYPE_ROOM = 2
    }

    private var modifiedList = getModifiedList()  // 수정된 리스트를 저장하는 변수

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = when (viewType) {
            TYPE_TAG -> LayoutInflater.from(parent.context).inflate(R.layout.search_list_tag, parent, false)
            TYPE_BUILDING -> LayoutInflater.from(parent.context).inflate(R.layout.search_list_building, parent, false)
            else -> LayoutInflater.from(parent.context).inflate(R.layout.search_list_room, parent, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val building = modifiedList[position]
        holder.itemView.setOnClickListener { itemClick(building) }  // 클릭 리스너 설정

        when (holder) {
            is ViewHolder -> holder.bind(building)
        }
    }

    override fun getItemCount(): Int {
        return modifiedList.size
    }

    override fun getItemViewType(position: Int): Int {
        val building = modifiedList[position]
        return when (building.placeType) {
            "TAG" -> TYPE_TAG
            "BUILDING" -> TYPE_BUILDING
            else -> TYPE_ROOM
        }
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
        modifiedList = getModifiedList()
        notifyDataSetChanged()
    }

    // BUILDING 타입의 아이템을 수정하여 리스트에 추가하는 메서드

    private fun getModifiedList(): List<BuildingSearchItem> {
        val modifiedList = mutableListOf<BuildingSearchItem>()
        for (building in buildingList) {
            if (building.placeType == "BUILDING" && false) { // 태그 미사용
                val modifiedBuilding = building.copy(name = "${building.name} ${Constants.TAG_SUFFIX}", placeType = "TAG")
                modifiedList.add(modifiedBuilding)
            }
            modifiedList.add(building)
        }
        return modifiedList
    }
}
