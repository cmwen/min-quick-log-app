package com.example.minandroidapp.ui.entries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minandroidapp.R
import com.example.minandroidapp.databinding.FragmentEntriesStatsBinding
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private var _binding: FragmentEntriesStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntriesOverviewViewModel by activityViewModels {
        (requireActivity() as EntriesOverviewActivity).viewModelFactory
    }

    private val adapter = StatBarAdapter()
    private var dateData: List<StatCount> = emptyList()
    private var locationData: List<StatCount> = emptyList()
    private var tagData: List<StatCount> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEntriesStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.statsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.statsRecycler.adapter = adapter

        setupTabs()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entryStats.collect { stats ->
                binding.statsTotal.text = getString(R.string.entries_stats_total, stats.total)
                binding.statsUniqueTags.text = getString(R.string.entries_stats_unique_tags, stats.uniqueTags)
                val topLabel = stats.topTagLabel ?: "—"
                binding.statsTopTag.text = getString(R.string.entries_stats_top_tag, topLabel, stats.topTagCount)
                binding.statsAvgDaily.text = getString(R.string.entries_stats_avg_daily, stats.averagePerDay)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dateCounts.collect {
                dateData = it
                if (binding.statsTabs.selectedTabPosition == 0) renderChart(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.locationCounts.collect {
                locationData = it
                if (binding.statsTabs.selectedTabPosition == 1) renderChart(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tagCounts.collect {
                tagData = it
                if (binding.statsTabs.selectedTabPosition == 2) renderChart(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupTabs() {
        val tabs = binding.statsTabs
        if (tabs.tabCount == 0) {
            tabs.addTab(tabs.newTab().setText(R.string.entries_stats_tab_date))
            tabs.addTab(tabs.newTab().setText(R.string.entries_stats_tab_location))
            tabs.addTab(tabs.newTab().setText(R.string.entries_stats_tab_tags))
        }
        tabs.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                when (tab.position) {
                    0 -> renderChart(dateData)
                    1 -> renderChart(locationData)
                    else -> renderChart(tagData)
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                onTabSelected(tab)
            }
        })
        tabs.getTabAt(0)?.select()
        renderChart(dateData)
    }

    private fun renderChart(data: List<StatCount>) {
        if (data.isEmpty()) {
            binding.emptyStatsMessage.visibility = View.VISIBLE
            binding.statsRecycler.visibility = View.GONE
        } else {
            binding.emptyStatsMessage.visibility = View.GONE
            binding.statsRecycler.visibility = View.VISIBLE
            adapter.submitData(data.take(20))
        }
    }
}
