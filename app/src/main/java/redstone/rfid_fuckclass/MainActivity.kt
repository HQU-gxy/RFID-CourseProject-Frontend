package redstone.rfid_fuckclass

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcA
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import redstone.rfid_fuckclass.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var nfcAdapter: NfcAdapter

    companion object {
        var nfcSupported = false
        var cardIDGotten: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_records, R.id.navigation_managing
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val testNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (testNfcAdapter != null) {
            nfcSupported = true
            nfcAdapter = testNfcAdapter
        }

    }

    override fun onResume() {
        super.onResume()
        if (!nfcSupported) {
            return
        }
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED))

        val techListsArray =
            arrayOf(arrayOf<String>(NfcA::class.java.name, MifareClassic::class.java.name))

        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_MUTABLE
        )

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techListsArray)
    }

    override fun onPause() {
        super.onPause()
        if (nfcSupported)
            nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val tag: Tag? = if (Build.VERSION.SDK_INT >= 33) intent.getParcelableExtra(
            NfcAdapter.EXTRA_TAG, Tag::class.java
        ) else intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)

        Log.i("tag", tag.toString())
        if (tag != null) {
            cardIDGotten = byteArrayToHexString(tag.id)
        }
    }

    private fun byteArrayToHexString(ba: ByteArray): String {
        val sb = StringBuilder(ba.size * 2)
        for (b in ba)
            sb.append(String.format("%02X", b))
        return sb.toString()
    }
}