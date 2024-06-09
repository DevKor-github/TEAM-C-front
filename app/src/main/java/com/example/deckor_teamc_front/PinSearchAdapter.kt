package com.example.deckor_teamc_front

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deckor_teamc_front.databinding.PinSearchListItemBinding

class PinSearchAdapter : RecyclerView.Adapter<PinSearchAdapter.PinSearchViewHolder>() {

    private val facilities = mutableListOf<FacilityItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinSearchViewHolder {
        val binding = PinSearchListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PinSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PinSearchViewHolder, position: Int) {
        holder.bind(facilities[position])
    }

    override fun getItemCount(): Int = facilities.size

    fun submitList(newFacilities: List<FacilityItem>) {
        facilities.clear()
        facilities.addAll(newFacilities)
        notifyDataSetChanged()
    }

    inner class PinSearchViewHolder(private val binding: PinSearchListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(facility: FacilityItem) {
            binding.facilityName.text = facility.name
            binding.facilityOperatingStatus.text = if (facility.availability) "운영 중" else "마감"
            // Additional binding can be done here if needed
        }
    }
}
