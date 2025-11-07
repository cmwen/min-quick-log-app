package com.example.minandroidapp.ui.entries

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.minandroidapp.R
import com.example.minandroidapp.model.LogEntry
import com.example.minandroidapp.model.LogTag
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class EntriesListAdapter(
    private val onEntryClicked: (LogEntry) -> Unit,
    private val onShareClicked: (LogEntry) -> Unit,
) : ListAdapter<EntryListItem, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is EntryListItem.Header -> VIEW_HEADER
        is EntryListItem.Entry -> VIEW_ENTRY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_HEADER) {
            val view = inflater.inflate(R.layout.item_section_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_entry, parent, false)
            EntryViewHolder(view, onEntryClicked, onShareClicked)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is EntryListItem.Header -> (holder as HeaderViewHolder).bind(item.title)
            is EntryListItem.Entry -> (holder as EntryViewHolder).bind(item.logEntry)
        }
    }

    private class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.sectionTitle)
        fun bind(text: String) {
            title.text = text
        }
    }

    private class EntryViewHolder(
        itemView: View,
        private val onEntryClicked: (LogEntry) -> Unit,
        private val onShareClicked: (LogEntry) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val timestampView: TextView = itemView.findViewById(R.id.entryTimestamp)
        private val locationView: TextView = itemView.findViewById(R.id.entryLocation)
        private val tagsGroup: ChipGroup = itemView.findViewById(R.id.entryTagsGroup)
        private val noteView: TextView = itemView.findViewById(R.id.entryNote)
        private val shareButton: View = itemView.findViewById(R.id.shareButton)

        private val formatter = DateTimeFormatter.ofPattern("EEE, MMM d • HH:mm", Locale.getDefault())
            .withZone(ZoneId.systemDefault())

        fun bind(entry: LogEntry) {
            timestampView.text = formatter.format(entry.createdAt)
            locationView.text = entry.location.label ?: itemView.context.getString(R.string.location_unknown)
            tagsGroup.removeAllViews()
            entry.tags.forEach { tag ->
                tagsGroup.addView(buildTagChip(tag))
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

        private fun buildTagChip(tag: LogTag): Chip {
            return Chip(itemView.context).apply {
                text = tag.label
                isCheckable = false
            }
        }
    }

    companion object {
        private const val VIEW_HEADER = 0
        private const val VIEW_ENTRY = 1

        private val DIFF = object : DiffUtil.ItemCallback<EntryListItem>() {
            override fun areItemsTheSame(oldItem: EntryListItem, newItem: EntryListItem): Boolean {
                return when {
                    oldItem is EntryListItem.Header && newItem is EntryListItem.Header -> oldItem.title == newItem.title
                    oldItem is EntryListItem.Entry && newItem is EntryListItem.Entry -> oldItem.logEntry.id == newItem.logEntry.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: EntryListItem, newItem: EntryListItem): Boolean = oldItem == newItem
        }
    }
}
