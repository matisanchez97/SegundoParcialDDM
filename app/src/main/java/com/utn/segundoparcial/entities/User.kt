package com.utn.segundoparcial.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDate

class User(id: String,name: String, phone_number: String, birthday: Long, username: String, password: String,email: String) {


    var id: String
    var name: String
    var phone_number: String
    var birthday: Long
    var username: String
    var password: String
    var email: String

    constructor() : this("","","", LocalDate.now().toEpochDay(),"","","")


    init {
        this.id = id
        this.name = name
        this.phone_number = phone_number
        this.birthday = birthday
        this.username = username
        this.password = password
        this.email = email
    }

    constructor(password: String,email: String)
            : this("","","0",LocalDate.now().toEpochDay(),"",password,email)
    constructor(username: String,password: String,email: String)
            : this("","","0",LocalDate.now().toEpochDay(),username,password,email)

    fun checkEmail (email: String):Boolean{
        if(this.email == email)
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

}