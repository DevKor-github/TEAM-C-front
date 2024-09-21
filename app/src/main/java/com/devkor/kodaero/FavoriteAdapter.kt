package com.devkor.kodaero

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoriteAdapter(
    private var items: List<CategoryItem>,
    private val onItemClick: (Int, String, String) -> Unit,
    private val onAddButtonClick: () -> Unit // Add 버튼 클릭 콜백 추가
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_ADD_BUTTON = 1
    }

    // 선택된 아이템들의 인덱스를 추적하는 Set
    private val selectedPositions: MutableSet<Int> = mutableSetOf()

    override fun getItemViewType(position: Int): Int {
        return if (position == items.size) VIEW_TYPE_ADD_BUTTON else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
            FavoriteViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_add, parent, false)
            AddButtonViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FavoriteViewHolder) {
            val item = items[position]
            holder.title.text = item.category

            val iconResId = when (item.color.lowercase()) {
                "red" -> R.drawable.icon_star_red
                "blue" -> R.drawable.icon_star_blue
                "green" -> R.drawable.icon_star_green
                "yellow" -> R.drawable.icon_star_yellow
                "orange" -> R.drawable.icon_star_orange
                "pink" -> R.drawable.icon_star_pink
                "purple" -> R.drawable.icon_star_purple
                else -> R.drawable.icon_star_red
            }
            holder.icon.setImageResource(iconResId)

            holder.itemView.setOnClickListener {
                // categoryId, category, color를 onItemClick 콜백에 전달
                onItemClick(item.categoryId, item.category, item.color)
            }

        }
        else if (holder is AddButtonViewHolder) {
            holder.addButton.setOnClickListener {
                onAddButtonClick() // Add 버튼 클릭 시 콜백 호출
            }
        }
    }

    override fun getItemCount(): Int = items.size + 1  // +1 to account for the "Add" button

    // 일반 아이템을 위한 뷰 홀더
    class FavoriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.star_icon)
        val title: TextView = view.findViewById(R.id.title)
    }

    // 추가 버튼을 위한 뷰 홀더
    class AddButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val addButton: View = view.findViewById(R.id.item_category_add)
    }

    // 데이터를 갱신하는 메서드
    fun updateItems(newItems: List<CategoryItem>) {
        items = newItems
        // 초기 상태 설정: bookmarked가 true인 아이템을 선택된 상태로 설정
        selectedPositions.clear()
        newItems.forEachIndexed { index, item ->
            if (item.bookmarked) {
                selectedPositions.add(index)
            }
        }
        Log.e("dddfsdfsdf","$items")
        notifyDataSetChanged()
    }

    // 선택된 카테고리 아이템을 반환하는 메서드
    fun getSelectedCategories(): List<CategoryItem> {
        return items.filterIndexed { index, _ -> selectedPositions.contains(index) }
    }
}
