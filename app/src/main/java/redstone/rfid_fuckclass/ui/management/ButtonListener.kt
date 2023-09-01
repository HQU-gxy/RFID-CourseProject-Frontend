package redstone.rfid_fuckclass.ui.management

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.View.inflate
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import redstone.rfid_fuckclass.MainActivity
import redstone.rfid_fuckclass.R

class EditButtonListener(
    private val username: String,
    private val uid: String,
    private val context: Context, private val activity: Activity
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
                0 -> {
                    val adbModifyUsername = AlertDialog.Builder(context)
                    val theView = inflate(context, R.layout.input_new_username, null)
                    adbModifyUsername.setTitle("新用户名是啥")
                    adbModifyUsername.setView(theView)
                    adbModifyUsername.setPositiveButton(
                        "彳亍"
                    ) { _, _ ->
                        val newUsername =
                            theView.findViewById<EditText>(R.id.textNewUsername).text.toString()
                        Toast.makeText(context, "新用户名是$newUsername", Toast.LENGTH_SHORT).show()
                    }
                    adbModifyUsername.setNegativeButton("算了", null)
                    adbModifyUsername.show()
                }

                1 -> {

                    if (!MainActivity.nfcSupported) {
                        Toast.makeText(
                            context,
                            context.resources.getString(R.string.nfc_unsupported),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setItems
                    }

                    MainActivity.cardIDGotten = null
                    val cardWaitingView = inflate(context, R.layout.wait_4_card_id, null)
                    val cardIdTV = cardWaitingView.findViewById<TextView>(R.id.textCardID)
                    val buttonConfirmUID =
                        cardWaitingView.findViewById<Button>(R.id.buttonConfirmUID)

                    val cardWaitThread = Thread {
                        try {
                            while (MainActivity.cardIDGotten == null) {
                                Thread.sleep(500)
                            }
                            val newId = MainActivity.cardIDGotten
                            activity.runOnUiThread {
                                cardIdTV.text = newId
                                buttonConfirmUID.visibility = View.VISIBLE
                            }
                            MainActivity.cardIDGotten = null
                        } catch (e: InterruptedException) {
                            Log.i("NFC", "Card wait thread interrupted")
                        }
                    }
                    cardWaitThread.start()

                    val adbRebindCard = AlertDialog.Builder(context).apply {
                        setTitle("重新绑定卡片")
                        setView(cardWaitingView)
                        setCancelable(false)
                        setNegativeButton("算了") { _, _ ->
                            cardWaitThread.interrupt()
                            MainActivity.cardIDGotten = null
                        }
                    }
                    val dialogRebind = adbRebindCard.show()
                    buttonConfirmUID.setOnClickListener {
                        val newId = cardIdTV.text.toString()
                        Toast.makeText(context, "新卡片ID是$newId", Toast.LENGTH_SHORT).show()
                        dialogRebind.dismiss()
                    }

                }
            }
        }
        adbEdit.show()
    }

}

class ButtonNewUserListener(private val context: Context, private val activity: Activity) :
    OnClickListener {
    override fun onClick(v: View?) {
        if (!MainActivity.nfcSupported) {
            Toast.makeText(
                context,
                context.resources.getString(R.string.nfc_unsupported),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val newUserView = inflate(context, R.layout.input_new_username, null)
        val adbNewUser = AlertDialog.Builder(context).apply {
            setTitle("新用户叫啥")
            setView(newUserView)
        }

        adbNewUser.setPositiveButton(
            "彳亍"
        ) { _, _ ->
            val newUsername =
                newUserView.findViewById<EditText>(R.id.textNewUsername).text.toString()

            MainActivity.cardIDGotten = null
            val cardWaitingView = inflate(context, R.layout.wait_4_card_id, null)
            val cardIdTV = cardWaitingView.findViewById<TextView>(R.id.textCardID)
            val buttonConfirmUID = cardWaitingView.findViewById<Button>(R.id.buttonConfirmUID)

            val cardWaitThread = Thread {
                try {
                    while (MainActivity.cardIDGotten == null) {
                        Thread.sleep(500)
                    }
                    val newId = MainActivity.cardIDGotten
                    activity.runOnUiThread {
                        cardIdTV.text = newId
                        buttonConfirmUID.visibility = View.VISIBLE
                    }
                    MainActivity.cardIDGotten = null
                } catch (e: InterruptedException) {
                    Log.i("NFC", "Card wait thread interrupted")
                }
            }
            cardWaitThread.start()

            val adbBindCard = AlertDialog.Builder(context).apply {
                setTitle("绑定卡片")
                setView(cardWaitingView)
                setCancelable(false)
                setNegativeButton("算了") { _, _ ->
                    cardWaitThread.interrupt()
                    MainActivity.cardIDGotten = null
                }
            }
            val dialogBind = adbBindCard.show()
            buttonConfirmUID.setOnClickListener {
                val newId = cardIdTV.text.toString()
                Toast.makeText(context, "用户名：$newUsername，卡片ID：$newId", Toast.LENGTH_SHORT).show()
                dialogBind.dismiss()
            }
        }
        adbNewUser.setNegativeButton("算了", null)
        adbNewUser.show()
    }

}