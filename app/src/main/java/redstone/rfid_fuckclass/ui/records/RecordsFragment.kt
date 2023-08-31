package redstone.rfid_fuckclass.ui.records

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import redstone.rfid_fuckclass.databinding.FragmentRecordsBinding
import kotlin.random.Random

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var recordsDataSet: MutableList<Array<String>>

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val recordsViewModel =
            ViewModelProvider(this)[RecordsViewModel::class.java]
        recordsDataSet = recordsViewModel.recordsDataSet

        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recordsList = binding.recordsList
        val recordsListAdapter = RecordsListAdapter(recordsDataSet)
        recordsList.layoutManager = LinearLayoutManager(this.context)
        recordsList.adapter = recordsListAdapter

        recordsDataSet.add(arrayOf("Stupid Asshole", "1145-1-4 19:19"))

        val refreshLayout = binding.refreshLayout
        refreshLayout.setOnRefreshListener {
            recordsListAdapter.notifyDataSetChanged()
            refreshLayout.finishRefresh()
        }
        refreshLayout.autoRefresh()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

