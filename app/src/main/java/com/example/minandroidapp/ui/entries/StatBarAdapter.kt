package com.example.minandroidapp.ui.entries

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minandroidapp.databinding.ItemStatBarBinding
import com.google.android.material.progressindicator.LinearProgressIndicator

class StatBarAdapter : RecyclerView.Adapter<StatBarAdapter.StatBarViewHolder>() {

    private var items: List<StatCount> = emptyList()
    private var maxValue: Int = 1

    fun submitData(data: List<StatCount>) {
        items = data
        maxValue = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatBarViewHolder {
        val binding = ItemStatBarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatBarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatBarViewHolder, position: Int) {
        holder.bind(items[position], maxValue)
    }

    override fun getItemCount(): Int = items.size

    class StatBarViewHolder(binding: ItemStatBarBinding) : RecyclerView.ViewHolder(binding.root) {
        private val labelView: TextView = binding.statLabel
        private val countView: TextView = binding.statCount
        private val barView: LinearProgressIndicator = binding.statProgress

        fun bind(item: StatCount, max: Int) {
            labelView.text = item.label
            countView.text = item.count.toString()
            barView.max = max
            barView.progress = item.count
        }
    }
}
