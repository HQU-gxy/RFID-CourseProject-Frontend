package redstone.rfid_fuckclass.ui.management

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import redstone.rfid_fuckclass.R

class UserListAdapter(private val dataSet: MutableList<Array<String>>) :
    RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usernameTV: TextView
        val uidTV: TextView
        val editButton:ImageButton
        init {
            usernameTV = view.findViewById(R.id.textUsername)
            uidTV = view.findViewById(R.id.textUID)
            editButton=view.findViewById(R.id.buttonEdit)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val username=dataSet[position][0]
        val uid=dataSet[position][1]
        holder.usernameTV.text = username
        holder.uidTV.text = uid
        holder.editButton.setOnClickListener(EditButtonListener(username,uid,holder.itemView.context))
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}