package redstone.rfid_fuckclass.ui.management

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.View.OnClickListener
import android.view.View.inflate
import android.widget.EditText
import android.widget.Toast
import redstone.rfid_fuckclass.R

class EditButtonListener(
    private val username: String,
    private val uid: String,
    private val context: Context
) : OnClickListener {
    override fun onClick(v: View?) {
        val options = arrayOf(
            context.resources.getString(R.string.modify_username),
            context.resources.getString(R.string.rebind_card)
        )
        val adbEdit = AlertDialog.Builder(context)
        adbEdit.setTitle("修改用户：$username")
        adbEdit.setItems(options) { _, which ->
            when (which) {
                0 ->{
                    val adbModifyUsername=AlertDialog.Builder(context)
                    val theView= inflate(context,R.layout.input_new_username,null)
                    adbModifyUsername.setTitle("新用户名是啥")
                    adbModifyUsername.setView(theView)
                    adbModifyUsername.setPositiveButton("彳亍"
                    ) { _, _ ->
                        val newUsername=theView.findViewById<EditText>(R.id.textNewUsername).text.toString()
                        Toast.makeText(context,"新用户名是$newUsername",Toast.LENGTH_SHORT).show()
                    }
                    adbModifyUsername.setNegativeButton("算了",null)
                    adbModifyUsername.show()
                }
                1->{
                    val adbRebindCard=AlertDialog.Builder(context)
                    adbRebindCard.setTitle("重新绑定卡片")
                    adbRebindCard.setPositiveButton("彳亍"
                    ) { _, _ ->
                        Toast.makeText(context,"重新绑定卡片",Toast.LENGTH_SHORT).show()
                    }
                    adbRebindCard.setNegativeButton("算了",null)
                    adbRebindCard.show()
                }
            }
        }
        adbEdit.show()
    }

}