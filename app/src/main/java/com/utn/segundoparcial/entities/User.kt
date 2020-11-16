package com.utn.segundoparcial.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDate

class User(id: Int,name: String, phone_number: String, birthday: Long, username: String, password: String) :
    Parcelable {


    var id: Int

    var name: String

    var phone_number: String

    var birthday: Long

    var username: String

    var password: String

    constructor() : this(0,"","", LocalDate.now().toEpochDay(),"","")

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

    init {
        this.id = id
        this.name = name
        this.phone_number = phone_number
        this.birthday = birthday
        this.username = username
        this.password = password
    }

    constructor(username: String,password: String)
            : this(-1,"","0",LocalDate.now().toEpochDay(),username,password)

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