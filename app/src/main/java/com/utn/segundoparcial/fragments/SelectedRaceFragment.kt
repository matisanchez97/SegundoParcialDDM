package com.utn.segundoparcial.fragments

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.Race
import com.utn.segundoparcial.framework.getRaceByIdandUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SelectedRaceFragment : Fragment() {

    private val PREF_NAME = "myPreferences"
    private var editor: SharedPreferences.Editor? = null
    private var selectedRace : Race? = null
    private var positions: MutableList<LatLng> = ArrayList<LatLng>()
    var raceId: Int = 0
    var currentUserId: String? = ""
    val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Main + parentJob)

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        if (selectedRace != null){
            positions = selectedRace!!.routePositions()
            googleMap
                .moveCamera(CameraUpdateFactory
                    .newLatLngZoom(positions.elementAt(0),15.toFloat()))
            var polylineOptions = PolylineOptions()
            for(position in positions){
                polylineOptions.add(position)
            }
            googleMap.addPolyline(polylineOptions)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_selected_race, container, false)
    }

    override fun onStart() {
        super.onStart()
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = sharedPref.edit()
        raceId = sharedPref.getInt("SELECTED_RACE_ID",-1)
        currentUserId = sharedPref.getString("CURRENT_USER_ID","")
        scope.launch {
            selectedRace = getRaceByIdandUser(currentUserId!!,raceId)
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}