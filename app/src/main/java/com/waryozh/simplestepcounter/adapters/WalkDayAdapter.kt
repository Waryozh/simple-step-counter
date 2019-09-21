package com.waryozh.simplestepcounter.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.waryozh.simplestepcounter.database.WalkDay
import com.waryozh.simplestepcounter.databinding.WalkdayItemSingleBinding

class WalkDayAdapter : ListAdapter<WalkDay, WalkDayAdapter.WalkDayViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkDayViewHolder {
        return WalkDayViewHolder(WalkdayItemSingleBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: WalkDayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<WalkDay>() {
        override fun areItemsTheSame(oldItem: WalkDay, newItem: WalkDay): Boolean {
            return oldItem.dayId == newItem.dayId
        }

        override fun areContentsTheSame(oldItem: WalkDay, newItem: WalkDay): Boolean {
            return oldItem == newItem
        }
    }

    class WalkDayViewHolder(private var binding: WalkdayItemSingleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WalkDay) {
            binding.walkDay = item
            binding.executePendingBindings()
        }
    }
}
