package com.utn.primerparcial.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.appcompat.view.ActionMode
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
class DetailProductFragment(productId:Int) : Fragment() {

    lateinit var v: View
    lateinit var textPrdDesc: TextView
    var selectedProduct: Product? = null
    var productId = productId
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
        return v
    }

    override fun onStart() {
        super.onStart()
        db = appDatabase.getAppDataBase(v.context)
        productDao = db?.productDao()
        selectedProduct = productDao?.loadProductById(productId)

        productDes = selectedProduct?.name + " de la marca " + selectedProduct?.brand + "\nMedida: " + selectedProduct?.measure + "\nPrecio: $" + selectedProduct?.price.toString()
        textPrdDesc.text = productDes
    }



}