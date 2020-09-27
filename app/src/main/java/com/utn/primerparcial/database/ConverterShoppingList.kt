package com.utn.primerparcial.database

import androidx.room.TypeConverter
import com.utn.primerparcial.constants.PRODUCTS_LIST
import com.utn.primerparcial.entities.Product
import java.time.LocalDate

class ConverterShoppingList {
    companion object{
        @TypeConverter
        @JvmStatic
        fun fromShoppingList(value: MutableList<Product>):String {
            var acc: String = ""
            if(value.isNotEmpty()) {
                for (product in value) {
                    acc += product.id.toString() + "-" + product.name + "-" + product.brand + "-" + product.price.toString() + "-" + product.quantity.toString() + "-" + product.measure + "-" + product.imageResId.toString() + "-/"
                }
            }
            return acc
        }

        @TypeConverter
        @JvmStatic
        fun toShoppingList(value: String): MutableList<Product> {
            var productArgs:Array<String?> = arrayOfNulls<String>(7)
            var ShoppingList: MutableList<Product> = ArrayList<Product>()
            var aux1: String
            var aux2: String
            var aux3: String
            var i = 0
            aux1 = value
            aux2 = aux1.substringBefore("/")
            while (aux2 != aux1)
            {
                aux1 = aux1.removePrefix(aux2 + "/")
                aux3 = aux2.substringBefore("-")
                while (aux3 != aux2){
                    productArgs[i] = aux3
                    aux2 = aux2.removePrefix(aux3 + "-")
                    aux3 = aux2.substringBefore("-")
                    i++
                }
                i = 0
                ShoppingList.add(Product(productArgs[0]!!.toInt(),productArgs[1]!!,productArgs[2]!!,productArgs[3]!!.toInt(),productArgs[4]!!.toInt(),productArgs[5]!!,productArgs[6]!!.toInt()))
                aux2 = aux1.substringBefore("/")
            }
            return ShoppingList
        }
    }
}