package com.ku.kodaero

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BusTimeAdapter(private val minuteList: List<String>) : RecyclerView.Adapter<BusTimeAdapter.BusTimeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusTimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus_modal_timetable_minute, parent, false)
        return BusTimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusTimeViewHolder, position: Int) {
        val minute = minuteList[position]

        if (position < minuteList.size - 1) {
            holder.minuteTextView.text = "$minute, "
        } else {
            holder.minuteTextView.text = minute
        }
    }

    override fun getItemCount(): Int {
        return minuteList.size
    }

    inner class BusTimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val minuteTextView: TextView = itemView.findViewById(R.id.item_bus_modal_timetable)

        fun bind(minute: String) {
            minuteTextView.text = minute
        }
    }
}
