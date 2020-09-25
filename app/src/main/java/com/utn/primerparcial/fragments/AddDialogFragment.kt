package com.utn.primerparcial.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.utn.primerparcial.MainActivity
import com.utn.primerparcial.R
import com.utn.primerparcial.constants.AREA_CODES
import com.utn.primerparcial.constants.PRODUCTS_LIST
import com.utn.primerparcial.database.appDatabase
import com.utn.primerparcial.database.productDao
import com.utn.primerparcial.database.userDao
import com.utn.primerparcial.entities.Product
import com.utn.primerparcial.entities.User
import java.net.UnknownServiceException


/**
 * A simple [Fragment] subclass.
 * Use the [AddDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddDialogFragment() : Fragment() {

    lateinit var v: View
    lateinit var textEditProductQuantity: AutoCompleteTextView
    lateinit var textProductNameList: AutoCompleteTextView
    lateinit var textProductBrandList: AutoCompleteTextView
    lateinit var butAccept: Button
    lateinit var butCancel: Button
    lateinit var butAdd: Button
    lateinit var butSust: Button
    var productListName: MutableList<String> = ArrayList<String>()
    var productListBrand: MutableList<String> = ArrayList<String>()
    var productListAux: MutableList<String> = ArrayList<String>()
    var measureAux: String = ""
    var selectedProduct: Product? = null
    var currentUser: User? = null
    var currentUserId = 0
    var newProductId = 0
    var newProduct: Product? = null
    var editProductPos = 0
    var editProduct: Product? = null
    private var db: appDatabase? = null
    private var productDao: productDao? = null
    private var userDao: userDao? = null
    var stringAux = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_add_dialog, container, false)
        textEditProductQuantity = v.findViewById(R.id.textEditQuantity)
        textProductBrandList = v.findViewById(R.id.textEditProductBrand)
        textProductNameList = v.findViewById(R.id.textEditProductName)
        butAdd = v.findViewById(R.id.buttonAdd)
        butSust = v.findViewById(R.id.buttonSust)
        butAccept = v.findViewById(R.id.buttonAccept)
        butCancel = v.findViewById(R.id.buttonCancel)
        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        currentUserId = AddDialogFragmentArgs.fromBundle(requireArguments()).currentUserId
        editProductPos = AddDialogFragmentArgs.fromBundle(requireArguments()).editProductId
        newProductId = AddDialogFragmentArgs.fromBundle(requireArguments()).newProductId
        db = appDatabase.getAppDataBase(v.context)
        productDao = db?.productDao()
        userDao = db?.userDao()
        currentUser = userDao?.loadPersonById(currentUserId)

        (activity as MainActivity).supportActionBar?.title = getString(R.string.title_7)

        for (product in productDao?.loadAllProducts()!!) {
            if (!(productListName.contains(product.name)))
                productListName.add(product.name)
            if (!(productListBrand.contains(product.brand)))
                productListBrand.add(product.brand)
        }
        val adapterName = ArrayAdapter<String>(requireContext(), R.layout.area_item, productListName)
        val adapterBrand = ArrayAdapter<String>(requireContext(), R.layout.area_item, productListBrand)
        textProductNameList.setAdapter(adapterName)
        textProductBrandList.setAdapter(adapterBrand)

        if (editProductPos != -1) {
            butAccept.text = getString(R.string.edit)
            editProduct = currentUser!!.shopping_list[editProductPos]
            textProductNameList.setText(editProduct!!.name, false)
            textProductBrandList.setText(editProduct!!.brand, false)
            textEditProductQuantity.setText(editProduct!!.quantity.toString())
        }

        if (newProductId != -1){
            newProduct = productDao?.loadProductById(newProductId)
            textProductNameList.setText(newProduct!!.name,false)
            textProductBrandList.setText(newProduct!!.brand, false)
        }

        textProductNameList.setOnItemClickListener { parent, view, position, id ->
            productListAux.removeAll(productListAux)
            for (product in productDao?.loadProductsByName(productListName[position])!!) {
                if (!(productListAux.contains(product.brand))) {
                    productListAux.add(product.brand)
                    measureAux = product.measure
                } else {
                    productListAux[productListAux.size - 1] += " x" + measureAux
                    productListAux.add(product.brand + " x" + product.measure)
                }
            }
            val adapterAux = ArrayAdapter<String>(requireContext(), R.layout.area_item, productListAux)
            textProductBrandList.setAdapter(adapterAux)
        }

        butAccept.setOnClickListener {
            if ((!textProductBrandList.text.contains(" x"))) {
                selectedProduct = productDao?.loadProductByNameAndBrand(
                    textProductNameList.text.toString(),
                    textProductBrandList.text.toString()
                )
                selectedProduct?.quantity = textEditProductQuantity.text.toString().toInt()
                if (editProductPos != -1) {
                    currentUser?.shopping_list?.removeAt(editProductPos)
                }
                    if (currentUser?.shopping_list?.any { it.name == selectedProduct?.name && it.brand == selectedProduct?.brand }!!) {
                        val index = currentUser?.shopping_list?.indexOfFirst { it.name == selectedProduct?.name && it.brand == selectedProduct?.brand }
                        currentUser!!.shopping_list[index!!].quantity += selectedProduct!!.quantity
                    } else if (editProductPos != -1)
                        currentUser?.shopping_list?.add(editProductPos,selectedProduct!!)
                    else
                        currentUser?.shopping_list?.add(selectedProduct!!)
                userDao?.updatePerson(currentUser)
            } else {
                stringAux = textProductBrandList.text.toString().substringBefore(" x")
                measureAux = textProductBrandList.text.toString().substringAfter(" x")
                selectedProduct = productDao?.loadProductByNameAndBrandAndMeasure(
                    textProductNameList.text.toString(),
                    stringAux,
                    measureAux
                )
                selectedProduct?.quantity = textEditProductQuantity.text.toString().toInt()
                currentUser?.shopping_list?.add(selectedProduct!!)
                userDao?.updatePerson(currentUser)
            }
            val action_6 =
                AddDialogFragmentDirections.actionAddDialogFragmentToShoppinglistFragment(
                    currentUserId
                )
            findNavController().navigate(action_6)
        }

        butCancel.setOnClickListener {
            val action_7 =
                AddDialogFragmentDirections.actionAddDialogFragmentToShoppinglistFragment(
                    currentUserId
                )
            findNavController().navigate(action_7)
        }

        butAdd.setOnClickListener {
            var aux = 0
            aux = textEditProductQuantity.text.toString().toInt() + 1
            textEditProductQuantity.setText(aux.toString())
        }

        butSust.setOnClickListener {
            var aux = 0
            aux = textEditProductQuantity.text.toString().toInt() - 1
            if (aux >= 0)
                textEditProductQuantity.setText(aux.toString())
        }
    }
}