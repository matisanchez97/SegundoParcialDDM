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
    lateinit var textFieldQuantity: TextInputLayout
    lateinit var textFieldName: TextInputLayout
    lateinit var textFieldBrand: TextInputLayout
    lateinit var butAccept: Button
    lateinit var butCancel: Button
    lateinit var butAdd: Button
    lateinit var butSust: Button

    var productListName: MutableList<String> = ArrayList<String>()
    var productListBrand: MutableList<String> = ArrayList<String>()
    var productListAux: MutableList<String> = ArrayList<String>()
    var isValid = true
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
        textFieldQuantity = v.findViewById(R.id.textFieldQuantity)
        textFieldBrand = v.findViewById(R.id.textProductBrand)
        textFieldName = v.findViewById(R.id.textProductName)
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
        currentUserId = AddDialogFragmentArgs.fromBundle(requireArguments()).currentUserId      //Si editProductId = -1 y newProduct = -1, el usuario quiere agregar cualquier prod
        editProductPos = AddDialogFragmentArgs.fromBundle(requireArguments()).editProductId     //Si editProductId != -1 el usuario quiere editar un producto
        newProductId = AddDialogFragmentArgs.fromBundle(requireArguments()).newProductId        //Si newProductId != -1 el usuario quiere agregar un producto especifico
        db = appDatabase.getAppDataBase(v.context)
        productDao = db?.productDao()
        userDao = db?.userDao()
        currentUser = userDao?.loadPersonById(currentUserId)

        if (editProductPos != -1)
            (activity as MainActivity).supportActionBar?.title = getString(R.string.title_8)
        else
            (activity as MainActivity).supportActionBar?.title = getString(R.string.title_7)


        for (product in productDao?.loadAllProducts()!!) {
            if (!(productListName.contains(product.name)))
                productListName.add(product.name)
            if (!(productListBrand.contains(product.brand)))
                productListBrand.add(product.brand)
        }
        val adapterName = ArrayAdapter<String>(requireContext(), R.layout.area_item, productListName)
        textProductNameList.setAdapter(adapterName)

        if (editProductPos != -1) {                                                 //Si se quiere editar un producto, se carga el producto
            butAccept.text = getString(R.string.edit)                                                 //a editar en las views
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

        textProductNameList.setOnItemClickListener { parent, view, position, id ->              //Cuano se aprieta en un producto, qiuero que solo aparezcan las marcas de ese producto
            productListBrand.removeAll(productListBrand)                                        //Primero borro la lista de marcas
            textProductBrandList.text.clear()
            for (product in productDao?.loadProductsByName(productListName[position])!!) {      //Para inicio un loop for para todos los productos del mismo nombre
                if (!(productListBrand.contains(product.brand))) {                              //Como habia productos del mismo nombre y marca, pero distinta medida, tengo que diferenciarlos
                    productListBrand.add(product.brand)                                         //Si tengo uno de estos casos, le cambio el nombre en la lista para distingirlos
                    measureAux = product.measure
                } else {
                    productListBrand[productListBrand.size - 1] += " x" + measureAux
                    productListBrand.add(product.brand + " x" + product.measure)
                }
            }
            val adapterBrand = ArrayAdapter<String>(requireContext(), R.layout.area_item, productListBrand)
            textProductBrandList.setAdapter(adapterBrand)
        }

        butAccept.setOnClickListener {
            val validationList =
                arrayListOf<TextInputLayout>(textFieldName, textFieldBrand, textFieldQuantity)

            for (textField in validationList) {               //Creo una lista text inputlayout, para verificar que esten todas completas
                if (textField.editText!!.text.isBlank()) {   //Si no lo estan envio un mensaje de error
                    isValid = false
                    textField.error = getString(R.string.error_msg)
                }
            }
            if (isValid) {
                if ((!textProductBrandList.text.contains(" x"))) {          //Si el producto no tiene varias medidas, lo cargo por nombre y marca
                    selectedProduct = productDao?.loadProductByNameAndBrand(
                        textProductNameList.text.toString(),
                        textProductBrandList.text.toString()
                    )
                } else {
                    stringAux = textProductBrandList.text.toString()
                        .substringBefore(" x")      //Si el producto tiene varias medidas, separo la medida y la marca
                    measureAux = textProductBrandList.text.toString()
                        .substringAfter(" x")      //Y lo cargo por nombre, marca y medida
                    selectedProduct = productDao?.loadProductByNameAndBrandAndMeasure(
                        textProductNameList.text.toString(),
                        stringAux,
                        measureAux
                    )
                }
                selectedProduct?.quantity = textEditProductQuantity.text.toString()     //Le cargo la cantidad que ingrese
                    .toInt()
                if (editProductPos != -1) {
                    currentUser?.shopping_list?.removeAt(editProductPos)                                //Si es un producto que edite, primero lo saco de la lista
                }
                if (currentUser?.shopping_list?.any { it.name == selectedProduct?.name && it.brand == selectedProduct?.brand && it.measure == selectedProduct?.measure }!!) {                  //Chequeo si el producto ya existe en la shopping list
                    val index =
                        currentUser?.shopping_list?.indexOfFirst { it.name == selectedProduct?.name && it.brand == selectedProduct?.brand && it.measure == selectedProduct?.measure } //Tomo el indice en donde esta el producot
                    currentUser!!.shopping_list[index!!].quantity += selectedProduct!!.quantity                                                                                              //Le sumo la cantidad que ingrese a la que ya tenia
                } else if (editProductPos != -1)                                        //Si el producto no estaba en la lista y estoy editando, lo vuelvo a agregar en el lugar que estaba
                    currentUser?.shopping_list?.add(editProductPos, selectedProduct!!)
                else
                    currentUser?.shopping_list?.add(selectedProduct!!)                 //Si el producto no estaba en la lista y estoy agregando, solo lo agrego a la lista
                userDao?.updatePerson(currentUser)
                val action_6 = AddDialogFragmentDirections.actionAddDialogFragmentToShoppinglistFragment(currentUserId)
                findNavController().navigate(action_6)
           } else
                isValid = true
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
            if (aux > 0)
                textEditProductQuantity.setText(aux.toString())
        }
    }
}