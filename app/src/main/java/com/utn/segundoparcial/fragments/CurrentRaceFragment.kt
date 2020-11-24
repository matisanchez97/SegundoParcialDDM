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
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.PolyUtil
import com.utn.segundoparcial.MainActivity
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.Race
import com.utn.segundoparcial.entities.User
import com.utn.segundoparcial.framework.addRace
import com.utn.segundoparcial.framework.getRacesByUser
import com.utn.segundoparcial.framework.getUserById
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
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
    private lateinit var textViewDistance: TextView
    private lateinit var textViewSpeed: TextView
    private lateinit var butFloatStop: FloatingActionButton
    private lateinit var butFloatPause: FloatingActionButton
    private lateinit var Timer: Chronometer
    private var starPoint: Location? = null
    private var race: MutableList<Location> = ArrayList<Location>()
    private var route: MutableList<LatLng> = ArrayList<LatLng>()
    private var distance:Float = 0.toFloat()
    private var distanceAux:Float = 0.toFloat()
    private var distancePerKm:Float = 0.toFloat()
    private var speedPerKm:MutableList<Int> = arrayListOf(0)
    private var time: Long = 0
    private var timeAux: Long = 0
    private var timePerKm: Long = 0
    private var i =0
    private var isCounting = false
    var currentUserId: String = ""
    var currentUser: User? = null
    var allRaces: MutableList<Race> = ArrayList<Race>()
    val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Main + parentJob)

    val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            for (location in p0!!.locations) {
                race.add(location)
                route.add(LatLng(location.latitude,location.longitude))
                distance += race.elementAt(i).distanceTo(race.elementAt(i + 1))
                distancePerKm = distance-distanceAux
                i++
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        getLastLocation()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_current_race, container, false)
        textViewDistance = v.findViewById(R.id.textDistance)
        textViewSpeed = v.findViewById(R.id.textSpeed)
        Timer = v.findViewById(R.id.view_timer)
        butFloatPause = v.findViewById(R.id.floating_pause_button)
        butFloatStop = v.findViewById(R.id.floating_stop_button)

        Timer.base = SystemClock.elapsedRealtime()
        Timer.start()
        isCounting = true

        return v
    }

    override fun onStart() {
        super.onStart()
        currentUserId = CurrentRaceFragmentArgs.fromBundle(requireArguments()).currentUserId

        Timer.setOnChronometerTickListener {
            time = (SystemClock.elapsedRealtime() - Timer.base) / 1000
            timePerKm = time - timeAux
            speedPerKm.set(speedPerKm.size - 1, timePerKm.toInt())
            if ((distancePerKm.toInt() / 1000) >= 1) {
                speedPerKm.add(0)
                distanceAux += (distancePerKm - distancePerKm.rem(1000))
                distancePerKm = 0.toFloat()
                timeAux += timePerKm
                timePerKm = 0
            }
            val distanceText = "Distance :" + distance.toInt().toString() + " mts"
            var speedText = "Average Time Per Km :0' 00'' /km"
            if (distance > 0)
                speedText = "Average Time Per Km :" + ((time * 1000) / distance).div(60).toInt()
                    .toString() + "' " + ((time * 1000) / distance).rem(60).toInt()
                    .toString() + "'' /km"
            textViewDistance.text = distanceText

            textViewSpeed.text = speedText
        }

        butFloatPause.setOnClickListener {
            if (isCounting) {
                Timer.stop()
                isCounting = false
                butFloatPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
            } else {
                Timer.base = SystemClock.elapsedRealtime() - time * 1000
                Timer.start()
                isCounting = true
                butFloatPause.setImageResource(R.drawable.ic_baseline_pause_24)
                requestNewLocationData()
            }
        }

        butFloatStop.setOnClickListener {
            scope.launch {
                Timer.stop()
                isCounting = false
                fusedLocationProviderClient.removeLocationUpdates(mLocationCallback).await()
                currentUser = getUserById(currentUserId)
                allRaces = getRacesByUser(currentUserId)
                val i = allRaces.size
                val currentDate = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                val race = Race(
                    i,
                    currentUser!!.username,
                    distance.toInt(),
                    time,
                    PolyUtil.encode(route),
                    currentDate,
                    speedPerKm.toList()
                )
                addRace(race)
                val action =
                    CurrentRaceFragmentDirections.actionCurrentRaceToContainerProductFragment(
                        race.id,
                        currentUserId
                    )
                findNavController().navigate(action)
            }
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
                        route.add(LatLng(starPoint!!.latitude,starPoint!!.longitude))
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