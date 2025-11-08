package com.example.minandroidapp.ui.entries

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.minandroidapp.MainActivity
import com.example.minandroidapp.R
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.data.db.LogDatabase
import com.example.minandroidapp.databinding.ActivityEntriesOverviewBinding
import com.example.minandroidapp.settings.ThemeManager
import com.google.android.material.tabs.TabLayoutMediator

class EntriesOverviewActivity : AppCompatActivity(), EntryActionHandler {

    private lateinit var binding: ActivityEntriesOverviewBinding

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
        binding.entriesToolbar.setNavigationOnClickListener { finish() }

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

        setupBottomNav()
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_entries
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_record -> {
                    startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_entries -> true
                R.id.nav_tags -> {
                    startActivity(Intent(this, com.example.minandroidapp.ui.tag.TagManagerActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
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
