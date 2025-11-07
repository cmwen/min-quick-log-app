package com.example.minandroidapp.ui.entries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minandroidapp.databinding.FragmentEntriesTagsBinding
import com.example.minandroidapp.ui.entries.EntriesOverviewActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class TagsFragment : Fragment() {

    private var _binding: FragmentEntriesTagsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntriesOverviewViewModel by activityViewModels {
        (requireActivity() as EntriesOverviewActivity).viewModelFactory
    }

    private lateinit var adapter: TagGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEntriesTagsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = TagGroupAdapter(onTagSelected = { tagId ->
            val list = viewModel.buildEntryListFromTags(tagId)
            val message = list.filterIsInstance<EntryListItem.Entry>()
                .joinToString(separator = "\n") { item ->
                    viewModel.formatSharePreview(item.logEntry)
                }
                .ifBlank { getString(com.example.minandroidapp.R.string.no_entries_message) }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(com.example.minandroidapp.R.string.entries_tab_tags))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        })
        binding.tagGroupsList.layoutManager = LinearLayoutManager(requireContext())
        binding.tagGroupsList.adapter = adapter

        binding.tagSearchInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setTagQuery(text?.toString().orEmpty())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredTagGroups.collect { groups ->
                adapter.submitList(groups)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
