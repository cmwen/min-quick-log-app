package com.example.minandroidapp.ui.entries

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.minandroidapp.databinding.ItemTagGroupBinding

class TagGroupAdapter(
    private val onTagSelected: (String) -> Unit,
) : ListAdapter<TagGroupItem, TagGroupAdapter.TagGroupViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagGroupViewHolder {
        val binding = ItemTagGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagGroupViewHolder(binding, onTagSelected)
    }

    override fun onBindViewHolder(holder: TagGroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TagGroupViewHolder(
        private val binding: ItemTagGroupBinding,
        private val onTagSelected: (String) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TagGroupItem) {
            binding.tagGroupTitle.text = item.tagLabel
            binding.tagGroupCount.text = "${item.entries.size} entries"
            binding.root.setOnClickListener { onTagSelected(item.tagId) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TagGroupItem>() {
            override fun areItemsTheSame(oldItem: TagGroupItem, newItem: TagGroupItem): Boolean =
                oldItem.tagId == newItem.tagId

            override fun areContentsTheSame(oldItem: TagGroupItem, newItem: TagGroupItem): Boolean =
                oldItem.entries.size == newItem.entries.size
        }
    }
}
