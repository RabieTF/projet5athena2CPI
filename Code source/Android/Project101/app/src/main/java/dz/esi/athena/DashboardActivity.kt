package dz.esi.athena

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esi.athena.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.lang.Error
import java.util.*

class DashboardActivity : AppCompatActivity(), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var database: DatabaseReference
    private lateinit var humidityText : TextView
    private lateinit var temperatureText : TextView
    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var locationManager: LocationManager
    private var doubleBackToExitPressedOnce = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        database = Firebase.database.reference
        humidityText = findViewById(R.id.humidityText)
        temperatureText = findViewById(R.id.temperatureText)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager




        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1);
            }else{
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1);
            }
        }

        // Setting a timer for updating the position each 15sec on the realtime database
        val updateDbTimer: Timer = Timer()
        updateDbTimer.schedule(object: TimerTask(){
            override fun run() {
                    sendLocationToDB()
            }

        }, 5000, 15000)



        //Part responsible for getting temps and humidity
        val postListener = object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.value.toString()
                val arr = post.split(",")
                val temp = arr[0].split("=")
                val humid = arr[1].split("=")
                humidityText.text = "${humid[1].dropLast(1)}%"
                temperatureText.text = "${temp[1]}° C"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ERROR", "Something went wrong in retrieving data: ${error.message}")
                Toast.makeText(
                    baseContext, "Something went wrong",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        database.addValueEventListener(postListener)



        //Part responsible for Maps Location API Config
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            doubleBackToExitPressedOnce = false
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

    }


    //Implementation for Maps Location API
    override fun onMapReady(p0: GoogleMap?) {
        map = p0?: return
        p0.setOnMyLocationButtonClickListener(this)
        p0.setOnMyLocationClickListener(this)
        enableMyLocation()

    }




    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Something went wrong! Please try again!", Toast.LENGTH_LONG).show()

            return
        }
        map.isMyLocationEnabled = true
        Toast.makeText(this, "Location enabled", Toast.LENGTH_LONG).show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Current location:\n$p0", Toast.LENGTH_LONG).show()
    }

    private fun sendLocationToDB(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show()
            return
        }
        val pos = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val latitude = pos?.latitude.toString()
        val longitude = pos?.longitude.toString()

        val key = database.child("position").push().key
        if (key == null) {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show()
            return
        }


        val childUpdates = hashMapOf<String, Any>(
            "/position" to mapOf(
                "longitude" to longitude,
                "latitude" to latitude
            )
        )



        database.updateChildren(childUpdates)

        childUpdates.put("created", FieldValue.serverTimestamp().toString())

        val store = FirebaseFirestore.getInstance()
        store.collection("position")
            .add(childUpdates)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }

    }


}




