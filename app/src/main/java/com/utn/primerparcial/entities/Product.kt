package com.utn.primerparcial.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "products")
class Product(id:Int, name: String,brand: String, price:Int, quantity:Int,measure:String) {
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Int
    @ColumnInfo(name = "name")
    var name: String
    @ColumnInfo(name = "brand")
    var brand: String
    @ColumnInfo(name = "price")
    var price: Int
    @ColumnInfo(name = "quantity")
    var quantity: Int
    @ColumnInfo(name = "measure")
    var measure: String



    init{
        this.id = id
        this.name = name
        this.brand = brand
        this.price = price
        this.quantity = quantity
        this.measure = measure
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

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + brand.hashCode()
        result = 31 * result + price
        result = 31 * result + quantity
        result = 31 * result + measure.hashCode()
        return result
    }


}