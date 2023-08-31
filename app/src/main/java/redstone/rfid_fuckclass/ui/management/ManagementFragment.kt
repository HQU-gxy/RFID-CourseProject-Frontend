package redstone.rfid_fuckclass.ui.management

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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

        usersDataSet=managementViewModel.usersDataSet
        _binding = FragmentManagementBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val usersList = binding.userList
        val userListAdapter = UserListAdapter(usersDataSet)
        usersList.layoutManager = LinearLayoutManager(this.context)
        usersList.adapter = userListAdapter

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}