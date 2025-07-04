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
import com.scwang.smart.refresh.layout.api.RefreshLayout
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import redstone.rfid_fuckclass.BuildConfig
import redstone.rfid_fuckclass.MainActivity
import redstone.rfid_fuckclass.R

class EditButtonListener(
    private val username: String,
    private val uid: String,
    private val context: Context,
    private val activity: Activity,
    private val refreshLayout: RefreshLayout
) : OnClickListener {
    override fun onClick(v: View?) {
        val options = arrayOf(
            context.resources.getString(R.string.modify_username),
            context.resources.getString(R.string.rebind_card),
            context.resources.getString(R.string.remove_user)
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
                        submitEdit(newUsername, null)
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
                        submitEdit(null, newId, dialogRebind)
                    }

                }

                2 -> {
                    val adbRemoveUser = AlertDialog.Builder(context)
                    adbRemoveUser.setTitle("删除用户：$username")
                    adbRemoveUser.setPositiveButton("彳亍") { _, _ ->
                        submitRemove()
                    }
                    adbRemoveUser.setNegativeButton("算了", null)
                    adbRemoveUser.show()
                }
            }
        }
        adbEdit.show()
    }

    private fun submitEdit(
        newUsername: String?,
        newUId: String?,
        dialog: AlertDialog? = null
    ) {
        val httpClient =
            OkHttpClient.Builder().connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS).build()
        val formBodyBuilder = FormBody.Builder()
        formBodyBuilder.add("uid", uid)
        formBodyBuilder.add("to-change-uid", if (newUId == null) "0" else "1")
        formBodyBuilder.add("to-change-name", if (newUsername == null) "0" else "1")
        if (newUId != null) formBodyBuilder.add("new-uid", newUId)
        if (newUsername != null) formBodyBuilder.add("new-name", newUsername)
        val formBody = formBodyBuilder.build()

        val request = Request.Builder()
            .url("${BuildConfig.serverAddr}/modify_info").post(formBody).build()

        Thread {
            try {
                val response = httpClient.newCall(request).execute()
                val responseText = response.body?.string()
                println(responseText)
                val responseJSON = responseText?.let { JSONObject(it) } ?: return@Thread

                if (responseJSON.getString("status").equals("SUC")) {
                    activity.runOnUiThread {
                        Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show()
                    }
                } else if (responseJSON.getString("status").equals("UID_EXIST")) {
                    activity.runOnUiThread {
                        Toast.makeText(context, "这个卡已经添加过了", Toast.LENGTH_SHORT).show()
                    }
                } else if (responseJSON.getString("status").equals("NAME_EXIST")) {
                    activity.runOnUiThread {
                        Toast.makeText(context, "已经🈶这个用户了", Toast.LENGTH_SHORT).show()
                    }
                } else if (responseJSON.getString("status").equals("NO_USER")) {
                    activity.runOnUiThread {
                        Toast.makeText(context, "没这用户", Toast.LENGTH_SHORT).show()
                    }
                }
                activity.runOnUiThread { refreshLayout.autoRefresh() }
            } catch (e: Exception) {
                activity.runOnUiThread {
                    Toast.makeText(context, "连不上服务器", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
            dialog?.dismiss()
        }.start()
    }

    private fun submitRemove() {
        val httpClient =
            OkHttpClient.Builder().connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS).build()
        val formBodyBuilder = FormBody.Builder()
        formBodyBuilder.add("uid", uid)
        val formBody = formBodyBuilder.build()
        val request = Request.Builder()
            .url("${BuildConfig.serverAddr}/del_user").post(formBody).build()
        Thread {
            try {
                val response = httpClient.newCall(request).execute()
                val responseText = response.body?.string()
                println(responseText)
                val responseJSON = responseText?.let { JSONObject(it) } ?: return@Thread
                val status = responseJSON.getString("status")
                Log.i("ServerStat", status)
                when (status) {
                    "SUC" -> activity.runOnUiThread {
                        Toast.makeText(
                            context,
                            "删除成功",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    "NO_USER" ->
                        activity.runOnUiThread {
                            Toast.makeText(context, "没这用户", Toast.LENGTH_SHORT).show()
                        }
                }
                activity.runOnUiThread { refreshLayout.autoRefresh() }
            } catch (e: Exception) {
                activity.runOnUiThread {
                    Toast.makeText(context, "连不上服务器", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }.start()
    }
}


class ButtonNewUserListener(
    private val context: Context,
    private val activity: Activity,
    private val refreshLayout: RefreshLayout
) :
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
                submitNewUser(dialogBind, newUsername, newId)
            }
        }
        adbNewUser.setNegativeButton("算了", null)
        adbNewUser.show()
    }

    private fun submitNewUser(dialog: AlertDialog, username: String, uid: String) {
        val httpClient =
            OkHttpClient.Builder().connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS).build()
        val formBodyBuilder = FormBody.Builder()
        formBodyBuilder.add("name", username)
        formBodyBuilder.add("uid", uid)
        val formBody = formBodyBuilder.build()
        val request = Request.Builder()
            .url("${BuildConfig.serverAddr}/add_user").post(formBody).build()
        Thread {
            try {
                val response = httpClient.newCall(request).execute()
                val responseText = response.body?.string()
                println(responseText)
                val responseJSON = responseText?.let { JSONObject(it) } ?: return@Thread
                val status = responseJSON.getString("status")
                Log.i("ServerStat", status)
                when (status) {
                    "SUC" -> activity.runOnUiThread {
                        Toast.makeText(
                            context,
                            "添加成功",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    "UID_EXIST" ->
                        activity.runOnUiThread {
                            Toast.makeText(context, "这个卡已经添加过了", Toast.LENGTH_SHORT).show()
                        }

                    "NAME_EXIST" ->
                        activity.runOnUiThread {
                            Toast.makeText(context, "已经🈶这个用户了", Toast.LENGTH_SHORT).show()
                        }
                }
                activity.runOnUiThread { refreshLayout.autoRefresh() }
            } catch (e: Exception) {
                activity.runOnUiThread {
                    Toast.makeText(context, "连不上服务器", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
            dialog.dismiss()

        }.start()
    }
}