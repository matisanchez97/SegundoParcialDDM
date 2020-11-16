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
import com.utn.segundoparcial.entities.Product
import com.utn.segundoparcial.framework.getProductByBrand
import com.utn.segundoparcial.framework.getProductByQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [BrandProductFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BrandProductFragment() : Fragment() {

    lateinit var v: View
    lateinit var recyclerBrandProducts: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var shoppingListAdapter: ShoppingListAdapter

    val db = Firebase.firestore
    val usersCollectionRef = db.collection("users")
    val productsCollectionRef = db.collection("products")

    var productId: Int = 0
    private val PREF_NAME = "myPreferences"
    private var editor: SharedPreferences.Editor? = null
    var selectedProduct: Product? = null
    var brandProductList: MutableList<Product>? = ArrayList<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_brand_product, container, false)
        recyclerBrandProducts = v.findViewById(R.id.recyclerBrandProducts)
        return v
    }

    override fun onStart() {
        super.onStart()
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)
        editor = sharedPref.edit()
        productId = sharedPref.getInt("SELECTED_PRODUCT_ID",-1)
        recyclerBrandProducts.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerBrandProducts.layoutManager = linearLayoutManager

        scope.launch {
            val query = productsCollectionRef
                .whereEqualTo("user","debug")
                .whereEqualTo("id",productId)
            recyclerBrandProducts.removeAllViews()
            selectedProduct = getProductByQuery(query)
            brandProductList?.removeAll(brandProductList!!)
            getProductByBrand(selectedProduct!!.brand,brandProductList)
            if(!(brandProductList.isNullOrEmpty())){
                shoppingListAdapter = ShoppingListAdapter(brandProductList!!,{position,cardView -> OnItemClick(position,cardView)},{position, cardView -> OnItemLongClick(position,cardView)})
                recyclerBrandProducts.adapter = shoppingListAdapter
            }
        }
    }
    fun OnItemClick(position: Int,cardView: CardView){
        selectedProduct = brandProductList!![position]
        editor?.putInt("SELECTED_PRODUCT_ID",selectedProduct!!.id)
        editor?.apply()
        val tabLayout = (activity as MainActivity).findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.getTabAt(0)?.select()

    }
    fun OnItemLongClick(position: Int, cardView: CardView){

    }

    override fun onResume() {
        super.onResume()
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)
        productId = sharedPref.getInt("SELECTED_PRODUCT_ID",-1)

        scope.launch {
            val query = productsCollectionRef
                .whereEqualTo("user","debug")
                .whereEqualTo("id",productId)
            recyclerBrandProducts.removeAllViews()
            selectedProduct = getProductByQuery(query)
            brandProductList?.removeAll(brandProductList!!)
            getProductByBrand(selectedProduct!!.brand,brandProductList)
            recyclerBrandProducts.adapter?.notifyDataSetChanged()
        }
    }




}