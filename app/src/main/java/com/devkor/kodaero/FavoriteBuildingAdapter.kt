package com.devkor.kodaero

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoriteBuildingAdapter(
    private var items: MutableList<FavoriteBuildingItem>,
    private val onDeleteClick: (Int) -> Unit, // 북마크 ID를 전달하는 삭제 리스너
    private val onItemClick: (Int) -> Unit // Building ID를 전달하는 아이템 클릭 리스너
) : RecyclerView.Adapter<FavoriteBuildingAdapter.BuildingViewHolder>() {

    // 뷰홀더 클래스 정의
    class BuildingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val buildingName: TextView = view.findViewById(R.id.building_name)
        val buildingAddress: TextView = view.findViewById(R.id.building_address)
        val deleteButton: TextView = view.findViewById(R.id.building_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuildingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.favorite_list_building, parent, false)
        return BuildingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuildingViewHolder, position: Int) {
        val item = items[position]
        holder.buildingName.text = item.buildingName
        holder.buildingAddress.text = item.buildingAddress

        // 삭제 버튼 클릭 리스너 설정
        holder.deleteButton.setOnClickListener {
            onDeleteClick(item.bookmarkId)
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
        }

        // 아이템 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            onItemClick(item.buildingId) // buildingId를 전달하여 콜백 호출
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // 데이터를 업데이트하는 메서드
    fun updateItems(newItems: MutableList<FavoriteBuildingItem>) {
        // 기존 아이템과 중복되지 않는 새 아이템만 추가
        val uniqueItems = newItems.filter { newItem ->
            items.none { it.buildingId == newItem.buildingId && it.bookmarkId == newItem.bookmarkId }
        }
        items.addAll(uniqueItems)
        notifyDataSetChanged()
    }
}



data class FavoriteBuildingItem(
    val buildingId: Int,
    val buildingName: String,
    val buildingAddress: String,
    val bookmarkId: Int
)
