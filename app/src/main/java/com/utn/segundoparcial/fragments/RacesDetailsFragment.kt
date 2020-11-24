package com.utn.segundoparcial.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.Race
import com.utn.segundoparcial.framework.getRaceByIdandUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [RacesDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RacesDetailsFragment() : Fragment() {

    lateinit var v: View
    lateinit var textViewDistance: TextView
    lateinit var textViewSpeed: TextView
    lateinit var textViewTime: TextView

    var raceId: Int = 0
    private val PREF_NAME = "myPreferences"
    private var editor: SharedPreferences.Editor? = null
    var currentUserId: String? = ""
    var selectedRace: Race? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_race_details, container, false)
        textViewDistance = v.findViewById(R.id.textViewDistance)
        textViewSpeed = v.findViewById(R.id.textViewSpeed)
        textViewTime = v.findViewById(R.id.textViewTime)
        return v
    }

    @SuppressLint("CommitPrefEdits")
    override fun onStart() {
        super.onStart()
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)
        editor = sharedPref.edit()
        raceId = sharedPref.getInt("SELECTED_RACE_ID",-1)
        currentUserId = sharedPref.getString("CURRENT_USER_ID","")


        scope.launch {
            selectedRace = getRaceByIdandUser(currentUserId!!,raceId)
            textViewDistance.text = "Race Distance : " + selectedRace!!.distance.toString() +" mts"
            textViewSpeed.text = "Race Average Speed : "+ ((selectedRace!!.time*1000)/selectedRace!!.distance).div(60).toString() + "' "+((selectedRace!!.time*1000)/selectedRace!!.distance).rem(60).toString()+"'' /km"
            textViewTime.text = "Race Time : " + selectedRace!!.time.div(60).toString() + "' " + selectedRace!!.time.rem(60).toString() + "''"
        }
    }

}