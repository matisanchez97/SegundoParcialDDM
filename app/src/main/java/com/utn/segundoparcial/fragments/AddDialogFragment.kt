package com.utn.segundoparcial.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.core.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.firestore.model.Document
import com.google.firebase.ktx.Firebase
import com.utn.segundoparcial.MainActivity
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.Product
import com.utn.segundoparcial.entities.User
import com.utn.segundoparcial.framework.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select


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
    lateinit var auxQuery: com.google.firebase.firestore.Query

    var productListName: MutableList<String> = ArrayList<String>()
    var productListBrand: MutableList<String> = ArrayList<String>()
    var productListAux: MutableList<Product> = ArrayList<Product>()
    var isValid = true
    var measureAux: String = ""
    var selectedProduct: Product? = null
    var currentUser: User? = null
    var currentUserId = 0
    var newProductId = 0
    var editProductPos = 0
    var originalProductId = -1
    var auxProduct: Product? = null
    var stringAux = ""

    val db = Firebase.firestore
    val productsCollectionRef = db.collection("products")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_add_dialog, container, false)
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
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.title_7)

        /*scope.launch {
            currentUser = getUserById(currentUserId)
            loadProductsLists(productListName,productListBrand)
            val adapterName = ArrayAdapter<String>(requireContext(), R.layout.area_item, productListName)
            textProductNameList.setAdapter(adapterName)
            auxProduct = getProduct_add_edit(editProductPos,newProductId,currentUser)
            if(auxProduct != null){
                originalProductId = auxProduct!!.id
                textProductNameList.setText(auxProduct!!.name, false)
                textProductBrandList.setText(auxProduct!!.brand, false)
                textEditProductQuantity.setText(auxProduct!!.quantity.toString())
            }
        }*/

        /*textProductNameList.setOnItemClickListener { parent, view, position, id ->              //Cuano se aprieta en un producto, qiuero que solo aparezcan las marcas de ese producto
            scope.launch {
                productListBrand.removeAll(productListBrand)                                        //Primero borro la lista de marcas
                textProductBrandList.text.clear()
                productListAux = getProductByName(productListName[position])
                for (product in productListAux) {      //Para inicio un loop for para todos los productos del mismo nombre
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

        }*/

        /*butAccept.setOnClickListener {
            val validationList =
                arrayListOf<TextInputLayout>(textFieldName, textFieldBrand, textFieldQuantity)
            productListAux.removeAll(productListAux)

            for (textField in validationList) {               //Creo una lista text inputlayout, para verificar que esten todas completas
                if (textField.editText!!.text.isBlank()) {   //Si no lo estan envio un mensaje de error
                    isValid = false
                    textField.error = getString(R.string.error_msg)
                }
            }
            if (isValid) {
                auxQuery = productsCollectionRef
                    .whereEqualTo("name",textProductNameList.text.toString())
                    .whereEqualTo("user", "debug")
                if ((!textProductBrandList.text.contains(" x"))) {          //Si el producto no tiene varias medidas, lo cargo por nombre y marca
                    auxQuery = auxQuery
                        .whereEqualTo("brand",textProductBrandList.text.toString())
                } else {
                    stringAux = textProductBrandList.text.toString()
                        .substringBefore(" x")      //Si el producto tiene varias medidas, separo la medida y la marca
                    measureAux = textProductBrandList.text.toString()
                        .substringAfter(" x")      //Y lo cargo por nombre, marca y medida
                    auxQuery = auxQuery
                        .whereEqualTo("brand", stringAux)
                        .whereEqualTo("measure", measureAux)
                }
                scope.launch {
                    selectedProduct = getProductByQuery(auxQuery)
                    getProductsByUser(0,currentUser,false,productListAux)
                    selectedProduct?.quantity = textEditProductQuantity.text.toString().toInt()
                    add_edit_Product(currentUser,selectedProduct!!,originalProductId)
                    val action_6 = AddDialogFragmentDirections.actionAddDialogFragmentToShoppinglistFragment(currentUserId)
                    findNavController().navigate(action_6)
                }
           } else
                isValid = true
        }*/

        /*butCancel.setOnClickListener {
            val action_7 =
                AddDialogFragmentDirections.actionAddDialogFragmentToShoppinglistFragment(
                    currentUserId
                )
            findNavController().navigate(action_7)
        }*/

        /*butAdd.setOnClickListener {
            var aux = 0
            aux = textEditProductQuantity.text.toString().toInt() + 1
            textEditProductQuantity.setText(aux.toString())
        }*/

        /*butSust.setOnClickListener {
            var aux = 0
            aux = textEditProductQuantity.text.toString().toInt() - 1
            if (aux > 0)
                textEditProductQuantity.setText(aux.toString())
        }*/
    }

}