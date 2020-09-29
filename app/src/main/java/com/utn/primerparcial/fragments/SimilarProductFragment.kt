package com.utn.primerparcial.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.utn.primerparcial.MainActivity
import com.utn.primerparcial.R
import com.utn.primerparcial.adapters.ShoppingListAdapter
import com.utn.primerparcial.constants.PRODUCT_CODES
import com.utn.primerparcial.database.appDatabase
import com.utn.primerparcial.database.productDao
import com.utn.primerparcial.database.userDao
import com.utn.primerparcial.entities.Product


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

    private var db: appDatabase? = null
    private var productDao: productDao? = null

    var productId: Int = 0
    private val PREF_NAME = "myPreferences"
    private var editor: SharedPreferences.Editor? = null
    var selectedProduct: Product? = null
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
        editor = sharedPref.edit()
        productId = sharedPref.getInt("SELECTED_PRODUCT_ID",-1)
        db = appDatabase.getAppDataBase(v.context)
        productDao = db?.productDao()
        selectedProduct = productDao?.loadProductById(productId)
        for(item in PRODUCT_CODES){
            if(selectedProduct!!.name.startsWith(item))
                similarProductList = productDao?.loadSimilarProducts(item)
        }
        recyclerSimilarProducts.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerSimilarProducts.layoutManager = linearLayoutManager
        if(!(similarProductList.isNullOrEmpty())){
            shoppingListAdapter = ShoppingListAdapter(similarProductList!!,{position,cardView -> OnItemClick(position,cardView)},{position , cardView-> OnItemLongClick(position,cardView)})
            recyclerSimilarProducts.adapter = shoppingListAdapter
        }
    }
    fun OnItemClick(position: Int,cardView: CardView){
        selectedProduct = similarProductList!![position]
        editor?.putInt("SELECTED_PRODUCT_ID",selectedProduct!!.id)
        editor?.apply()
        val tabLayout = (activity as MainActivity).findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.getTabAt(0)?.select()
    }

    fun OnItemLongClick(position: Int,cardView: CardView){

    }

    override fun onResume() {
        super.onResume()
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        productId = sharedPref.getInt("SELECTED_PRODUCT_ID",-1)
        selectedProduct = productDao?.loadProductById(productId)
        similarProductList?.removeAll(similarProductList!!)
        for(item in PRODUCT_CODES){
            if(selectedProduct!!.name.startsWith(item))
                similarProductList?.addAll(productDao?.loadSimilarProducts(item)!!)
        }
        recyclerSimilarProducts.adapter?.notifyDataSetChanged()
    }

}