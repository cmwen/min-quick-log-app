package com.example.minandroidapp.ui.entries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minandroidapp.databinding.FragmentEntriesListBinding
import kotlinx.coroutines.launch

class SimpleEntriesFragment : Fragment() {

    private var _binding: FragmentEntriesListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntriesOverviewViewModel by lazy {
        ViewModelProvider(requireActivity())[EntriesOverviewViewModel::class.java]
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
        adapter = EntriesListAdapter(onEntryClicked = {}, onShareClicked = {})
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
