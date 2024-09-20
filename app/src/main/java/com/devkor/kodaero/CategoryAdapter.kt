package com.devkor.kodaero

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private var items: List<CategoryItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_ADD_BUTTON = 1
    }

    // 외부에서 버튼 클릭 리스너를 설정할 수 있도록 변수 추가
    var onAddButtonClick: (() -> Unit)? = null

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

            // 색상에 따른 드로어블 리소스를 설정
            val iconResId = when (item.color.lowercase()) {
                "red" -> R.drawable.icon_star_red
                "blue" -> R.drawable.icon_star_blue
                "green" -> R.drawable.icon_star_green
                "yellow" -> R.drawable.icon_star_yellow
                "orange" -> R.drawable.icon_star_orange
                "pink" -> R.drawable.icon_star_pink
                "purple" -> R.drawable.icon_star_purple
                else -> R.drawable.icon_star_red  // 기본 드로어블 설정
            }
            holder.icon.setImageResource(iconResId)
        } else if (holder is AddButtonViewHolder) {
            holder.addButton.setOnClickListener {
                onAddButtonClick?.invoke()
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
        notifyDataSetChanged()
    }
}
