package com.utn.segundoparcial.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.Product
import com.utn.segundoparcial.entities.User
import com.utn.segundoparcial.framework.getProductByQuery
import com.utn.segundoparcial.framework.getUserById
import com.utn.segundoparcial.framework.updateProduct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
    lateinit var imageProduct: ImageView
    lateinit var favoritequery:Query


    var productId: Int = 0
    var currentUserId: Int = 0
    var selectedProduct: Product? = null
    var currentUser: User? = null
    var productDes: String? = ""

    private val PREF_NAME = "myPreferences"
    private var editor: SharedPreferences.Editor? = null

    val db = Firebase.firestore
    val productsCollectionRef = db.collection("products")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_detail_product, container, false)
        textPrdDesc = v.findViewById(R.id.textProductDesc)
        butAdd = v.findViewById(R.id.buttonAccept2)
        butFav = v.findViewById(R.id.buttonFav)
        imageProduct = v.findViewById(R.id.imageProduct)
        return v
    }

    override fun onStart() {
        super.onStart()
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)
        editor = sharedPref.edit()
        currentUserId = sharedPref.getInt("CURRENT_USER_ID",-1)
        productId = sharedPref.getInt("SELECTED_PRODUCT_ID",-1)

        scope.launch {
            currentUser = getUserById(currentUserId.toString())
            val query = productsCollectionRef
                .whereEqualTo("user",currentUser?.username)
                .whereEqualTo("id", productId)
            selectedProduct = getProductByQuery(query)
            productDes = selectedProduct?.name + " de la marca " + selectedProduct?.brand + "\nMedida: " + selectedProduct?.measure + "\nPrecio: $" + selectedProduct?.price.toString()
            textPrdDesc.text = productDes
            imageProduct.setImageResource(selectedProduct!!.imageResId)
            if(selectedProduct!!.favorite)
                butFav.setChecked(true)
            else
                butFav.setChecked(false)
        }

        butAdd.setOnClickListener {
            val action = ContainerProductFragmentDirections.actionContainerProductFragmentToAddDialogFragment(currentUserId,-1,productId)
            findNavController().navigate(action)
        }

        butFav.setOnClickListener {
            scope.launch {
                val query = productsCollectionRef
                    .whereEqualTo("user", currentUser?.username)
                    .whereEqualTo("id", selectedProduct?.id)
                selectedProduct = getProductByQuery(query)
                selectedProduct?.favorite = !(selectedProduct!!.favorite)
                updateProduct(currentUser, selectedProduct!!)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)
        productId = sharedPref.getInt("SELECTED_PRODUCT_ID",-1)

        scope.launch {
            currentUser = getUserById(currentUserId.toString())
            val query = productsCollectionRef
                .whereEqualTo("user",currentUser?.username)
                .whereEqualTo("id", productId)
            selectedProduct = getProductByQuery(query)
            productDes = selectedProduct?.name + " de la marca " + selectedProduct?.brand + "\nMedida: " + selectedProduct?.measure + "\nPrecio: $" + selectedProduct?.price.toString()
            textPrdDesc.text = productDes
            imageProduct.setImageResource(selectedProduct!!.imageResId)
            if(selectedProduct!!.favorite)
                butFav.setChecked(true)
            else
                butFav.setChecked(false)
        }


    }





}