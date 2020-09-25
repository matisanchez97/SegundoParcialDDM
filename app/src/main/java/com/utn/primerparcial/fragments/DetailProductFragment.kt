package com.utn.primerparcial.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.appcompat.view.ActionMode
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.utn.primerparcial.R
import com.utn.primerparcial.database.appDatabase
import com.utn.primerparcial.database.productDao
import com.utn.primerparcial.database.userDao
import com.utn.primerparcial.entities.Product

/**
 * A simple [Fragment] subclass.
 * Use the [DetailProductFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailProductFragment() : Fragment() {

    lateinit var v: View
    lateinit var textPrdDesc: TextView
    lateinit var butAdd: Button
    var productId: Int = 0
    var currentUserId: Int = 0
    var selectedProduct: Product? = null
    private val PREF_NAME = "myPreferences"
    private var editor: SharedPreferences.Editor? = null
    var productDes: String? = ""
    private var db: appDatabase? = null
    private var productDao: productDao? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_detail_product, container, false)
        textPrdDesc = v.findViewById(R.id.textProductDesc)
        butAdd = v.findViewById(R.id.buttonAccept2)
        return v
    }

    override fun onStart() {
        super.onStart()
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = sharedPref.edit()
        currentUserId = sharedPref.getInt("CURRENT_USER_ID",-1)
        productId = sharedPref.getInt("SELECTED_PRODUCT_ID",-1)
        db = appDatabase.getAppDataBase(v.context)
        productDao = db?.productDao()
        selectedProduct = productDao?.loadProductById(productId)
        productDes = selectedProduct?.name + " de la marca " + selectedProduct?.brand + "\nMedida: " + selectedProduct?.measure + "\nPrecio: $" + selectedProduct?.price.toString()
        textPrdDesc.text = productDes

        butAdd.setOnClickListener {
            val action = ContainerProductFragmentDirections.actionContainerProductFragmentToAddDialogFragment(currentUserId,-1,productId)
            findNavController().navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()
        onStart()
    }





}