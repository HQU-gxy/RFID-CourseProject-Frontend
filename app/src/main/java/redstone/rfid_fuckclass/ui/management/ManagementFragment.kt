package redstone.rfid_fuckclass.ui.management

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import redstone.rfid_fuckclass.BuildConfig
import redstone.rfid_fuckclass.databinding.FragmentManagementBinding

class ManagementFragment : Fragment() {

    private var _binding: FragmentManagementBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var usersDataSet: MutableList<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val managementViewModel =
            ViewModelProvider(this)[ManagementViewModel::class.java]

        usersDataSet = managementViewModel.usersDataSet
        _binding = FragmentManagementBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val refreshLayout = binding.refreshLayoutMgmt
        val usersList = binding.userList
        val userListAdapter = UserListAdapter(usersDataSet, requireActivity(), refreshLayout)

        usersList.layoutManager = LinearLayoutManager(this.context)
        usersList.adapter = userListAdapter

        refreshLayout.setOnRefreshListener {
            fetchUserList(refreshLayout, userListAdapter)
        }
        refreshLayout.autoRefresh()

        binding.buttonNewUser.setOnClickListener(
            ButtonNewUserListener(
                requireContext(),
                requireActivity(),
                refreshLayout
            )
        )
        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchUserList(refreshLayout: SmartRefreshLayout, userListAdapter: UserListAdapter) {
        val httpClient =
            OkHttpClient.Builder().connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS).build()
        val request = Request.Builder()
            .url("${BuildConfig.serverAddr}/list_users").get().build()

        Thread {
            try {
                val response = httpClient.newCall(request).execute()
                val responseText = response.body?.string()
                println(responseText)
                val responseJSON = responseText?.let { JSONObject(it) }
                if (responseJSON != null)
                    if (responseJSON.getString("status").equals("SUC")) {
                        val recordsJSONArray = responseJSON.getJSONArray("users")
                        usersDataSet.clear()
                        for (i in 0 until recordsJSONArray.length()) {
                            val record = recordsJSONArray.getJSONObject(i)
                            usersDataSet.add(
                                arrayOf(
                                    record.getString("name"),
                                    record.getString("uid")
                                )
                            )

                        }
                        activity?.runOnUiThread { userListAdapter.notifyDataSetChanged() }
                    } else if (responseJSON.getString("status").equals("NO_RECORD")) {
                        usersDataSet.clear()
                        activity?.runOnUiThread { userListAdapter.notifyDataSetChanged() }
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}