package com.example.minandroidapp.ui.tag

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.data.db.LogDatabase
import com.example.minandroidapp.databinding.ActivityTagManagerBinding
import com.example.minandroidapp.model.TagRelations
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class TagManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTagManagerBinding
    private lateinit var adapter: TagRelationsAdapter

    private val viewModel: TagManagerViewModel by viewModels {
        TagManagerViewModel.Factory(QuickLogRepository(LogDatabase.getInstance(applicationContext)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.tagToolbar)
        binding.tagToolbar.setNavigationOnClickListener { finish() }

        adapter = TagRelationsAdapter(
            onEdit = { relation ->
                showEditDialog(relation)
            },
        )
        binding.tagRecycler.layoutManager = LinearLayoutManager(this)
        binding.tagRecycler.adapter = adapter

        lifecycleScope.launch {
            viewModel.tagRelations.collect { relations ->
                adapter.submitList(relations)
            }
        }
    }

    private fun showEditDialog(relations: TagRelations) {
        val allTags = adapter.currentList.filter { it.tag.id != relations.tag.id }
        val labels = allTags.map { it.tag.label }.toTypedArray()
        val selectedIds = relations.related.map { it.id }.toMutableSet()
        val checkedItems = allTags.map { selectedIds.contains(it.tag.id) }.toBooleanArray()

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(com.example.minandroidapp.R.string.tag_link_dialog_title, relations.tag.label))
            .setMultiChoiceItems(labels, checkedItems) { _, which, isChecked ->
                val tagId = allTags[which].tag.id
                if (isChecked) {
                    selectedIds.add(tagId)
                } else {
                    selectedIds.remove(tagId)
                }
            }
            .setPositiveButton(com.example.minandroidapp.R.string.new_tag_dialog_confirm) { _, _ ->
                viewModel.updateConnections(relations.tag.id, selectedIds)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
