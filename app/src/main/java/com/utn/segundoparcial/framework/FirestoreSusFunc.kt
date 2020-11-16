package com.utn.segundoparcial.framework

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.Product
import com.utn.segundoparcial.entities.User
import kotlinx.coroutines.tasks.await

suspend fun getProductsByUser(sortingOrder: Int,currentUser: User?,favmenu:Boolean,shoppingList: MutableList<Product>?): Int {
    val db = Firebase.firestore
    val productsCollectionRef = db.collection("products")
    try {
        val data = productsCollectionRef
            .whereEqualTo("user",currentUser?.username)
            .whereEqualTo("favorite", favmenu)
            .get()
            .await()
        for(product in data){
            shoppingList?.add(product.toObject<Product>())
        }
    }
    catch (e:Exception){

    }
    when(sortingOrder){
        1 ->
            shoppingList!!.sortBy { it.id }
        2 ->
            shoppingList!!.sortBy { it.price }
        else -> ""
    }
    return 0
}

suspend fun getProductByBrand(brand:String, shoppingList: MutableList<Product>?):Int{
    val db = Firebase.firestore
    val productsCollectionRef = db.collection("products")
    try {
        val data = productsCollectionRef
            .whereEqualTo("user","debug")
            .whereEqualTo("brand",brand )
            .get()
            .await()
        for(product in data){
            shoppingList?.add(product.toObject<Product>())
        }
    }
    catch (e:Exception){

    }

    return 0
}

suspend fun getAllProducts(shoppingList: MutableList<Product>?):Int{
    val db = Firebase.firestore
    val productsCollectionRef = db.collection("products")
    try {
        val data = productsCollectionRef
            .whereEqualTo("user","debug")
            .get()
            .await()
        for(product in data){
            shoppingList?.add(product.toObject<Product>())
        }
    }
    catch (e:Exception){

    }

    return 0
}

suspend fun getProductByName(productName: String): MutableList<Product>{
    val db = Firebase.firestore
    val productsCollectionRef = db.collection("products")
    var shoppingList: MutableList<Product> = ArrayList<Product>()
    try {
        val data = productsCollectionRef
            .whereEqualTo("user","debug")
            .whereEqualTo("name", productName)
            .get()
            .await()
        for(product in data){
            shoppingList?.add(product.toObject<Product>())
        }
    }
    catch (e:Exception){

    }
    return shoppingList
}

suspend fun getProductByQuery(query: Query): Product?{
    var product: Product? = null
    try {
        val data = query
            .get()
            .await()
        product = data
            .elementAt(0)
            .toObject<Product>()
    }
    catch (e:Exception){

    }
    return product
}

suspend fun getProduct_add_edit(editProductPos: Int, newProductId: Int,currentUser: User?): Product? {
    val db = Firebase.firestore
    val productsCollectionRef = db.collection("products")
    var auxProduct: Product? = null

    if (editProductPos != -1) {                                                 //Si se quiere editar un producto, se carga el producto
        try {
            val data = productsCollectionRef
                .whereEqualTo("user", currentUser?.username)
                .get()
                .await()
            auxProduct = data.elementAt(editProductPos).toObject<Product>()
        } catch (e: Exception) {
        }
    }
    if (newProductId != -1)
        try {
            val data = productsCollectionRef
                .whereEqualTo("user", "debug")
                .whereEqualTo("id", newProductId)
                .get()
                .await()
            auxProduct = data.elementAt(0).toObject<Product>()
        } catch (e: Exception) {
        }
    return auxProduct
    }

suspend fun add_edit_Product(currentUser: User?,selectedProduct: Product,originalProductId: Int): Int{
    val db = Firebase.firestore
    val productsCollectionRef = db.collection("products")
    selectedProduct.shopping = true
    selectedProduct.user = currentUser!!.username
    try {
        val product = productsCollectionRef
            .whereEqualTo("user",currentUser?.username)
            .whereEqualTo("id", selectedProduct.id)
            .get()
            .await()
        val auxProduct = productsCollectionRef
            .whereEqualTo("user",currentUser?.username)
            .whereEqualTo("id",originalProductId)
            .get()
            .await()
        if (originalProductId != -1){
            auxProduct.elementAt(0).reference.set(selectedProduct)
        }
        else if ((product.isEmpty)){
            productsCollectionRef.add(selectedProduct)
        }
        else {
            selectedProduct.quantity = selectedProduct.quantity + product.elementAt(0).toObject<Product>().quantity
            product.elementAt(0).reference.set(selectedProduct)
        }
    }
    catch (e:Exception){

    }
    return 0
}

suspend fun deleteProduct(query: Query,selectedProducts: MutableList<Product>?): Int{

    try {
        val data = query
            .get()
            .await()
        for(product in data){
            if (selectedProducts!!.contains(product.toObject<Product>()))
                product.reference.delete()
        }
    }
    catch (e:Exception){

    }
    return 0
}

suspend fun updateProduct(currentUser: User?,selectedProduct: Product): Int{
    val db = Firebase.firestore
    val productsCollectionRef = db.collection("products")
    try {
        val data = productsCollectionRef
            .whereEqualTo("user",currentUser?.username)
            .whereEqualTo("id",selectedProduct.id)
            .get()
            .await()
        data.elementAt(0).reference.set(selectedProduct)
    }
    catch (e:Exception){

    }
    return 0
}

suspend fun loadProductsLists(productListName: MutableList<String>,productListBrand: MutableList<String>): Int {
    val db = Firebase.firestore
    val productsCollectionRef = db.collection("products")
    try {
        val data = productsCollectionRef
            .whereEqualTo("user","debug")
            .get()
            .await()
        for (product in data) {
            if (!(productListName.contains(product.toObject<Product>().name)))
                productListName.add(product.toObject<Product>().name)
            if (!(productListBrand.contains(product.toObject<Product>().brand)))
                productListBrand.add(product.toObject<Product>().brand)
        }
    }
    catch (e:Exception){

    }
    return 0
}

suspend fun getUserById(userid:Int): User? {
    val db = Firebase.firestore
    val usersCollectionRef = db.collection("users")
    var currentUser: User? = null

    try{
        val data = usersCollectionRef
            .whereEqualTo("id",userid)
            .get()
            .await()

        currentUser = data.elementAt(0).toObject<User>()
    }
    catch (e:Exception){

    }
    return currentUser
}

suspend fun setPrefs(currentUser: User?,context: Context):Int {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()
    val db = Firebase.firestore
    val usersCollectionRef = db.collection("users")

    lateinit var settingName: String
    lateinit var settingPassword: String

    settingName = prefs.getString("first_name", currentUser?.name)!!
    settingPassword = prefs.getString("password", currentUser?.password)!!
    if (settingName.isEmpty()) {
        editor.putString("first_name", currentUser?.name)
        settingName = currentUser!!.name
    }
    if (settingPassword.isEmpty()) {
        editor.putString("password", currentUser?.password)
        settingPassword = currentUser!!.password
    }
    editor.apply()
    if (currentUser?.name != settingName)
        currentUser?.name = settingName
    if (currentUser?.password != settingPassword)
        currentUser?.password = settingPassword
    try {
        val data = usersCollectionRef
            .whereEqualTo("id",currentUser?.id)
            .get()
            .await()
        data
            .elementAt(0)
            .reference
            .set(currentUser!!)
    }
    catch (e:Exception){

    }

    return prefs.getString("sort","0")!!.toInt()

}

