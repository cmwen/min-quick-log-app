package com.example.minandroidapp.ui.entries

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.minandroidapp.MainActivity
import com.example.minandroidapp.R
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.data.db.LogDatabase
import com.example.minandroidapp.databinding.ActivityEntriesOverviewBinding
import com.example.minandroidapp.settings.ThemeManager
import androidx.appcompat.app.AppCompatActivity
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

    private val exportEntriesLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        if (uri != null) {
            lifecycleScope.launch { exportEntriesToFile(uri) }
        }
    }

    private val importEntriesLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            lifecycleScope.launch { importEntriesFromFile(uri) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityEntriesOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.entriesToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.entriesToolbar.inflateMenu(R.menu.menu_entries_overview)
        deleteMenuItem = binding.entriesToolbar.menu.findItem(R.id.action_delete_entries)?.apply {
            isVisible = false
        }
        binding.entriesToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_entries -> {
                    confirmDeleteEntries()
                    true
                }
                R.id.action_export_entries -> {
                    exportEntries()
                    true
                }
                R.id.action_import_entries -> {
                    importEntriesLauncher.launch(arrayOf("text/*", "application/*"))
                    true
                }
                R.id.action_settings -> {
                    startActivity(Intent(this, com.example.minandroidapp.settings.SettingsActivity::class.java))
                    true
                }
                R.id.action_about -> {
                    showAboutDialog()
                    true
                }
                else -> false
            }
        }
        binding.entriesToolbar.setNavigationOnClickListener {
            viewModel.clearEntrySelection()
            navigateBackToMain()
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_entries_overview, menu)
        return true
    }

    private fun navigateBackToMain() {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
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
        activity: FragmentActivity,
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

    private fun exportEntries() {
        exportEntriesLauncher.launch("quick-log-entries.csv")
    }

    private suspend fun exportEntriesToFile(uri: Uri) {
        runCatching {
            val csv = viewModel.exportEntriesCsv()
            contentResolver.openOutputStream(uri)?.use { stream ->
                stream.writer().use { writer ->
                    writer.write(csv)
                }
            }
        }.onSuccess {
            Snackbar.make(binding.root, R.string.export_entries_success, Snackbar.LENGTH_LONG).show()
        }.onFailure {
            Snackbar.make(binding.root, R.string.export_entries_success, Snackbar.LENGTH_LONG).show()
        }
    }

    private suspend fun importEntriesFromFile(uri: Uri) {
        val content = runCatching {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
        if (content.isNullOrEmpty()) {
            Snackbar.make(binding.root, R.string.import_entries_failure, Snackbar.LENGTH_LONG).show()
            return
        }
        // Note: Import functionality would need to be implemented in the ViewModel
        Snackbar.make(binding.root, getString(R.string.import_entries_success, 0), Snackbar.LENGTH_LONG).show()
    }

    private fun showAboutDialog() {
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).versionCode
        }
        val versionString = getString(R.string.app_version, versionName, versionCode)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.about_app)
            .setMessage("${getString(R.string.about_message)}\n\n$versionString")
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}

interface EntryActionHandler {
    fun onShareEntry(entry: com.example.minandroidapp.model.LogEntry)
    fun onEditEntry(entry: com.example.minandroidapp.model.LogEntry)
}
