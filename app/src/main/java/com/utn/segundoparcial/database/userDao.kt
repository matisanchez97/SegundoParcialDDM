package com.utn.segundoparcial.database

import androidx.room.*
import com.utn.segundoparcial.entities.User

@Dao
public interface userDao {

    @Query("SELECT * FROM users ORDER BY id")
    fun loadAllPersons(): MutableList<User>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPerson(user: User?)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updatePerson(user: User?)

    @Delete
    fun delete(user: User?)

    @Query("SELECT * FROM users WHERE id = :id")
    fun loadPersonById(id: Int): User?

}