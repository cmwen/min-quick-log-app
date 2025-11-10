package com.example.minandroidapp.ui.entries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minandroidapp.databinding.FragmentEntriesListBinding
import com.example.minandroidapp.model.LogEntry
import com.example.minandroidapp.ui.common.SwipeToDeleteCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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
        val actionHost = activity as? EntryActionHandler
        adapter = EntriesListAdapter(
            onEntryClicked = { entry -> actionHost?.onEditEntry(entry) },
            onShareClicked = { entry -> actionHost?.onShareEntry(entry) },
            onToggleSelection = { entry -> viewModel.toggleEntrySelection(entry.id) },
        )
        binding.entriesList.layoutManager = LinearLayoutManager(requireContext())
        binding.entriesList.adapter = adapter

        val tabType = arguments?.getString(ARG_TAB)?.let { EntriesTab.valueOf(it) } ?: EntriesTab.DATE

        val flow = when (tabType) {
            EntriesTab.DATE -> viewModel.entriesByDate
            EntriesTab.LOCATION -> viewModel.entriesByLocation
        }

        attachSwipeToDelete()

        viewLifecycleOwner.lifecycleScope.launch {
            flow.collect { list -> adapter.submitList(list) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedEntryIds.collect { ids -> adapter.setSelection(ids) }
        }
    }

    private fun attachSwipeToDelete() {
        val swipeCallback = object : SwipeToDeleteCallback(requireContext(), canSwipe = { holder ->
            holder.itemViewType == EntriesListAdapter.VIEW_ENTRY
        }) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val entry = adapter.getEntryAt(position)
                if (entry == null) {
                    adapter.notifyItemChanged(position)
                    return
                }
                confirmSwipeDelete(entry, position)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.entriesList)
    }

    private fun confirmSwipeDelete(entry: LogEntry, adapterPosition: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(com.example.minandroidapp.R.string.delete_entries))
            .setMessage(getString(com.example.minandroidapp.R.string.delete_entries_confirm_message, 1))
            .setPositiveButton(com.example.minandroidapp.R.string.delete_action) { _, _ ->
                viewModel.deleteEntries(listOf(entry.id)) { success ->
                    if (success) {
                        Snackbar.make(
                            binding.root,
                            getString(com.example.minandroidapp.R.string.delete_entries_success, 1),
                            Snackbar.LENGTH_LONG,
                        ).show()
                    } else {
                        Snackbar.make(
                            binding.root,
                            getString(com.example.minandroidapp.R.string.delete_entries_failure),
                            Snackbar.LENGTH_LONG,
                        ).show()
                        adapter.notifyItemChanged(adapterPosition)
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                adapter.notifyItemChanged(adapterPosition)
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(adapterPosition)
            }
            .show()
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
