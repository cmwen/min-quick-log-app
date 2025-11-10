package com.example.minandroidapp.ui.tag

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.minandroidapp.R
import com.example.minandroidapp.databinding.ItemTagRelationBinding
import com.example.minandroidapp.model.TagRelations
import com.google.android.material.chip.Chip

class TagRelationsAdapter(
    private val onEdit: (TagRelations) -> Unit,
    private val onToggleSelection: (TagRelations) -> Unit,
) : ListAdapter<TagRelations, TagRelationsAdapter.TagRelationViewHolder>(DIFF) {

    private var selectedIds: Set<String> = emptySet()
    private var selectionMode: Boolean = false

    fun setSelection(ids: Set<String>) {
        selectedIds = ids.toSet()
        selectionMode = selectedIds.isNotEmpty()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagRelationViewHolder {
        val binding = ItemTagRelationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagRelationViewHolder(binding, onEdit)
    }

    override fun onBindViewHolder(holder: TagRelationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun inSelectionMode(): Boolean = selectedIds.isNotEmpty()

    inner class TagRelationViewHolder(
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
            val strokeWidth = binding.root.resources.getDimensionPixelSize(
                if (selectedIds.contains(relations.tag.id)) {
                    R.dimen.tag_card_stroke_selected
                } else {
                    R.dimen.tag_card_stroke_default
                },
            )
            binding.root.strokeWidth = strokeWidth

            val isSelected = selectedIds.contains(relations.tag.id)
            binding.tagSelectionCheck.setOnCheckedChangeListener(null)
            binding.tagSelectionCheck.isVisible = selectionMode
            binding.tagSelectionCheck.isChecked = isSelected
            binding.tagSelectionCheck.setOnCheckedChangeListener { _, _ ->
                onToggleSelection(relations)
            }

            binding.root.setOnLongClickListener {
                onToggleSelection(relations)
                true
            }
            binding.root.setOnClickListener {
                if (inSelectionMode()) {
                    onToggleSelection(relations)
                }
            }

            binding.editConnectionsButton.isEnabled = !selectionMode
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
