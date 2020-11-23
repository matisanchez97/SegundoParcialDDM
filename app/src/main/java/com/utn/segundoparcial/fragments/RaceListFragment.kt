package com.utn.segundoparcial.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.view.ActionMode
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.utn.segundoparcial.MainActivity
import com.utn.segundoparcial.R
import com.utn.segundoparcial.adapters.RaceListAdapter
import com.utn.segundoparcial.entities.Product
import com.utn.segundoparcial.entities.Race
import com.utn.segundoparcial.entities.User
import com.utn.segundoparcial.framework.*
import kotlinx.android.synthetic.main.fragment_race_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [RaceListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RaceListFragment : Fragment() {
    lateinit var v: View
    lateinit var recyclerProducts: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var butFloatAdd: FloatingActionButton
    lateinit var mainLayout: ConstraintLayout
    lateinit var textTitle: TextView

    lateinit var raceListAdapter: RaceListAdapter
    lateinit var selectedRace: Race

    var currentUserId: String = ""
    var currentUser: User? = null
    var raceList: MutableList<Race>? = ArrayList<Race>()
    var raceListaux: MutableList<Race>? = ArrayList<Race>()
    var selectedRaces: MutableList<Race>? = ArrayList<Race>()
    var selectedCards: MutableList<CardView>? = ArrayList<CardView>()
    var actionMode : ActionMode? = null
    var favMenu = false

    private var sortingOrder = 0
    private lateinit var callback : ActionMode.Callback

    val db = Firebase.firestore
    val racesCollectionRef = db.collection("races")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)     //Activo las opciones del toolbar
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_app_bar, menu)
        callback = object : ActionMode.Callback {           //Creo el AcrionMode para la contextual toolbar

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                inflater.inflate(R.menu.contextual_action_bar, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                val parentJob = Job()
                val scope = CoroutineScope(Dispatchers.Main + parentJob)

                return when (item?.itemId) {        //Defino el comportamiento de los items de la contextual toolbar
                    R.id.delete -> {

                        scope.launch {
                            deleteAllRaces(selectedRaces)
                            raceList?.removeAll(selectedRaces as Collection<Race>)
                            actionMode?.finish()
                            recyclerProducts.adapter?.notifyDataSetChanged()
                        }
                        true
                    }

                    R.id.more -> {
                        true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                selectedRaces = ArrayList<Race>()
                for (card in selectedCards!!){
                    card.setCardBackgroundColor(Color.parseColor("#ffffff"))
                }
                selectedCards = ArrayList<CardView>()
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)
        val id = when(item.itemId) {

            R.id.calculate ->{
                var total_run = 0
                for (race in raceList!!){
                    total_run += race.distance
                }
                Snackbar.make(mainLayout,"You have run a total of " + total_run.toString() +" mts", Snackbar.LENGTH_SHORT).show()
            }
            R.id.more -> {
                val action_8 = RaceListFragmentDirections.actionShoppinglistFragmentToSettingsActivity()
                findNavController().navigate(action_8)
            }
            else -> ""
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_race_list, container, false)
        recyclerProducts = v.findViewById(R.id.recyclerProducts)
        butFloatAdd = v.findViewById(R.id.floating_action_button)
        mainLayout = v.findViewById(R.id.welcomeLayout)
        textTitle = v.findViewById(R.id.textTitle_3)
        setHasOptionsMenu(true)
        return v
    }

    override fun onStart() {
        super.onStart()
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.app_name)
        currentUserId = RaceListFragmentArgs.fromBundle(requireArguments()).loggedUserId
        recyclerProducts.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerProducts.layoutManager = linearLayoutManager

        scope.launch {
            currentUser = getUserById(currentUserId)
            sortingOrder = setPrefs(currentUser,requireContext())
            raceList?.removeAll(raceList!!)
            raceList = getRacesByUser(currentUserId)
            raceListAdapter = RaceListAdapter(raceList!!,{ position, cardView -> OnItemClick(position,cardView)},{ position, cardView -> OnItemLongClick(position,cardView)})
            recyclerProducts.adapter = raceListAdapter
        }

        butFloatAdd.setOnClickListener {
            val action_6 = RaceListFragmentDirections.actionShoppinglistFragmentToStartRaceFragment(currentUser!!.id)
            findNavController().navigate(action_6)
        }
    }

    fun OnItemClick(position: Int,cardView: CardView){
        if(selectedRaces!!.isEmpty()) {              //Si no estamos en el Contextual Toolbar
            selectedRace = raceList!![position]  //Me voy para el DetailFragment
            val action_4 = RaceListFragmentDirections.actionShoppinglistFragmentToContainerProductFragment(selectedRace.id, currentUserId)
            findNavController().navigate(action_4)
        }
        else{
            if(selectedRaces!!.contains(raceList!![position])){  //Si estamos en el Contextual Toolbar
                selectedRaces!!.remove(raceList!![position])     //Y el producto ya esta seleccionado
                selectedCards!!.remove(cardView)
                cardView.setCardBackgroundColor(Color.parseColor("#ffffff"))
                if(selectedRaces?.size == 0)
                    actionMode?.finish()
                actionMode?.title = selectedRaces?.size.toString() + " selected"
            }
            else {
                selectedRaces?.add(raceList!![position])   //Si estamos en el Contextual Toolbar
                selectedCards?.add(cardView)                      //Y el producto no esta seleccionado
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.colorAccent))
                actionMode?.title = selectedRaces?.size.toString() + " selected"
            }
        }
    }

    fun OnItemLongClick(position: Int,cardView: CardView){
        selectedRaces?.add(raceList!![position])
        selectedCards?.add(cardView)
        cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.colorAccent))
        if(selectedRaces?.size == 1) {
            actionMode = (activity as MainActivity).startSupportActionMode(callback)
        }
        actionMode?.title = selectedRaces?.size.toString() + " selected"
    }
}


