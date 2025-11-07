package com.example.minandroidapp.ui.tag

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.minandroidapp.databinding.ItemTagRelationBinding
import com.example.minandroidapp.model.TagRelations
import com.google.android.material.chip.Chip

class TagRelationsAdapter(
    private val onEdit: (TagRelations) -> Unit,
) : ListAdapter<TagRelations, TagRelationsAdapter.TagRelationViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagRelationViewHolder {
        val binding = ItemTagRelationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagRelationViewHolder(binding, onEdit)
    }

    override fun onBindViewHolder(holder: TagRelationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TagRelationViewHolder(
        private val binding: ItemTagRelationBinding,
        private val onEdit: (TagRelations) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(relations: TagRelations) {
            binding.tagTitle.text = relations.tag.label
            binding.relatedChips.removeAllViews()
            relations.related.forEach { tag ->
                val chip = Chip(binding.root.context).apply {
                    text = tag.label
                    isCheckable = false
                }
                binding.relatedChips.addView(chip)
            }
            binding.editConnectionsButton.setOnClickListener {
                onEdit(relations)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TagRelations>() {
            override fun areItemsTheSame(oldItem: TagRelations, newItem: TagRelations): Boolean =
                oldItem.tag.id == newItem.tag.id

            override fun areContentsTheSame(oldItem: TagRelations, newItem: TagRelations): Boolean =
                oldItem == newItem
        }
    }
}
