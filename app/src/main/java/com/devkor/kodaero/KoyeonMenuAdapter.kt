package com.devkor.kodaero

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class KoyeonMenuAdapter(private val menuList: List<String>) : RecyclerView.Adapter<KoyeonMenuAdapter.KoyeonMenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KoyeonMenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return KoyeonMenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: KoyeonMenuViewHolder, position: Int) {
        holder.bind(menuList[position])
    }

    override fun getItemCount(): Int = menuList.size

    class KoyeonMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val menuTextView: TextView = itemView.findViewById(R.id.title)

        fun bind(menu: String) {
            menuTextView.text = menu
        }
    }
}
