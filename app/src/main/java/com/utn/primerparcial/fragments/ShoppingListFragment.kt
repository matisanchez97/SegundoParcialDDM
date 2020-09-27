package com.utn.primerparcial.fragments

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
import com.utn.primerparcial.MainActivity
import com.utn.primerparcial.R
import com.utn.primerparcial.adapters.ShoppingListAdapter
import com.utn.primerparcial.database.appDatabase
import com.utn.primerparcial.database.productDao
import com.utn.primerparcial.database.userDao
import com.utn.primerparcial.entities.Product
import com.utn.primerparcial.entities.User
import com.wajahatkarim3.roomexplorer.RoomExplorer.show
import kotlinx.android.synthetic.main.fragment_shopping_list.*


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
    lateinit var favoriteListAdapter: ShoppingListAdapter
    lateinit var selectedProduct: Product
    lateinit var settingName: String
    lateinit var settingPassword: String

    var currentUserId: Int = 0
    var editProductPos: Int = 0
    var currentUser: User? = null
    var shoppingList: MutableList<Product>? = ArrayList<Product>()
    var selectedProducts: MutableList<Product>? = ArrayList<Product>()
    var selectedCards: MutableList<CardView>? = ArrayList<CardView>()
    var actionMode : ActionMode? = null
    var favMenu = false

    private var db: appDatabase? = null
    private var userDao: userDao? = null
    private var productDao: productDao? = null
    private var sortingOrder = 0
    private lateinit var callback : ActionMode.Callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_app_bar, menu)
        callback = object : ActionMode.Callback {

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                inflater.inflate(R.menu.contextual_action_bar, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.edit -> {
                        // Handle share icon press
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
                        // Handle delete icon press
                        shoppingList?.removeAll(selectedProducts as Collection<Product>)
                        userDao?.updatePerson(currentUser)
                        actionMode?.finish()
                        recyclerProducts.adapter = ShoppingListAdapter(shoppingList!!,{position,cardView -> OnItemClick(position,cardView)},{position,cardView -> OnItemLongClick(position,cardView)})
                        true
                    }
                    R.id.more -> {
                        // Handle more item (inside overflow menu) press
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
        val id = when(item.itemId) {

            R.id.favorite -> {
                if (favMenu) {
                    shoppingList = currentUser?.shopping_list
                    favoriteListAdapter = ShoppingListAdapter(
                        shoppingList!!,
                        { position, cardView -> OnItemClick(position, cardView) },
                        { position, cardView -> OnItemLongClick(position, cardView) })
                    recyclerProducts.adapter = shoppingListAdapter
                    textTitle_3.text = getString(R.string.title_3a)
                    favMenu = false
                } else {
                    shoppingList = currentUser?.favorite_products
                    favoriteListAdapter = ShoppingListAdapter(
                        shoppingList!!,
                        { position, cardView -> OnItemClick(position, cardView) },
                        { position, cardView -> OnItemLongClick(position, cardView) })
                    recyclerProducts.adapter = favoriteListAdapter
                    textTitle_3.text = getString(R.string.title_3b)
                    favMenu = true
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
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val editor = prefs.edit()

        (activity as MainActivity).supportActionBar?.title = getString(R.string.app_name)
        db = appDatabase.getAppDataBase(v.context)
        userDao = db?.userDao()
        productDao = db?.productDao()
        currentUserId = ShoppingListFragmentArgs.fromBundle(requireArguments()).loggedUserId
        currentUser = userDao?.loadPersonById(currentUserId)

        sortingOrder = prefs.getString("sort","0")!!.toInt()
        settingName = prefs.getString("first_name", currentUser?.name)!!
        settingPassword = prefs.getString("password", currentUser?.password)!!
        if (settingName.isEmpty()) {
            editor.putString("first_name", currentUser?.name)
            settingName = currentUser!!.name
        }
        if (settingPassword.isEmpty()) {
            editor.putString("password", currentUser?.password)
            settingPassword = currentUser!!.password
        }
        editor.apply()
        if (currentUser?.name != settingName)
            currentUser?.name = settingName
        if (currentUser?.password != settingPassword)
            currentUser?.password = settingPassword
        userDao?.updatePerson(currentUser)

        /*shoppingList = currentUser?.shopping_list
        when(sortingOrder){
            1 ->
                shoppingList!!.sortBy { it.id }
            2 ->
                shoppingList!!.sortBy { it.price }
            else -> ""
        }*/
        recyclerProducts.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerProducts.layoutManager = linearLayoutManager
        shoppingListAdapter = ShoppingListAdapter(shoppingList!!,{position,cardView -> OnItemClick(position,cardView)},{position,cardView -> OnItemLongClick(position,cardView)})
        recyclerProducts.adapter = shoppingListAdapter


        butFloatAdd.setOnClickListener {
            val action_5 = ShoppingListFragmentDirections.actionShoppinglistFragmentToAddDialogFragment(currentUserId,-1,-1)
            findNavController().navigate(action_5)
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