package com.utn.segundoparcial.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.utn.segundoparcial.R
import com.utn.segundoparcial.adapters.KilometerListAdapter
import com.utn.segundoparcial.entities.Race
import com.utn.segundoparcial.framework.getRaceByIdandUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [KilometerTimesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class KilometerTimesFragment() : Fragment() {

    lateinit var v: View
    lateinit var recyclerBrandProducts: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var kilometerListAdapter: KilometerListAdapter

    var raceId: Int = 0
    var currentUserId: String? = ""
    private val PREF_NAME = "myPreferences"
    private var editor: SharedPreferences.Editor? = null
    var selectedRace: Race? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_kilometer_times, container, false)
        recyclerBrandProducts = v.findViewById(R.id.recyclerBrandProducts)
        return v
    }

    override fun onStart() {
        super.onStart()
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)
        editor = sharedPref.edit()
        raceId = sharedPref.getInt("SELECTED_RACE_ID",-1)
        currentUserId = sharedPref.getString("CURRENT_USER_ID","")

        recyclerBrandProducts.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerBrandProducts.layoutManager = linearLayoutManager

        scope.launch {
            selectedRace = getRaceByIdandUser(currentUserId!!,raceId)
            kilometerListAdapter = KilometerListAdapter(selectedRace!!.timePerKm.toMutableList())
            recyclerBrandProducts.removeAllViews()
            if(!(selectedRace!!.timePerKm.isNullOrEmpty())){
                recyclerBrandProducts.adapter = kilometerListAdapter
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)

        scope.launch {
            selectedRace = getRaceByIdandUser(currentUserId!!,raceId)
            kilometerListAdapter = KilometerListAdapter(selectedRace!!.timePerKm.toMutableList())
            recyclerBrandProducts.removeAllViews()
            if(!(selectedRace!!.timePerKm.isNullOrEmpty())){
                recyclerBrandProducts.adapter = kilometerListAdapter
            }
        }
    }
}