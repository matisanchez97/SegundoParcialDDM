package com.utn.segundoparcial.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.utn.segundoparcial.MainActivity
import com.utn.segundoparcial.R
import com.utn.segundoparcial.adapters.RaceListAdapter
import com.utn.segundoparcial.entities.Product
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
            textViewSpeed.text = "Race Average Speed : "+ (selectedRace!!.distance/selectedRace!!.time).toString() +" mts/s"
            textViewTime.text = "Race Time : " + selectedRace!!.time.toString() +" seg"
        }
    }
    /*fun OnItemClick(position: Int,cardView: CardView){
        selectedProduct = similarProductList!![position]
        editor?.putInt("SELECTED_RACE_ID",selectedProduct!!.id)
        editor?.apply()
        val tabLayout = (activity as MainActivity).findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.getTabAt(0)?.select()
    }

    fun OnItemLongClick(position: Int,cardView: CardView){

    }

    override fun onResume() {
        super.onResume()
        val sharedPref: SharedPreferences =
            requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        raceId = sharedPref.getInt("SELECTED_RACE_ID", -1)
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)

        scope.launch {
            val query = productsCollectionRef
                .whereEqualTo("user", "debug")
                .whereEqualTo("id", raceId)
            selectedProduct = getProductByQuery(query)
            getAllProducts(allProducts)
            similarProductList?.removeAll(similarProductList!!)
            for (item in PRODUCT_CODES) {
                if (selectedProduct!!.name.startsWith(item)) {
                    for (product in allProducts!!) {
                        if (product!!.name.startsWith(item))
                            similarProductList?.add(product)
                    }
                }
            }
            recyclerSimilarProducts.adapter?.notifyDataSetChanged()

        }
    }*/

}