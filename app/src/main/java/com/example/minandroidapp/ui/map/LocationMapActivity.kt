package com.example.minandroidapp.ui.map

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.minandroidapp.MainActivity
import com.example.minandroidapp.R
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.data.db.LogDatabase
import com.example.minandroidapp.databinding.ActivityLocationMapBinding
import com.example.minandroidapp.settings.ThemeManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LocationMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationMapBinding
    private val viewModel: LocationMapViewModel by viewModels {
        LocationMapViewModel.Factory(QuickLogRepository(LogDatabase.getInstance(applicationContext)))
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        
        // Configure osmdroid
        Configuration.getInstance().userAgentValue = packageName
        
        binding = ActivityLocationMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mapToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.mapToolbar.setNavigationOnClickListener {
            finish()
        }

        setupMap()
        setupDateFilters()
        bindViewModel()
        setupBottomNav()
    }

    private fun setupMap() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.controller.setZoom(12.0)
        
        // Default to user's current location if available, otherwise show a default location
        val defaultLocation = GeoPoint(37.7749, -122.4194) // San Francisco as default
        binding.mapView.controller.setCenter(defaultLocation)
    }

    private fun setupDateFilters() {
        binding.selectDateRangeButton.setOnClickListener {
            showDateRangePicker()
        }
        
        binding.clearFiltersButton.setOnClickListener {
            viewModel.clearDateFilter()
        }
        
        binding.showTimelineButton.setOnClickListener {
            showTimeline()
        }
    }

    private fun bindViewModel() {
        lifecycleScope.launch {
            viewModel.locationEntries.collect { entries ->
                updateMapMarkers(entries)
                binding.entriesCountText.text = getString(R.string.map_entries_count, entries.size)
                binding.noEntriesText.isVisible = entries.isEmpty()
                binding.showTimelineButton.isEnabled = entries.isNotEmpty()
            }
        }

        lifecycleScope.launch {
            viewModel.dateFilter.collect { filter ->
                binding.dateFilterText.text = when {
                    filter.startDate != null && filter.endDate != null -> {
                        val start = LocalDate.ofInstant(filter.startDate, ZoneId.systemDefault())
                        val end = LocalDate.ofInstant(filter.endDate, ZoneId.systemDefault())
                        getString(R.string.map_date_filter, 
                            start.format(dateFormatter), 
                            end.format(dateFormatter))
                    }
                    else -> getString(R.string.map_all_dates)
                }
                binding.clearFiltersButton.isVisible = filter.startDate != null || filter.endDate != null
            }
        }
    }

    private fun updateMapMarkers(entries: List<LocationEntry>) {
        binding.mapView.overlays.clear()
        
        if (entries.isEmpty()) {
            return
        }
        
        entries.forEach { entry ->
            if (entry.latitude != null && entry.longitude != null) {
                val marker = Marker(binding.mapView)
                marker.position = GeoPoint(entry.latitude, entry.longitude)
                marker.title = entry.locationLabel ?: getString(R.string.location_unknown)
                marker.snippet = formatEntrySnippet(entry)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                binding.mapView.overlays.add(marker)
            }
        }
        
        // Center map on first entry
        entries.firstOrNull()?.let { entry ->
            if (entry.latitude != null && entry.longitude != null) {
                binding.mapView.controller.setCenter(GeoPoint(entry.latitude, entry.longitude))
            }
        }
        
        binding.mapView.invalidate()
    }

    private fun formatEntrySnippet(entry: LocationEntry): String {
        val date = LocalDate.ofInstant(entry.createdAt, ZoneId.systemDefault())
        val formattedDate = date.format(dateFormatter)
        val tags = entry.tags.joinToString(", ")
        return "$formattedDate\n$tags"
    }

    private fun showDateRangePicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(R.string.map_select_date_range)
        
        val currentFilter = viewModel.dateFilter.value
        if (currentFilter.startDate != null && currentFilter.endDate != null) {
            builder.setSelection(
                androidx.core.util.Pair(
                    currentFilter.startDate.toEpochMilli(),
                    currentFilter.endDate.toEpochMilli()
                )
            )
        }
        
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { selection ->
            val startDate = Instant.ofEpochMilli(selection.first)
            val endDate = Instant.ofEpochMilli(selection.second)
            viewModel.setDateFilter(startDate, endDate)
        }
        picker.show(supportFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun showTimeline() {
        val entries = viewModel.locationEntries.value
        if (entries.isEmpty()) {
            return
        }
        
        val timelineText = buildTimelineText(entries)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.map_timeline_title)
            .setMessage(timelineText)
            .setPositiveButton(android.R.string.ok, null)
            .setNeutralButton(R.string.share_entry) { _, _ ->
                shareTimeline(timelineText)
            }
            .show()
    }

    private fun buildTimelineText(entries: List<LocationEntry>): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm")
        return entries.sortedBy { it.createdAt }.joinToString("\n\n") { entry ->
            val dateTime = entry.createdAt.atZone(ZoneId.systemDefault())
            val location = entry.locationLabel ?: String.format("%.4f, %.4f", 
                entry.latitude ?: 0.0, entry.longitude ?: 0.0)
            val tags = entry.tags.joinToString(", ")
            "${dateTime.format(formatter)}\n📍 $location\n🏷️ $tags"
        }
    }

    private fun shareTimeline(timelineText: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, timelineText)
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.map_timeline_title))
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_entry)))
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_locations
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_record -> {
                    startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    finish()
                    true
                }
                R.id.nav_entries -> {
                    startActivity(Intent(this, com.example.minandroidapp.ui.entries.EntriesOverviewActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tags -> {
                    startActivity(Intent(this, com.example.minandroidapp.ui.tag.TagManagerActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_locations -> true
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_location_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export_locations -> {
                exportLocationDataAsJson()
                true
            }
            R.id.action_import_locations -> {
                // TODO: Implement import functionality
                Snackbar.make(binding.root, "Import locations feature coming soon", Snackbar.LENGTH_LONG).show()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, com.example.minandroidapp.settings.SettingsActivity::class.java))
                true
            }
            R.id.action_export_json -> {
                exportLocationDataAsJson()
                true
            }
            R.id.action_export_csv -> {
                exportLocationDataAsCsv()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportLocationDataAsJson() {
        val json = viewModel.exportEntriesAsJson()
        if (json.isBlank()) {
            Snackbar.make(binding.root, R.string.no_entries_message, Snackbar.LENGTH_LONG).show()
            return
        }
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_TEXT, json)
            putExtra(Intent.EXTRA_SUBJECT, "location-data.json")
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.export_location_data)))
    }

    private fun exportLocationDataAsCsv() {
        val csv = viewModel.exportEntriesAsCsv()
        if (csv.isBlank()) {
            Snackbar.make(binding.root, R.string.no_entries_message, Snackbar.LENGTH_LONG).show()
            return
        }
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_TEXT, csv)
            putExtra(Intent.EXTRA_SUBJECT, "location-data.csv")
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.export_location_data)))
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }
}
