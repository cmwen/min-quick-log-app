package com.example.minandroidapp.ui.common

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.minandroidapp.MainActivity
import com.example.minandroidapp.R
import com.example.minandroidapp.ui.entries.EntriesOverviewActivity
import com.example.minandroidapp.ui.map.LocationMapActivity
import com.example.minandroidapp.ui.tag.TagManagerActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Base activity for screens with bottom navigation.
 * 
 * Provides common navigation setup and handling to eliminate code duplication.
 * Activities extending this class only need to specify which nav item is current.
 */
abstract class BaseNavigationActivity : AppCompatActivity() {

    /**
     * The navigation item ID that should be selected for this activity.
     * Must be one of: R.id.nav_record, R.id.nav_entries, R.id.nav_tags, R.id.nav_locations
     */
    protected abstract val currentNavItem: Int

    /**
     * Setup bottom navigation with standard behavior.
     * Call this in onCreate() after setting up the binding.
     * 
     * @param bottomNav The BottomNavigationView instance from the activity's layout
     */
    protected fun setupBottomNav(bottomNav: BottomNavigationView) {
        bottomNav.selectedItemId = currentNavItem
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_record -> {
                    navigateToRecord()
                    true
                }
                R.id.nav_entries -> {
                    navigateToEntries()
                    true
                }
                R.id.nav_tags -> {
                    navigateToTags()
                    true
                }
                R.id.nav_locations -> {
                    navigateToLocations()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Navigate to the record screen (MainActivity).
     * Uses FLAG_ACTIVITY_CLEAR_TOP to prevent stack buildup.
     */
    private fun navigateToRecord() {
        if (currentNavItem != R.id.nav_record) {
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }
    }

    /**
     * Navigate to the entries overview screen.
     */
    private fun navigateToEntries() {
        if (currentNavItem != R.id.nav_entries) {
            startActivity(Intent(this, EntriesOverviewActivity::class.java))
            finish()
        }
    }

    /**
     * Navigate to the tag manager screen.
     */
    private fun navigateToTags() {
        if (currentNavItem != R.id.nav_tags) {
            startActivity(Intent(this, TagManagerActivity::class.java))
            finish()
        }
    }

    /**
     * Navigate to the location map screen.
     */
    private fun navigateToLocations() {
        if (currentNavItem != R.id.nav_locations) {
            startActivity(Intent(this, LocationMapActivity::class.java))
            finish()
        }
    }
}
