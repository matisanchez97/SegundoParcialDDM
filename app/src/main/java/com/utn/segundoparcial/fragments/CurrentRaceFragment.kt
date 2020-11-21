package com.utn.segundoparcial.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.Race
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.seconds


/**
 * A simple [Fragment] subclass.
 * Use the [CurrentRaceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CurrentRaceFragment : Fragment() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationCallback : LocationCallback
    private lateinit var textViewDistance: TextView
    private lateinit var butFloatStop: FloatingActionButton
    private lateinit var butFloatPause: FloatingActionButton
    private lateinit var Timer: Chronometer
    private lateinit var T: Timer
    private var starPoint: Location? = null
    private var race: MutableList<Location> = ArrayList<Location>()
    private var distance:Float = 0.toFloat()
    private var speed:Float = 0.toFloat()
    private var time: Long = 0
    private var route: String =""
    private var i =0
    private var isCounting = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                for (location in p0!!.locations) {
                    race.add(location)
                    route = route + "#" + race.elementAt(i+1).latitude.toString() + "," + race.elementAt(0).longitude.toString()
                    distance += race.elementAt(i).distanceTo(race.elementAt(i + 1))
                    i++
                    }
            }
        }
        getLastLocation()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_current_race, container, false)
        textViewDistance = v.findViewById(R.id.textDistance)
        Timer = v.findViewById(R.id.view_timer)
        butFloatPause = v.findViewById(R.id.floating_pause_button)
        butFloatStop = v.findViewById(R.id.floating_stop_button)
        return v
    }

    override fun onStart() {
        super.onStart()
        Timer.base = SystemClock.elapsedRealtime()
        Timer.start()
        isCounting = true
        Timer.setOnChronometerTickListener {
            time = (SystemClock.elapsedRealtime() - Timer.base)/1000
            if (Timer!=null && time > 0 )
                speed = distance.div(time)
            textViewDistance.text = "Distance :" + distance.toString() +" mts\nSpeed :" + speed.toString() + " mts/s"
        }
        butFloatPause.setOnClickListener {
            if(isCounting){
                Timer.stop()
                isCounting = false
                butFloatPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
            } else{
                Timer.base = SystemClock.elapsedRealtime()-time*1000
                Timer.start()
                isCounting = true
                butFloatPause.setImageResource(R.drawable.ic_baseline_pause_24)
                requestNewLocationData()
            }
        }
        butFloatStop.setOnClickListener {
            val race = Race(0,"matute",distance.toInt(),time,route)
            race.routePositions()
        }
    }




    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {


                fusedLocationProviderClient.lastLocation.addOnCompleteListener() { task ->
                    var location: Location? = task.result
                    if (location != null) {
                        starPoint = location
                        race.add(starPoint!!)
                        route =  race.elementAt(0).latitude.toString() + "," + race.elementAt(0).longitude.toString()
                        requestNewLocationData()
                    } else {
//                        findViewById<TextView>(R.id.latTextView).text = location.latitude.toString()
//                        findViewById<TextView>(R.id.lonTextView).text = location.longitude.toString()
                    }
                }


            } else {
                Toast.makeText(this.requireContext(), "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 500
        mLocationRequest.fastestInterval = 500

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        fusedLocationProviderClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this.requireActivity(),
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        )
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    }


}