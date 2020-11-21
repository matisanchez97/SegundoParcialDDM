package com.utn.segundoparcial.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

class Product(id:Int, name: String,brand: String, price:Int, quantity:Int,measure:String,downloadUri: String, user:String) {
    var id: Int
    var name: String
    var brand: String
    var price: Int
    var quantity: Int
    var measure: String
    var downloadUri: String
    var user:String
    var favorite:Boolean
    var shopping:Boolean

    constructor() : this(0,"","",0,0,"","","")
    constructor(id: Int,name: String,brand: String,price: Int,quantity: Int,measure: String)
            :this(id,name,brand,price,quantity,measure,"", "debug")
    init{
        this.id = id
        this.name = name
        this.brand = brand
        this.price = price
        this.quantity = quantity
        this.measure = measure
        this.downloadUri = downloadUri
        this.user = user
        this.favorite = false
        this.shopping = false
    }

    fun addDownloadUri(downloadUri: Uri){
        this.downloadUri = downloadUri.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (id != other.id) return false
        if (name != other.name) return false
        if (brand != other.brand) return false
        if (price != other.price) return false
        if (quantity != other.quantity) return false
        if (measure != other.measure) return false
        if (user != other.user) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + brand.hashCode()
        result = 31 * result + price
        result = 31 * result + quantity
        result = 31 * result + measure.hashCode()
        result = 31 * result + user.hashCode()
        return result
    }


}