package dz.esi.athena

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.SoftApConfiguration
import androidx.appcompat.app.AppCompatActivity
import com.esi.athena.R
import 	android.net.wifi.WifiManager
import android.os.*
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class WifiActivity : AppCompatActivity() {
    private lateinit var hotspotStatusText: TextView
    private lateinit var hotspotConfig: SoftApConfiguration

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotspot)

        hotspotStatusText = findViewById<TextView>(R.id.hotspotStatus)
    }
    /*
    override fun onStart() {
        super.onStart()
        val intentFilter: IntentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(wifiStateReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(wifiStateReceiver)
    }


    if (wifiIsOn()) hotspotStatusText.text = "Please turn off your WIFI"
    else {
        hotspotStatusText.text = "Activating hotspot, Please wait..."
        if (startHotspot()){
            Toast.makeText(baseContext, "Hotspot activated!",
                Toast.LENGTH_SHORT).show()
        }
    }*/

    /*val wifiStateReceiver = object: BroadcastReceiver(){
        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            val wifiExtra: Int =
                intent?.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN) ?: -1
            when(wifiExtra) {
                WifiManager.WIFI_STATE_ENABLED -> {
                    hotspotStatusText.text = "Please disable your WIFI"
                }
                WifiManager.WIFI_STATE_DISABLED -> {
                    if (startHotspot()) {
                        hotspotStatusText.text = "Hotspot enabled"
                        startActivity(Intent(this, WifiActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }*/

    fun wifiIsOn(): Boolean {
        val manager: WifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        return manager.isWifiEnabled()
    }





    @RequiresApi(Build.VERSION_CODES.O)
    fun startHotspot(): Boolean {  //Returns true if hotspot started successfully
        var success: Boolean = false
        val manager: WifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        manager.startLocalOnlyHotspot( object: WifiManager.LocalOnlyHotspotCallback(){
            @RequiresApi(Build.VERSION_CODES.R)
            override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                super.onStarted(reservation)
                Log.v("HOTSPOT", "Wifi Hotspot started")
                if (reservation != null) {
                    hotspotConfig = reservation.softApConfiguration
                }
                success = true
            }

            override fun onStopped() {
                super.onStopped()
                Log.v("HOTSPOT", "Wifi Hotspot stopped")
                success = false
            }

            override fun onFailed(reason: Int) {
                super.onFailed(reason)
                Log.e("HOTSPOT", "Hotspot creation failure, code: $reason")
                success = false
            }
        }, Handler(Looper.getMainLooper()))
        return success
    }

}