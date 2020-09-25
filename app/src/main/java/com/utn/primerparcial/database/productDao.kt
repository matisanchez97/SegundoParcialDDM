package com.utn.primerparcial.database

import com.utn.primerparcial.entities.Product
import androidx.room.*
@Dao
public interface productDao {

    @Query("SELECT * FROM products ORDER BY id")
    fun loadAllProducts(): MutableList<Product>?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :search || '%'")
    fun loadSimilarProducts(search: String?): MutableList<Product>?

    @Query("SELECT * FROM products WHERE name = :name")
    fun loadProductsByName(name: String?): MutableList<Product>?

    @Query("SELECT * FROM products WHERE brand = :brand")
    fun loadProductsByBrand(brand: String?): MutableList<Product>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: Product?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMultipleProduct(product: MutableList<Product>?)

    @Update
    fun updateProduct(product: Product?)

    @Delete
    fun delete(product: Product?)

    @Query("SELECT * FROM products WHERE id = :id")
    fun loadProductById(id: Int): Product?

    @Query("SELECT * FROM products WHERE name = :name AND brand = :brand")
    fun loadProductByNameAndBrand(name: String?, brand: String?): Product?

    @Query("SELECT * FROM products WHERE name = :name AND brand = :brand AND measure = :measure")
    fun loadProductByNameAndBrandAndMeasure(name: String?, brand: String?,measure: String?): Product?
}