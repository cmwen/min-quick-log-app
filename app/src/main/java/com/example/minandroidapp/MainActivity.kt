package com.example.minandroidapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.data.db.LogDatabase
import com.example.minandroidapp.databinding.ActivityMainBinding
import com.example.minandroidapp.location.LocationProvider
import com.example.minandroidapp.model.EntryLocation
import com.example.minandroidapp.model.LogEntry
import com.example.minandroidapp.model.LogTag
import com.example.minandroidapp.model.QuickLogEvent
import com.example.minandroidapp.ui.EntryAdapter
import com.example.minandroidapp.ui.QuickLogViewModel
import com.example.minandroidapp.ui.entries.EntriesOverviewActivity
import com.example.minandroidapp.ui.tag.TagManagerActivity
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var locationProvider: LocationProvider
    private val entryAdapter = EntryAdapter(
        onEntryClicked = { entry ->
            viewModel.beginEditing(entry.id)
            binding.contentScroll.smoothScrollTo(0, 0)
        },
        onShareClicked = { entry ->
            shareEntry(entry)
        },
    )
    private var suppressNoteUpdates = false

    private val viewModel: QuickLogViewModel by viewModels {
        val database = LogDatabase.getInstance(applicationContext)
        QuickLogViewModel.Factory(QuickLogRepository(database))
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (fineGranted || coarseGranted) {
                fetchLocationAndApply()
            } else {
                showMessage(getString(R.string.location_unknown))
            }
        }

    private val timeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d • HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        locationProvider = LocationProvider(this)

        setupDrawer()
        setupRecyclerView()
        setupInputs()
        bindViewModel()

        maybeFetchLocationOnStart()
    }

    private fun setupDrawer() {
        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.drawer_open,
            R.string.drawer_close,
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_entries_overview -> openEntriesOverview()
                R.id.nav_tag_manager -> openTagManager()
                R.id.nav_export_text -> exportLog()
                R.id.nav_export_csv -> exportCsv()
                R.id.nav_about -> showAboutDialog()
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupRecyclerView() {
        binding.entryList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = entryAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupInputs() {
        binding.clearTagsButton.setOnClickListener {
            viewModel.clearTags()
        }
        binding.newTagButton.setOnClickListener {
            showCreateTagDialog()
        }
        binding.manageTagsButton.setOnClickListener {
            openTagManager()
        }
        binding.saveButton.setOnClickListener {
            viewModel.saveEntry()
        }
        binding.refreshLocationButton.setOnClickListener {
            ensureLocationPermission()
        }
        binding.noteInput.doOnTextChanged { text, _, _, _ ->
            if (suppressNoteUpdates) return@doOnTextChanged
            viewModel.setNote(text?.toString().orEmpty())
        }
    }

    private fun bindViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                val timestamp = if (state.draft.entryId == null) {
                    timeFormatter.format(state.clockInstant)
                } else {
                    timeFormatter.format(state.draft.createdAt)
                }
                binding.timeText.text = if (state.draft.entryId == null) {
                    getString(R.string.current_time, timestamp)
                } else {
                    getString(R.string.editing_entry_time, timestamp)
                }

                val locationLabel = state.draft.location.label
                binding.locationText.text = locationLabel ?: getString(R.string.location_unknown)

                renderSelectableChips(binding.recentTagsGroup, state.recentTags, state.draft.selectedTags)
                binding.recentTagsLabel.isVisible = state.recentTags.isNotEmpty()

                renderSelectableChips(binding.suggestedTagsGroup, state.suggestedTags, state.draft.selectedTags)
                binding.suggestedTagsLabel.isVisible = state.suggestedTags.isNotEmpty()
                binding.suggestedTagsGroup.isVisible = state.suggestedTags.isNotEmpty()

                renderSelectedChips(binding.selectedTagsGroup, state.draft.selectedTags)
                val hasSelection = state.draft.selectedTags.isNotEmpty()
                binding.selectedTagsLabel.isVisible = hasSelection
                binding.selectedTagsGroup.isVisible = hasSelection
                binding.clearTagsButton.isEnabled = hasSelection

                suppressNoteUpdates = true
                if (binding.noteInput.text?.toString() != state.draft.note) {
                    binding.noteInput.setText(state.draft.note)
                    binding.noteInput.setSelection(binding.noteInput.text?.length ?: 0)
                }
                suppressNoteUpdates = false

                binding.saveButton.isEnabled = !state.isSaving && hasSelection
                binding.saveButton.text = if (state.draft.entryId == null) {
                    getString(R.string.save_entry)
                } else {
                    getString(R.string.update_entry)
                }

                entryAdapter.submitList(state.entries)
                binding.emptyEntriesText.isVisible = state.entries.isEmpty()
            }
        }

        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is QuickLogEvent.EntrySaved -> {
                        val messageRes = if (event.updated) R.string.entry_updated else R.string.entry_saved
                        showMessage(getString(messageRes))
                        binding.contentScroll.smoothScrollTo(0, 0)
                    }
                    is QuickLogEvent.Error -> showMessage(event.message)
                    is QuickLogEvent.CustomTagCreated -> showMessage(
                        getString(R.string.new_tag_created, event.label),
                    )
                }
            }
        }
    }

    private fun renderSelectableChips(
        group: com.google.android.material.chip.ChipGroup,
        tags: List<LogTag>,
        selected: List<LogTag>,
    ) {
        group.removeAllViews()
        val selectedIds = selected.map { it.id }.toSet()
        val inflater = layoutInflater
        tags.forEach { tag ->
            val chip = inflater.inflate(R.layout.view_tag_chip, group, false) as Chip
            chip.text = tag.label
            chip.isCheckable = true
            chip.isChecked = selectedIds.contains(tag.id)
            chip.setOnClickListener {
                viewModel.toggleTag(tag)
            }
            group.addView(chip)
        }
        group.isVisible = tags.isNotEmpty()
    }

    private fun renderSelectedChips(group: com.google.android.material.chip.ChipGroup, tags: List<LogTag>) {
        group.removeAllViews()
        val inflater = layoutInflater
        tags.forEach { tag ->
            val chip = inflater.inflate(R.layout.view_tag_chip, group, false) as Chip
            chip.text = tag.label
            chip.isCloseIconVisible = true
            chip.isCheckable = false
            chip.setOnCloseIconClickListener {
                viewModel.toggleTag(tag)
            }
            chip.setOnClickListener {
                viewModel.toggleTag(tag)
            }
            group.addView(chip)
        }
    }

    private fun maybeFetchLocationOnStart() {
        if (hasLocationPermission()) {
            fetchLocationAndApply()
        }
    }

    private fun ensureLocationPermission() {
        if (hasLocationPermission()) {
            fetchLocationAndApply()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    private fun fetchLocationAndApply() {
        binding.refreshLocationButton.isEnabled = false
        lifecycleScope.launch {
            val location = locationProvider.getCurrentLocation()
            if (location != null) {
                applyLocationToDraft(location)
            } else {
                showMessage(getString(R.string.location_unknown))
            }
            binding.refreshLocationButton.isEnabled = true
        }
    }

    private fun applyLocationToDraft(location: EntryLocation) {
        viewModel.updateLocation(location)
        viewModel.applyCurrentLocationToDraft()
    }

    private fun exportLog() {
        val text = viewModel.exportLogAsText()
        if (text.isBlank()) {
            showMessage(getString(R.string.no_entries_message))
            return
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_HTML_TEXT, viewModel.exportLogAsHtml())
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.export_log)))
    }

    private fun shareEntry(entry: LogEntry) {
        val payload = viewModel.exportEntry(entry)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, payload.plain)
            putExtra(Intent.EXTRA_HTML_TEXT, payload.html)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_entry)))
    }

    private fun exportCsv() {
        val csv = viewModel.exportEntriesCsv()
        if (csv.isBlank()) {
            showMessage(getString(R.string.no_entries_message))
            return
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_TEXT, csv)
            putExtra(Intent.EXTRA_SUBJECT, "quick-log.csv")
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.export_csv)))
    }

    private fun openTagManager() {
        startActivity(Intent(this, TagManagerActivity::class.java))
    }

    private fun openEntriesOverview() {
        startActivity(Intent(this, EntriesOverviewActivity::class.java))
    }

    private fun showAboutDialog() {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = packageInfo.versionName ?: "1.0"
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.about_app)
            .setMessage(
                getString(R.string.app_version, versionName, versionCode) +
                    "\n\n" + getString(R.string.about_message),
            )
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun showCreateTagDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_tag, null)
        val input = dialogView.findViewById<TextInputEditText>(R.id.newTagInput)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.new_tag_dialog_title)
            .setView(dialogView)
            .setPositiveButton(R.string.new_tag_dialog_confirm) { _, _ ->
                val label = input?.text?.toString().orEmpty()
                viewModel.createCustomTag(label)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
        input?.post {
            input.requestFocus()
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
