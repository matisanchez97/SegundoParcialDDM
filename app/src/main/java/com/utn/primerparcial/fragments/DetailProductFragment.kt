package com.utn.primerparcial.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.CheckBox
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
import com.utn.primerparcial.entities.User

/**
 * A simple [Fragment] subclass.
 * Use the [DetailProductFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailProductFragment() : Fragment() {

    lateinit var v: View
    lateinit var textPrdDesc: TextView
    lateinit var butAdd: Button
    lateinit var butFav: CheckBox

    var productId: Int = 0
    var currentUserId: Int = 0
    var selectedProduct: Product? = null
    var currentUser: User? = null
    var productDes: String? = ""

    private val PREF_NAME = "myPreferences"
    private var editor: SharedPreferences.Editor? = null

    private var db: appDatabase? = null
    private var productDao: productDao? = null
    private var userDao: userDao? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_detail_product, container, false)
        textPrdDesc = v.findViewById(R.id.textProductDesc)
        butAdd = v.findViewById(R.id.buttonAccept2)
        butFav = v.findViewById(R.id.buttonFav)
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
        userDao = db?.userDao()
        currentUser = userDao?.loadPersonById(currentUserId)
        selectedProduct = productDao?.loadProductById(productId)
        productDes = selectedProduct?.name + " de la marca " + selectedProduct?.brand + "\nMedida: " + selectedProduct?.measure + "\nPrecio: $" + selectedProduct?.price.toString()
        textPrdDesc.text = productDes
        if(currentUser?.favorite_products!!.contains(selectedProduct))
            butFav.setChecked(true)
        else
            butFav.setChecked(false)


        butAdd.setOnClickListener {
            val action = ContainerProductFragmentDirections.actionContainerProductFragmentToAddDialogFragment(currentUserId,-1,productId)
            findNavController().navigate(action)
        }

        butFav.setOnClickListener {
            if (butFav.isChecked){
                currentUser?.favorite_products?.add(selectedProduct!!)
                userDao?.updatePerson(currentUser)
            }else{
                currentUser?.favorite_products?.remove(selectedProduct!!)
                userDao?.updatePerson(currentUser)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        onStart()
    }





}