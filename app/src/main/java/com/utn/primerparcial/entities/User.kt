package com.utn.primerparcial.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.utn.primerparcial.constants.PRODUCTS_LIST
import java.time.LocalDate

@Entity(tableName = "users")
class User(id: Int,name: String, phone_number: String, birthday: LocalDate, username: String, password: String) :
    Parcelable {

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Int

    @ColumnInfo(name = "name")
    var name: String

    @ColumnInfo(name = "phone_number")
    var phone_number: String

    @ColumnInfo(name = "birthday")
    var birthday: LocalDate

    @ColumnInfo(name = "username")
    var username: String

    @ColumnInfo(name = "password")
    var password: String

    @ColumnInfo(name = "products")
    var shopping_list:MutableList<Product>

    @ColumnInfo(name = "favorite")
    var favorite_products:MutableList<Product>

    @Ignore
    constructor(parcel: Parcel) : this(
        TODO("id"),
        TODO("name"),
        TODO("phone_number"),
        TODO("birthday"),
        TODO("username"),
        TODO("password")
    ) {
        id = parcel.readInt()
        name = parcel.readString()!!
        phone_number = parcel.readString()!!
        username = parcel.readString()!!
        password = parcel.readString()!!
    }

    init{
        this.id = id
        this.name = name
        this.phone_number = phone_number
        this.birthday = birthday
        this.username = username
        this.password =  password
        this.shopping_list = ArrayList<Product>()
        this.favorite_products = ArrayList<Product>()
    }

    @Ignore
    constructor(username: String,password: String)
            : this(-1,"","0",LocalDate.now(),username,password)

    fun checkUsername (username: String):Boolean{
        if(this.username == username)
            return true
        else
            return false
    }

    fun checkPassword (password: String):Boolean{
        if (this.password == password)
            return true
        else
            return false
    }

   fun totalPrice():Int{
        var total = 0
        for (product in this.shopping_list){
            total += product.quantity*product.price
        }
        return total
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(phone_number)
        parcel.writeString(username)
        parcel.writeString(password)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }


}