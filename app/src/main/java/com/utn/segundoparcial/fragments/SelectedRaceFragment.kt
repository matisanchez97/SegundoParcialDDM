package com.utn.segundoparcial.fragments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.maps.android.PolyUtil
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
    private var editor: SharedPreferences.Editor? = null
    private var selectedRace : Race? = null
    private var positions: MutableList<LatLng> = ArrayList<LatLng>()
    lateinit var mSnapshotCallback: GoogleMap.SnapshotReadyCallback
    var raceId: Int = 0
    var currentUserId: String? = ""
    val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Main + parentJob)
    val storage = Firebase.storage
    lateinit var imageRef: StorageReference

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
            positions = PolyUtil.decode(selectedRace!!.route)
            googleMap
                .moveCamera(CameraUpdateFactory
                    .newLatLngZoom(positions.elementAt(0),15.toFloat()))
            var polylineOptions = PolylineOptions()
            for(position in positions){
                polylineOptions.add(position)
            }
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
        editor = sharedPref.edit()
        raceId = sharedPref.getInt("SELECTED_RACE_ID",-1)
        currentUserId = sharedPref.getString("CURRENT_USER_ID","")
        mSnapshotCallback = object : GoogleMap.SnapshotReadyCallback{
            override fun onSnapshotReady(p0: Bitmap?) {
                scope.launch {
                    val baos = ByteArrayOutputStream()
                    p0!!.compress(Bitmap.CompressFormat.JPEG,100,baos)
                    val data = baos.toByteArray()
                    imageRef = storage.reference.child("images/" + selectedRace!!.user + selectedRace!!.id.toString())
                    imageRef.putBytes(data).await()
                    selectedRace!!.downloadUri = imageRef.downloadUrl.await().toString()
                    updateRace(selectedRace!!)
                }
            }
        }

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