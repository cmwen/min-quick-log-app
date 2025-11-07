package com.example.minandroidapp.ui.tag

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.data.db.LogDatabase
import com.example.minandroidapp.databinding.ActivityTagManagerBinding
import com.example.minandroidapp.model.TagRelations
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class TagManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTagManagerBinding
    private lateinit var adapter: TagRelationsAdapter

    private val exportTagsLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        if (uri != null) {
            lifecycleScope.launch { exportTags(uri) }
        }
    }

    private val importTagsLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            lifecycleScope.launch { importTags(uri) }
        }
    }

    private val viewModel: TagManagerViewModel by viewModels {
        TagManagerViewModel.Factory(QuickLogRepository(LogDatabase.getInstance(applicationContext)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.tagToolbar)
        binding.tagToolbar.setNavigationOnClickListener { finish() }
        binding.tagToolbar.inflateMenu(com.example.minandroidapp.R.menu.menu_tag_manager)
        binding.tagToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                com.example.minandroidapp.R.id.action_export_tags -> {
                    exportTagsLauncher.launch("quick-log-tags.csv")
                    true
                }
                com.example.minandroidapp.R.id.action_import_tags -> {
                    importTagsLauncher.launch(arrayOf("text/*", "application/*"))
                    true
                }
                else -> false
            }
        }

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

    private suspend fun exportTags(uri: Uri) {
        runCatching {
            val csv = viewModel.exportTagsCsv()
            contentResolver.openOutputStream(uri)?.use { stream ->
                stream.writer().use { writer ->
                    writer.write(csv)
                }
            }
        }.onSuccess {
            Snackbar.make(binding.root, com.example.minandroidapp.R.string.export_tags_success, Snackbar.LENGTH_LONG).show()
        }.onFailure {
            Snackbar.make(binding.root, it.localizedMessage ?: getString(com.example.minandroidapp.R.string.import_tags_failure), Snackbar.LENGTH_LONG).show()
        }
    }

    private suspend fun importTags(uri: Uri) {
        val content = runCatching {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
        if (content.isNullOrEmpty()) {
            Snackbar.make(binding.root, com.example.minandroidapp.R.string.import_tags_failure, Snackbar.LENGTH_LONG).show()
            return
        }
        runCatching {
            val count = viewModel.importTagsCsv(content)
            Snackbar.make(
                binding.root,
                getString(com.example.minandroidapp.R.string.import_tags_success, count),
                Snackbar.LENGTH_LONG,
            ).show()
        }.onFailure {
            Snackbar.make(binding.root, com.example.minandroidapp.R.string.import_tags_failure, Snackbar.LENGTH_LONG).show()
        }
    }
}
