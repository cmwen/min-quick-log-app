package com.example.minandroidapp.ui.entries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.minandroidapp.databinding.FragmentEntriesStatsBinding
import com.example.minandroidapp.ui.entries.EntriesOverviewActivity
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private var _binding: FragmentEntriesStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntriesOverviewViewModel by activityViewModels {
        (requireActivity() as EntriesOverviewActivity).viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEntriesStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entryStats.collect { stats ->
                binding.statsTotal.text = getString(com.example.minandroidapp.R.string.entries_stats_total, stats.total)
                binding.statsUniqueTags.text = getString(com.example.minandroidapp.R.string.entries_stats_unique_tags, stats.uniqueTags)
                val topLabel = stats.topTagLabel ?: "—"
                binding.statsTopTag.text = getString(com.example.minandroidapp.R.string.entries_stats_top_tag, topLabel, stats.topTagCount)
                binding.statsAvgDaily.text = getString(com.example.minandroidapp.R.string.entries_stats_avg_daily, stats.averagePerDay)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
