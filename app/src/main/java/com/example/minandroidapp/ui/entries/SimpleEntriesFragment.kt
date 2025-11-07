package com.example.minandroidapp.ui.entries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minandroidapp.databinding.FragmentEntriesListBinding
import com.example.minandroidapp.ui.entries.EntriesOverviewActivity
import kotlinx.coroutines.launch

class SimpleEntriesFragment : Fragment() {

    private var _binding: FragmentEntriesListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntriesOverviewViewModel by activityViewModels {
        (requireActivity() as EntriesOverviewActivity).viewModelFactory
    }

    private lateinit var adapter: EntriesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEntriesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val shareHost = activity as? EntryShareHandler
        adapter = EntriesListAdapter(
            onEntryClicked = {},
            onShareClicked = { entry -> shareHost?.onShareEntry(entry) },
        )
        binding.entriesList.layoutManager = LinearLayoutManager(requireContext())
        binding.entriesList.adapter = adapter

        val tabType = arguments?.getString(ARG_TAB)?.let { EntriesTab.valueOf(it) } ?: EntriesTab.DATE

        val flow = when (tabType) {
            EntriesTab.DATE -> viewModel.entriesByDate
            EntriesTab.LOCATION -> viewModel.entriesByLocation
        }

        viewLifecycleOwner.lifecycleScope.launch {
            flow.collect { list -> adapter.submitList(list) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TAB = "tab"

        fun newInstance(tab: EntriesTab): SimpleEntriesFragment {
            val fragment = SimpleEntriesFragment()
            fragment.arguments = Bundle().apply { putString(ARG_TAB, tab.name) }
            return fragment
        }
    }
}
