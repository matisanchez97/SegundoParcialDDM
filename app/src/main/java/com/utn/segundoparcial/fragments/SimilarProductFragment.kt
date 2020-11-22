package com.utn.segundoparcial.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.utn.segundoparcial.MainActivity
import com.utn.segundoparcial.R
import com.utn.segundoparcial.adapters.ShoppingListAdapter
import com.utn.segundoparcial.constants.PRODUCT_CODES
import com.utn.segundoparcial.entities.Product
import com.utn.segundoparcial.framework.getAllProducts
import com.utn.segundoparcial.framework.getProductByQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [SimilarProductFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SimilarProductFragment() : Fragment() {

    lateinit var v: View
    lateinit var recyclerSimilarProducts: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var shoppingListAdapter: ShoppingListAdapter

    val db = Firebase.firestore
    val usersCollectionRef = db.collection("users")
    val productsCollectionRef = db.collection("products")

    var raceId: Int = 0
    private val PREF_NAME = "myPreferences"
    private var editor: SharedPreferences.Editor? = null
    var selectedProduct: Product? = null
    var allProducts: MutableList<Product>? = ArrayList<Product>()
    var similarProductList: MutableList<Product>? = ArrayList<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_similar_product, container, false)
        recyclerSimilarProducts = v.findViewById(R.id.recyclerSimilarProducts)
        return v
    }

    override fun onStart() {
        super.onStart()
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)
        editor = sharedPref.edit()
        raceId = sharedPref.getInt("SELECTED_RACE_ID",-1)
        recyclerSimilarProducts.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerSimilarProducts.layoutManager = linearLayoutManager

        scope.launch {
            val query = productsCollectionRef
                .whereEqualTo("user","debug")
                .whereEqualTo("id",raceId)
            selectedProduct = getProductByQuery(query)
            getAllProducts(allProducts)
            similarProductList?.removeAll(similarProductList!!)
            for(item in PRODUCT_CODES){
                if (selectedProduct!!.name.startsWith(item)){
                    for (product in allProducts!!){
                        if (product!!.name.startsWith(item))
                            similarProductList?.add(product)
                    }
                }
            }
            if(!(similarProductList.isNullOrEmpty())){
                shoppingListAdapter = ShoppingListAdapter(similarProductList!!,{position,cardView -> OnItemClick(position,cardView)},{position , cardView-> OnItemLongClick(position,cardView)})
                recyclerSimilarProducts.adapter = shoppingListAdapter
            }
        }
    }
    fun OnItemClick(position: Int,cardView: CardView){
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
    }

}