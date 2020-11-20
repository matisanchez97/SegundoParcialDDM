package com.utn.segundoparcial.fragments

import android.content.Context
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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.utn.segundoparcial.MainActivity
import com.utn.segundoparcial.R
import com.utn.segundoparcial.adapters.ShoppingListAdapter
import com.utn.segundoparcial.entities.Product
import com.utn.segundoparcial.entities.User
import com.utn.segundoparcial.framework.deleteProduct
import com.utn.segundoparcial.framework.getProductsByUser
import com.utn.segundoparcial.framework.getUserById
import com.utn.segundoparcial.framework.setPrefs
import kotlinx.android.synthetic.main.fragment_shopping_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


/**
 * A simple [Fragment] subclass.
 * Use the [ShoppingListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShoppingListFragment : Fragment() {
    lateinit var v: View
    lateinit var recyclerProducts: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var butFloatAdd: FloatingActionButton
    lateinit var mainLayout: ConstraintLayout
    lateinit var textTitle: TextView

    lateinit var shoppingListAdapter: ShoppingListAdapter
    lateinit var selectedProduct: Product
    lateinit var auxQuery: Query

    var currentUserId: Int = 0
    var editProductPos: Int = 0
    var currentUser: User? = null
    var shoppingList: MutableList<Product>? = ArrayList<Product>()
    var shoppingListaux: MutableList<Product>? = ArrayList<Product>()
    var selectedProducts: MutableList<Product>? = ArrayList<Product>()
    var selectedCards: MutableList<CardView>? = ArrayList<CardView>()
    var actionMode : ActionMode? = null
    var favMenu = false

    private var sortingOrder = 0
    private lateinit var callback : ActionMode.Callback

    val db = Firebase.firestore
    val productsCollectionRef = db.collection("products")

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
                    R.id.edit -> {
                        if (favMenu)
                            Snackbar.make(mainLayout,"You cant edit in Favorite List", Snackbar.LENGTH_SHORT).show()
                        else {
                            if (selectedProducts!!.size == 1){
                                editProductPos = shoppingList!!.indexOf(selectedProducts!![0])
                                val action_7 = ShoppingListFragmentDirections.actionShoppinglistFragmentToAddDialogFragment(currentUserId,editProductPos,-1)
                                actionMode?.finish()
                                findNavController().navigate(action_7)
                            }
                            else
                                Snackbar.make(mainLayout,"Please select only 1 product to edit", Snackbar.LENGTH_SHORT).show()

                        }
                        true
                    }
                    R.id.delete -> {

                        scope.launch {
                            if (favMenu) {
                                auxQuery
                                    .whereEqualTo("favorite", true)
                            } else {
                                auxQuery
                                    .whereEqualTo("shopping", true)

                            }
                            deleteProduct(auxQuery, selectedProducts)
                            shoppingList?.removeAll(selectedProducts as Collection<Product>)
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
                selectedProducts = ArrayList<Product>()
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
            R.id.favorite -> {
                if (favMenu) {
                    shoppingList?.removeAll(shoppingList!!)
                    shoppingList?.addAll(shoppingListaux!!)
                    recyclerProducts.adapter?.notifyDataSetChanged()
                    textTitle_3.text = getString(R.string.title_3a)
                    favMenu = false
                } else {
                    scope.launch {
                        favMenu = true
                        shoppingListaux = shoppingList?.toMutableList()
                        shoppingList?.removeAll(shoppingList!!)
                        getProductsByUser(0, currentUser, favMenu,shoppingList)
                        recyclerProducts.adapter?.notifyDataSetChanged()
                        textTitle_3.text = getString(R.string.title_3b)
                    }
                }
            }
            R.id.calculate ->{
                var total_cost = 0
                for (product in shoppingList!!){
                    total_cost += product.price * product.quantity
                }
                Snackbar.make(mainLayout,"The Calculated cost is $" + total_cost.toString(), Snackbar.LENGTH_SHORT).show()
            }
            R.id.more -> {
                val action_8 = ShoppingListFragmentDirections.actionShoppinglistFragmentToSettingsActivity()
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
        v = inflater.inflate(R.layout.fragment_shopping_list, container, false)
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
        currentUserId = ShoppingListFragmentArgs.fromBundle(requireArguments()).loggedUserId
        recyclerProducts.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerProducts.layoutManager = linearLayoutManager

        scope.launch {
            currentUser = getUserById(currentUserId)
            sortingOrder = setPrefs(currentUser,requireContext())
            shoppingList?.removeAll(shoppingList!!)
            getProductsByUser(sortingOrder,currentUser,false,shoppingList)
            auxQuery = productsCollectionRef
                .whereEqualTo("user",currentUser?.username)
            shoppingListAdapter = ShoppingListAdapter(shoppingList!!,{position,cardView -> OnItemClick(position,cardView)},{position,cardView -> OnItemLongClick(position,cardView)})
            recyclerProducts.adapter = shoppingListAdapter
        }

        butFloatAdd.setOnClickListener {
            val action_5 = ShoppingListFragmentDirections.actionShoppinglistFragmentToAddDialogFragment(currentUserId,-1,-1)
            val action_6 = ShoppingListFragmentDirections.actionShoppinglistFragmentToStartRaceFragment()
            findNavController().navigate(action_6)
        }
    }

    fun OnItemClick(position: Int,cardView: CardView){
        if(selectedProducts!!.isEmpty()) {              //Si no estamos en el Contextual Toolbar
            selectedProduct = shoppingList!![position]  //Me voy para el DetailFragment
            val action_4 = ShoppingListFragmentDirections.actionShoppinglistFragmentToContainerProductFragment(selectedProduct.id, currentUserId)
            findNavController().navigate(action_4)
        }
        else{
            if(selectedProducts!!.contains(shoppingList!![position])){  //Si estamos en el Contextual Toolbar
                selectedProducts!!.remove(shoppingList!![position])     //Y el producto ya esta seleccionado
                selectedCards!!.remove(cardView)
                cardView.setCardBackgroundColor(Color.parseColor("#ffffff"))
                if(selectedProducts?.size == 0)
                    actionMode?.finish()
                actionMode?.title = selectedProducts?.size.toString() + " selected"
            }
            else {
                selectedProducts?.add(shoppingList!![position])   //Si estamos en el Contextual Toolbar
                selectedCards?.add(cardView)                      //Y el producto no esta seleccionado
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.colorAccent))
                actionMode?.title = selectedProducts?.size.toString() + " selected"
            }
        }
    }

    fun OnItemLongClick(position: Int,cardView: CardView){
        selectedProducts?.add(shoppingList!![position])
        selectedCards?.add(cardView)
        cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.colorAccent))
        if(selectedProducts?.size == 1) {
            actionMode = (activity as MainActivity).startSupportActionMode(callback)
        }
        actionMode?.title = selectedProducts?.size.toString() + " selected"
    }
}


