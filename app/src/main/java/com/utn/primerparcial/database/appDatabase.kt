package com.utn.primerparcial.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.utn.primerparcial.entities.Product
import com.utn.primerparcial.entities.User


@Database(entities = [User::class, Product::class], version = 1, exportSchema = false)
@TypeConverters(ConverterShoppingList::class, ConverterLocalDate::class)
public  abstract class appDatabase : RoomDatabase() {

    abstract fun userDao(): userDao
    abstract fun productDao(): productDao


    companion object {
        var INSTANCE: appDatabase? = null

        fun getAppDataBase(context: Context): appDatabase? {
            if (INSTANCE == null) {
                synchronized(appDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        appDatabase::class.java,
                        "myDB"
                    ).allowMainThreadQueries().build() // No es lo mas recomendable que se ejecute en el mainthread
                }
            }
            return INSTANCE
        }

        fun destroyDataBase(){
            INSTANCE = null
        }
    }
}