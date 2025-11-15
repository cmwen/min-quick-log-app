package com.example.minandroidapp.ui.entries

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.minandroidapp.MainActivity
import com.example.minandroidapp.R
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.data.db.LogDatabase
import com.example.minandroidapp.databinding.ActivityEntriesOverviewBinding
import com.example.minandroidapp.settings.ThemeManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class EntriesOverviewActivity : AppCompatActivity(), EntryActionHandler {

    private lateinit var binding: ActivityEntriesOverviewBinding
    private var deleteMenuItem: MenuItem? = null

    val viewModelFactory: EntriesOverviewViewModel.Factory by lazy {
        EntriesOverviewViewModel.Factory(QuickLogRepository(LogDatabase.getInstance(applicationContext)))
    }

    private val viewModel: EntriesOverviewViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityEntriesOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.entriesToolbar)
        binding.entriesToolbar.inflateMenu(R.menu.menu_entries_overview)
        deleteMenuItem = binding.entriesToolbar.menu.findItem(R.id.action_delete_entries)?.apply {
            isVisible = false
        }
        binding.entriesToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_location_map -> {
                    startActivity(Intent(this, com.example.minandroidapp.ui.map.LocationMapActivity::class.java))
                    true
                }
                R.id.action_delete_entries -> {
                    confirmDeleteEntries()
                    true
                }
                else -> false
            }
        }
        binding.entriesToolbar.setNavigationOnClickListener {
            viewModel.clearEntrySelection()
            finish()
        }

        val adapter = EntriesPagerAdapter(this)
        binding.entriesPager.adapter = adapter

        TabLayoutMediator(binding.entriesTabs, binding.entriesPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(com.example.minandroidapp.R.string.entries_tab_date)
                1 -> getString(com.example.minandroidapp.R.string.entries_tab_location)
                2 -> getString(com.example.minandroidapp.R.string.entries_tab_tags)
                else -> getString(com.example.minandroidapp.R.string.entries_tab_stats)
            }
        }.attach()

        binding.entriesPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position >= 2) {
                    viewModel.clearEntrySelection()
                }
            }
        })

        lifecycleScope.launch {
            viewModel.selectedEntryIds.collect { ids ->
                updateEntrySelectionUi(ids.size)
            }
        }

        setupBottomNav()
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_entries
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_record -> {
                    startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    finish()
                    true
                }
                R.id.nav_entries -> true
                R.id.nav_tags -> {
                    startActivity(Intent(this, com.example.minandroidapp.ui.tag.TagManagerActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun confirmDeleteEntries() {
        val count = viewModel.selectedEntryIds.value.size
        if (count == 0) return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_entries)
            .setMessage(getString(R.string.delete_entries_confirm_message, count))
            .setPositiveButton(R.string.delete_action) { _, _ ->
                viewModel.deleteSelectedEntries { success ->
                    showDeleteResult(success, count)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showDeleteResult(success: Boolean, count: Int) {
        val messageRes = if (success) {
            R.string.delete_entries_success
        } else {
            R.string.delete_entries_failure
        }
        val text = if (success) getString(messageRes, count) else getString(messageRes)
        Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG).show()
    }

    private fun updateEntrySelectionUi(count: Int) {
        deleteMenuItem?.isVisible = count > 0
        binding.entriesToolbar.subtitle = if (count > 0) {
            resources.getQuantityString(R.plurals.entry_selection_count, count, count)
        } else {
            null
        }
    }

    private class EntriesPagerAdapter(
        activity: AppCompatActivity,
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int) = when (position) {
            0 -> SimpleEntriesFragment.newInstance(EntriesTab.DATE)
            1 -> SimpleEntriesFragment.newInstance(EntriesTab.LOCATION)
            2 -> TagsFragment()
            else -> StatsFragment()
        }
    }

    override fun onShareEntry(entry: com.example.minandroidapp.model.LogEntry) {
        val payload = viewModel.buildSharePayload(entry)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, payload.plain)
            putExtra(Intent.EXTRA_HTML_TEXT, payload.html)
        }
        startActivity(Intent.createChooser(shareIntent, getString(com.example.minandroidapp.R.string.share_entry)))
    }

    override fun onEditEntry(entry: com.example.minandroidapp.model.LogEntry) {
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_EDIT_ENTRY_ID, entry.id)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}

interface EntryActionHandler {
    fun onShareEntry(entry: com.example.minandroidapp.model.LogEntry)
    fun onEditEntry(entry: com.example.minandroidapp.model.LogEntry)
}
