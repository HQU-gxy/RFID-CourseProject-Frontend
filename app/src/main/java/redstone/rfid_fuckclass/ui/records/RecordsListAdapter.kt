package redstone.rfid_fuckclass.ui.records

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import redstone.rfid_fuckclass.R

class RecordsListAdapter(private val dataSet: MutableList<Array<String>>) :
    RecyclerView.Adapter<RecordsListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recordUsername: TextView
        val recordTime: TextView

        init {
            recordUsername = view.findViewById(R.id.textViewUsername)
            recordTime = view.findViewById(R.id.textViewSignTime)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.record_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.recordUsername.text = dataSet[position][0]
        holder.recordTime.text = dataSet[position][1]
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}