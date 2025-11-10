package com.example.minandroidapp.ui.tag

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.minandroidapp.MainActivity
import com.example.minandroidapp.R
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.data.db.LogDatabase
import com.example.minandroidapp.databinding.ActivityTagManagerBinding
import com.example.minandroidapp.model.TagRelations
import com.example.minandroidapp.ui.entries.EntriesOverviewActivity
import com.example.minandroidapp.settings.ThemeManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class TagManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTagManagerBinding
    private lateinit var adapter: TagRelationsAdapter
    private val selectedTagIds = linkedSetOf<String>()
    private var deleteMenuItem: MenuItem? = null

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
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityTagManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.tagToolbar)
        onBackPressedDispatcher.addCallback(this) {
            if (!clearSelectionIfNeeded()) {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
        binding.tagToolbar.setNavigationOnClickListener {
            if (!clearSelectionIfNeeded()) {
                finish()
            }
        }
        binding.tagToolbar.inflateMenu(R.menu.menu_tag_manager)
        deleteMenuItem = binding.tagToolbar.menu.findItem(R.id.action_delete_tags)?.apply {
            isVisible = false
        }
        binding.tagToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_export_tags -> {
                    exportTagsLauncher.launch("quick-log-tags.csv")
                    true
                }
                R.id.action_import_tags -> {
                    importTagsLauncher.launch(arrayOf("text/*", "application/*"))
                    true
                }
                R.id.action_delete_tags -> {
                    confirmDeleteSelection()
                    true
                }
                else -> false
            }
        }

        adapter = TagRelationsAdapter(
            onEdit = { relation ->
                showEditDialog(relation)
            },
            onToggleSelection = { relations ->
                toggleSelection(relations)
            },
        )
        adapter.setSelection(emptySet())
        binding.tagRecycler.layoutManager = LinearLayoutManager(this)
        binding.tagRecycler.adapter = adapter
        attachSwipeToDelete()

        lifecycleScope.launch {
            viewModel.tagRelations.collect { relations ->
                val existingIds = relations.map { it.tag.id }.toSet()
                if (selectedTagIds.retainAll(existingIds)) {
                    adapter.setSelection(selectedTagIds)
                    updateSelectionUi()
                }
                adapter.submitList(relations)
            }
        }
        binding.addTagFab.setOnClickListener {
            showCreateTagDialog()
        }
        setupBottomNav()
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

    private fun showCreateTagDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_tag, null)
        val input = dialogView.findViewById<TextInputEditText>(R.id.newTagInput)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.new_tag_dialog_title)
            .setView(dialogView)
            .setPositiveButton(R.string.new_tag_dialog_confirm) { _, _ ->
                val label = input?.text?.toString().orEmpty()
                viewModel.createCustomTag(label) { success ->
                    val message = if (success) {
                        getString(R.string.new_tag_created, label.trim())
                    } else {
                        getString(R.string.new_tag_error)
                    }
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
        input?.post {
            input.requestFocus()
        }
    }

    private fun toggleSelection(relations: TagRelations) {
        val tagId = relations.tag.id
        if (!selectedTagIds.add(tagId)) {
            selectedTagIds.remove(tagId)
        }
        adapter.setSelection(selectedTagIds)
        updateSelectionUi()
    }

    private fun updateSelectionUi() {
        val count = selectedTagIds.size
        deleteMenuItem?.isVisible = count > 0
        binding.tagToolbar.subtitle = if (count > 0) {
            resources.getQuantityString(R.plurals.tag_selection_count, count, count)
        } else {
            null
        }
    }

    private fun clearSelectionIfNeeded(): Boolean {
        return if (selectedTagIds.isNotEmpty()) {
            selectedTagIds.clear()
            adapter.setSelection(selectedTagIds)
            updateSelectionUi()
            true
        } else {
            false
        }
    }

    private fun confirmDeleteSelection() {
        val count = selectedTagIds.size
        if (count == 0) return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_tags)
            .setMessage(getString(R.string.delete_tags_confirm_message, count))
            .setPositiveButton(R.string.delete_action) { _, _ ->
                performDeleteSelection()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun performDeleteSelection() {
        val ids = selectedTagIds.toList()
        deleteTagsByIds(
            ids,
            onSuccess = { clearSelectionIfNeeded() },
        )
    }

    private fun deleteTagsByIds(
        ids: List<String>,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {},
    ) {
        if (ids.isEmpty()) {
            onFailure()
            return
        }
        viewModel.deleteTags(ids) { success ->
            if (success) {
                removeIdsFromSelection(ids)
                Snackbar.make(
                    binding.root,
                    getString(R.string.delete_tags_success, ids.size),
                    Snackbar.LENGTH_LONG,
                ).show()
                onSuccess()
            } else {
                Snackbar.make(binding.root, R.string.delete_tags_failure, Snackbar.LENGTH_LONG).show()
                onFailure()
            }
        }
    }

    private fun removeIdsFromSelection(ids: List<String>) {
        if (selectedTagIds.removeAll(ids.toSet())) {
            adapter.setSelection(selectedTagIds)
            updateSelectionUi()
        }
    }

    private fun attachSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val relations = adapter.currentList.getOrNull(position)
                if (relations == null) {
                    adapter.notifyItemChanged(position)
                    return
                }
                confirmSwipeDelete(relations, position)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.tagRecycler)
    }

    private fun confirmSwipeDelete(relations: TagRelations, adapterPosition: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_tags))
            .setMessage(getString(R.string.delete_tags_confirm_message, 1))
            .setPositiveButton(R.string.delete_action) { _, _ ->
                deleteTagsByIds(
                    listOf(relations.tag.id),
                    onFailure = { adapter.notifyItemChanged(adapterPosition) },
                )
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                adapter.notifyItemChanged(adapterPosition)
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(adapterPosition)
            }
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
            Snackbar.make(binding.root, R.string.export_tags_success, Snackbar.LENGTH_LONG).show()
        }.onFailure {
            Snackbar.make(binding.root, it.localizedMessage ?: getString(R.string.import_tags_failure), Snackbar.LENGTH_LONG).show()
        }
    }

    private suspend fun importTags(uri: Uri) {
        val content = runCatching {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
        if (content.isNullOrEmpty()) {
            Snackbar.make(binding.root, R.string.import_tags_failure, Snackbar.LENGTH_LONG).show()
            return
        }
        runCatching {
            val count = viewModel.importTagsCsv(content)
            Snackbar.make(
                binding.root,
                getString(R.string.import_tags_success, count),
                Snackbar.LENGTH_LONG,
            ).show()
        }.onFailure {
            Snackbar.make(binding.root, R.string.import_tags_failure, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_tags
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_record -> {
                    startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    finish()
                    true
                }
                R.id.nav_entries -> {
                    startActivity(Intent(this, EntriesOverviewActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tags -> true
                else -> false
            }
        }
    }
}
