package com.example.minandroidapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.minandroidapp.R
import com.example.minandroidapp.model.LogEntry
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.button.MaterialButton
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class EntryAdapter(
    private val onEntryClicked: (LogEntry) -> Unit,
    private val onShareClicked: (LogEntry) -> Unit,
) : ListAdapter<LogEntry, EntryAdapter.EntryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_entry, parent, false)
        return EntryViewHolder(view, onEntryClicked, onShareClicked)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EntryViewHolder(
        itemView: View,
        private val onEntryClicked: (LogEntry) -> Unit,
        private val onShareClicked: (LogEntry) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val timestampView: TextView = itemView.findViewById(R.id.entryTimestamp)
        private val locationView: TextView = itemView.findViewById(R.id.entryLocation)
        private val tagsGroup: ChipGroup = itemView.findViewById(R.id.entryTagsGroup)
        private val noteView: TextView = itemView.findViewById(R.id.entryNote)
        private val shareButton: MaterialButton = itemView.findViewById(R.id.shareButton)

        private val formatter = DateTimeFormatter.ofPattern("EEE, MMM d • HH:mm", Locale.getDefault())
            .withZone(ZoneId.systemDefault())

        fun bind(entry: LogEntry) {
            timestampView.text = formatter.format(entry.createdAt)
            locationView.text = entry.location.label ?: itemView.context.getString(R.string.location_unknown)

            tagsGroup.removeAllViews()
            entry.tags.forEach { tag ->
                val chip = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.view_tag_chip, tagsGroup, false) as Chip
                chip.text = tag.label
                chip.isChecked = false
                chip.isCheckable = false
                tagsGroup.addView(chip)
            }

            if (entry.note.isNullOrBlank()) {
                noteView.visibility = View.GONE
            } else {
                noteView.visibility = View.VISIBLE
                noteView.text = entry.note
            }

            itemView.setOnClickListener { onEntryClicked(entry) }
            shareButton.setOnClickListener { onShareClicked(entry) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LogEntry>() {
            override fun areItemsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean {
                return oldItem == newItem
            }
        }
    }
}
