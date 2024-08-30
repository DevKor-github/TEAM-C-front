package com.devkor.kodaero

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
class SearchListAdapter(
    private var buildingList: List<BuildingSearchItem>,
    private val itemClick: (BuildingSearchItem) -> Unit
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
        return ViewHolder(view, parent.context)  // context를 ViewHolder로 전달
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val building = modifiedList[position]
        holder.itemView.setOnClickListener { itemClick(building) }

        when (holder) {
            is ViewHolder -> holder.bind(building)
        }
    }

    override fun getItemCount(): Int {
        return modifiedList.size
    }

    override fun getItemViewType(position: Int): Int {
        val building = modifiedList[position]
        return when (building.locationType) {
            "TAG" -> TYPE_TAG
            "BUILDING" -> TYPE_BUILDING
            else -> TYPE_ROOM
        }
    }

    inner class ViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
        private val buildingNameTextView: TextView = itemView.findViewById(R.id.building_name)
        private val buildingAddressTextView: TextView = itemView.findViewById(R.id.building_address)

        // ViewModelProvider를 사용해 ViewModel 가져오기
        private val viewModel: FetchDataViewModel = ViewModelProvider(
            context as ViewModelStoreOwner
        ).get(FetchDataViewModel::class.java)

        fun bind(building: BuildingSearchItem) {
            buildingNameTextView.text = building.name

            if (building.locationType == "CLASSROOM") {
                // ViewModel을 사용해 API 호출
                viewModel.fetchPlaceInfo(building.id) { placeInfo ->
                    placeInfo?.let {
                        if (it.detail != "."){
                            buildingAddressTextView.text = it.detail
                        }
                        else{
                            buildingAddressTextView.text = null
                        }
                    } ?: run {
                        buildingAddressTextView.text = null
                    }
                }
            } else {
                buildingAddressTextView.text = building.address
            }
        }
    }

    // 새로운 건물 목록을 설정하여 RecyclerView를 업데이트하는 메서드
    fun setBuildingList(newList: List<BuildingSearchItem>) {
        buildingList = newList
        modifiedList = getModifiedList()
        notifyDataSetChanged()
    }

    private fun getModifiedList(): List<BuildingSearchItem> {
        val modifiedList = mutableListOf<BuildingSearchItem>()
        for (building in buildingList) {
            if (building.locationType == "BUILDING" && false) {
                val modifiedBuilding = building.copy(name = "${building.name} ${Constants.TAG_SUFFIX}", locationType = "TAG")
                modifiedList.add(modifiedBuilding)
            }
            modifiedList.add(building)
        }
        return modifiedList
    }
}
