package com.utn.segundoparcial.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.Product

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.account_item,parent,false)
        return (ShoppingListHolder(view))
    }

    override fun onBindViewHolder(holder: ShoppingListHolder, position: Int) {
        holder.setProduct(shoppingList[position].name,shoppingList[position].brand)
        holder.getCardLayout().setOnClickListener {
            onItemClick(position,holder.getCardLayout())
        }
        holder.getCardLayout().setOnLongClickListener {
            onItemLongClick(position,holder.getCardLayout())
            true
        }
        holder.setQuantity(shoppingList[position].quantity.toString())
        holder.getImageView().setImageResource(shoppingList[position].imageResId)
        /*if(shoppingList[position].name.startsWith(PRODUCT_CODES[0]))
            holder.getImageView().setImageResource(R.drawable.ic_milk_bottle)
        else if (shoppingList[position].name.startsWith(PRODUCT_CODES[1]))
            holder.getImageView().setImageResource(R.drawable.ic_cheese)
        else if (shoppingList[position].name.startsWith(PRODUCT_CODES[2]))
            holder.getImageView().setImageResource(R.drawable.ic_jam)
        else if (shoppingList[position].name.startsWith(PRODUCT_CODES[3]))
            holder.getImageView().setImageResource(R.drawable.ic_beer)
        else if (shoppingList[position].name.startsWith(PRODUCT_CODES[4]))
            holder.getImageView().setImageResource(R.drawable.ic_dulce_de_leche)*/


    }

    override fun getItemCount(): Int {
        return shoppingList.size
    }
}