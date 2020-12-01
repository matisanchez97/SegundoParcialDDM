package com.utn.segundoparcial.fragments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.maps.android.PolyUtil
import com.utn.segundoparcial.MainActivity
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.Race
import com.utn.segundoparcial.framework.getRaceByIdandUser
import com.utn.segundoparcial.framework.updateRace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class SelectedRaceFragment : Fragment() {

    private val PREF_NAME = "myPreferences"
    private var selectedRace : Race? = null
    private var positions: MutableList<LatLng> = ArrayList<LatLng>()
    var raceId: Int = 0
    var currentUserId: String? = ""
    val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Main + parentJob)
    val storage = Firebase.storage
    lateinit var imageRef: StorageReference


    private val mSnapshotCallback = object : GoogleMap.SnapshotReadyCallback{
        override fun onSnapshotReady(p0: Bitmap?) {
            val sharedPref: SharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            val isUpdating = sharedPref.getBoolean("IS_UPDATING",true)
            if (isUpdating) {
                scope.launch {
                    val baos = ByteArrayOutputStream()
                    p0!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    imageRef =
                        storage.reference.child("images/" + selectedRace!!.user + selectedRace!!.id.toString())
                    imageRef.putBytes(data).await()
                    selectedRace!!.downloadUri = imageRef.downloadUrl.await().toString()
                    updateRace(selectedRace!!)
                    editor.putBoolean("IS_UPDATING", false)
                    editor.apply()
                }
            }
        }
    }


    private val callback = OnMapReadyCallback { googleMap ->
        if (selectedRace != null){
            positions = PolyUtil.decode(selectedRace!!.route)
            val polylineOptions = PolylineOptions().width(15.toFloat())
            val bounds =setBounds(positions)
            for(position in positions){
                polylineOptions.add(position)
            }
            googleMap
                .moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,200))
            googleMap.addPolyline(polylineOptions)
            googleMap.setOnMapLoadedCallback {
                googleMap.snapshot(mSnapshotCallback)
            }

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
        val editor = sharedPref.edit()
        raceId = sharedPref.getInt("SELECTED_RACE_ID",-1)
        currentUserId = sharedPref.getString("CURRENT_USER_ID","")


        scope.launch {
            selectedRace = getRaceByIdandUser(currentUserId!!,raceId)
            if (selectedRace!!.downloadUri == ""){
                editor.putBoolean("IS_UPDATING",true)
                editor.apply()
            }
            val mapFragment = childFragmentManager.findFragmentById(R.id.map2) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    fun setBounds(positions: MutableList<LatLng>): LatLngBounds{
        var bounds: MutableList<LatLng> = arrayListOf(positions.elementAt(0),positions.elementAt(0),positions.elementAt(0),positions.elementAt(0))
        val builder = LatLngBounds.builder()
        for (position in positions) {
            if (position.latitude>bounds.elementAt(0).latitude){
                bounds.set(0,position)
            }
            if (position.latitude<bounds.elementAt(1).latitude){
                bounds.set(1,position)
            }
            if (position.longitude>bounds.elementAt(2).longitude){
                bounds.set(2,position)
            }
            if (position.latitude<bounds.elementAt(3).longitude){
                bounds.set(3,position)
            }
        }
        for (bound in bounds){
            builder.include(bound)
        }
        return builder.build()
    }
}