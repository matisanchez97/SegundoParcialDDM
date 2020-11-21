package com.utn.segundoparcial.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ShoppingListAdapter (private var shoppingList: MutableList<Product>, val onItemClick : (Int,CardView) -> Unit, val onItemLongClick : (Int,CardView) -> Unit) : RecyclerView.Adapter<ShoppingListAdapter.ShoppingListHolder>(){
    class ShoppingListHolder (v: View) : RecyclerView.ViewHolder(v){

        private var view: View

        init {
            this.view = v
        }

        fun setProduct(product_name:String, product_brand: String){
            val txt: TextView = view.findViewById(R.id.textAccItem)
            txt.text = product_name + " " + product_brand
        }

        fun setQuantity(quantity: String){
            val txt: TextView = view.findViewById(R.id.textQuant)
            txt.text = "x" + quantity
        }

        fun getCardLayout(): CardView {
            return view.findViewById(R.id.cardAccItem)
        }

        fun getImageView(): ImageView {
            return view.findViewById(R.id.imageAccItem)
        }
    }
    /*@GlideModule
    public class MyAppGlideModule: AppGlideModule(){
        override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
            super.registerComponents(context, glide, registry)
            registry.append(StorageReference.class, InputStream.class, FirebaseImageLoader.Factory())

        }
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.account_item,parent,false)
        return (ShoppingListHolder(view))
    }

    override fun onBindViewHolder(holder: ShoppingListHolder, position: Int) {


        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)
        scope.launch {
            loadHolder(holder,position)
        }

    }
    suspend fun loadHolder(holder: ShoppingListHolder, position: Int){
        val storage = Firebase.storage
        lateinit var imageRef: StorageReference

        holder.setProduct(shoppingList[position].name,shoppingList[position].brand)
        holder.getCardLayout().setOnClickListener {
            onItemClick(position,holder.getCardLayout())
        }
        holder.getCardLayout().setOnLongClickListener {
            onItemLongClick(position,holder.getCardLayout())
            true
        }
        holder.setQuantity(shoppingList[position].quantity.toString())
        imageRef = storage.getReferenceFromUrl(shoppingList[position].downloadUri)
        try {
            Glide.with(holder.getImageView().context)
                .load(Uri.parse(shoppingList[position].downloadUri))
                .into(holder.getImageView())
        }
        catch (e:Exception){
            e.cause
        }
    }

    override fun getItemCount(): Int {
        return shoppingList.size
    }
}