package redstone.rfid_fuckclass.ui.records

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.scwang.smart.refresh.layout.api.RefreshLayout
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import redstone.rfid_fuckclass.BuildConfig
import redstone.rfid_fuckclass.databinding.FragmentRecordsBinding

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var recordsDataSet: MutableList<Array<String>>

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
        val refreshLayout = binding.refreshLayoutRec

        recordsList.layoutManager = LinearLayoutManager(this.context)
        recordsList.adapter = recordsListAdapter

        refreshLayout.setOnRefreshListener {
            fetchRecords(refreshLayout, recordsListAdapter)
        }

        refreshLayout.autoRefresh()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("NotifyDataSetChanged")
    fun fetchRecords(refreshLayout: RefreshLayout, recordsListAdapter: RecordsListAdapter) {
        val httpClient =
            OkHttpClient.Builder().connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS).build()
        val request = Request.Builder()
            .url("${BuildConfig.serverAddr}/list_records").get().build()

        Thread {
            try {
                val response = httpClient.newCall(request).execute()
                val responseText = response.body?.string()
                println(responseText)
                val responseJSON = responseText?.let { JSONObject(it) }
                if (responseJSON != null)
                    if (responseJSON.getString("status").equals("SUC")) {
                        val recordsJSONArray = responseJSON.getJSONArray("records")
                        recordsDataSet.clear()
                        for (i in 0 until recordsJSONArray.length()) {
                            val record = recordsJSONArray.getJSONObject(i)
                            recordsDataSet.add(
                                arrayOf(
                                    record.getString("username"),
                                    record.getString("sign_dt")
                                )
                            )
                        }
                        activity?.runOnUiThread { recordsListAdapter.notifyDataSetChanged() }
                    } else if (responseJSON.getString("status").equals("NO_RECORD")) {
                        recordsDataSet.clear()
                        activity?.runOnUiThread { recordsListAdapter.notifyDataSetChanged() }
                    }

            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "连不上服务器", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
            refreshLayout.finishRefresh()
        }.start()
    }

}

