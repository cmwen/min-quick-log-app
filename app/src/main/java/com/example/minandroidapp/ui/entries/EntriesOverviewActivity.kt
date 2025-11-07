package com.example.minandroidapp.ui.entries

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.minandroidapp.data.QuickLogRepository
import com.example.minandroidapp.data.db.LogDatabase
import com.example.minandroidapp.databinding.ActivityEntriesOverviewBinding
import com.google.android.material.tabs.TabLayoutMediator

class EntriesOverviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntriesOverviewBinding

    val viewModelFactory: EntriesOverviewViewModel.Factory by lazy {
        EntriesOverviewViewModel.Factory(QuickLogRepository(LogDatabase.getInstance(applicationContext)))
    }

    private val viewModel: EntriesOverviewViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
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
}
