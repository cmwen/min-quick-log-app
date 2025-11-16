package com.example.minandroidapp.ui.map

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.minandroidapp.MainActivity
import com.example.minandroidapp.R
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.data.db.LogDatabase
import com.example.minandroidapp.databinding.ActivityLocationMapBinding
import com.example.minandroidapp.model.EntryLocation
import com.example.minandroidapp.settings.ThemeManager
import com.example.minandroidapp.ui.common.BaseNavigationActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LocationMapActivity : BaseNavigationActivity() {

    override val currentNavItem = R.id.nav_locations

    private lateinit var binding: ActivityLocationMapBinding
    private val viewModel: LocationMapViewModel by viewModels {
        LocationMapViewModel.Factory(QuickLogRepository(LogDatabase.getInstance(applicationContext)))
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private var dropPinMode = false
    private var tempMarker: Marker? = null
    private val importLocationsLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            lifecycleScope.launch { importLocations(uri) }
        }
    }

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
        setupAddLocationFab()
        bindViewModel()
        setupBottomNav(binding.bottomNav)
    }

    private fun setupMap() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.controller.setZoom(12.0)
        
        // Default to user's current location if available, otherwise show a default location
        val defaultLocation = GeoPoint(37.7749, -122.4194) // San Francisco as default
        binding.mapView.controller.setCenter(defaultLocation)

        // Add map events overlay for drop pin functionality
        setupMapTapListener()
    }

    private fun setupMapTapListener() {
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (dropPinMode && p != null) {
                    dropPinAtLocation(p)
                    return true
                }
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        })
        binding.mapView.overlays.add(0, mapEventsOverlay)
    }

    private fun setupAddLocationFab() {
        binding.addLocationFab.setOnClickListener {
            if (!dropPinMode) {
                enterDropPinMode()
            } else {
                exitDropPinMode()
            }
        }
    }

    private fun enterDropPinMode() {
        dropPinMode = true
        binding.addLocationFab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        Snackbar.make(binding.root, R.string.drop_pin_mode, Snackbar.LENGTH_LONG).show()
    }

    private fun exitDropPinMode() {
        dropPinMode = false
        binding.addLocationFab.setImageResource(R.drawable.ic_location)
        removeTempMarker()
    }

    private fun dropPinAtLocation(geoPoint: GeoPoint) {
        removeTempMarker()
        
        tempMarker = Marker(binding.mapView).apply {
            position = geoPoint
            title = getString(R.string.pin_dropped)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            isDraggable = true
            setOnMarkerClickListener { marker, _ ->
                showSaveLocationDialog(marker.position)
                true
            }
        }
        
        binding.mapView.overlays.add(tempMarker)
        binding.mapView.invalidate()
        
        Snackbar.make(binding.root, R.string.pin_dropped, Snackbar.LENGTH_LONG)
            .setAction(R.string.save_location) {
                showSaveLocationDialog(geoPoint)
            }
            .show()
    }

    private fun removeTempMarker() {
        tempMarker?.let {
            binding.mapView.overlays.remove(it)
            binding.mapView.invalidate()
            tempMarker = null
        }
    }

    private fun showSaveLocationDialog(geoPoint: GeoPoint) {
        val locationLabel = String.format("%.4f, %.4f", geoPoint.latitude, geoPoint.longitude)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.save_location)
            .setMessage("Save location at:\n$locationLabel")
            .setPositiveButton(R.string.save_entry) { _, _ ->
                saveLocationAndOpenLog(geoPoint)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                removeTempMarker()
            }
            .setOnCancelListener {
                removeTempMarker()
            }
            .show()
    }

    private fun saveLocationAndOpenLog(geoPoint: GeoPoint) {
        val location = EntryLocation(
            latitude = geoPoint.latitude,
            longitude = geoPoint.longitude,
            label = String.format("%.4f, %.4f", geoPoint.latitude, geoPoint.longitude)
        )
        
        // Navigate to MainActivity with the location
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("location_latitude", location.latitude)
            putExtra("location_longitude", location.longitude)
            putExtra("location_label", location.label)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        exitDropPinMode()
        startActivity(intent)
        Snackbar.make(binding.root, R.string.location_saved, Snackbar.LENGTH_SHORT).show()
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
        // Clear all markers but preserve the map events overlay (first overlay)
        val mapEventsOverlay = binding.mapView.overlays.firstOrNull()
        binding.mapView.overlays.clear()
        if (mapEventsOverlay != null) {
            binding.mapView.overlays.add(mapEventsOverlay)
        }
        
        // Add temp marker back if it exists
        tempMarker?.let {
            binding.mapView.overlays.add(it)
        }
        
        if (entries.isEmpty()) {
            binding.mapView.invalidate()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_location_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export_location_json -> {
                val json = viewModel.exportEntriesAsJson()
                if (json.isBlank()) {
                    Snackbar.make(binding.root, R.string.no_location_entries, Snackbar.LENGTH_LONG).show()
                } else {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_TEXT, json)
                        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_location_data))
                    }
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.export_location_data)))
                }
                true
            }
            R.id.action_export_location_csv -> {
                val csv = viewModel.exportEntriesAsCsv()
                if (csv.isBlank()) {
                    Snackbar.make(binding.root, R.string.no_location_entries, Snackbar.LENGTH_LONG).show()
                } else {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_TEXT, csv)
                        putExtra(Intent.EXTRA_SUBJECT, "quick-log-locations.csv")
                    }
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.export_location_data)))
                }
                true
            }
            R.id.action_import_locations -> {
                importLocationsLauncher.launch(arrayOf("text/*", "application/*"))
                true
            }
            R.id.action_copy_location_prompt -> {
                copyLocationPrompt()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, com.example.minandroidapp.settings.SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private suspend fun importLocations(uri: Uri) {
        val content = runCatching {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
        if (content.isNullOrBlank()) {
            Snackbar.make(binding.root, R.string.import_locations_failure, Snackbar.LENGTH_LONG).show()
            return
        }
        runCatching {
            val count = viewModel.importLocationsCsv(content)
            Snackbar.make(binding.root, getString(R.string.import_locations_success, count), Snackbar.LENGTH_LONG).show()
        }.onFailure {
            Snackbar.make(binding.root, R.string.import_locations_failure, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun copyLocationPrompt() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(R.string.copy_llm_prompt), getString(R.string.location_llm_prompt))
        clipboard.setPrimaryClip(clip)
        Snackbar.make(binding.root, R.string.copy_llm_prompt_done, Snackbar.LENGTH_SHORT).show()
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
