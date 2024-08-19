package com.devkor.kodaero

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devkor.kodaero.databinding.PinSearchListItemBinding

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
            binding.facilityOperatingTime.text = facility.operatingTime
            binding.facilityOperatingStatus.text = if (facility.operating) "운영 중" else "마감"
        }
    }
}
