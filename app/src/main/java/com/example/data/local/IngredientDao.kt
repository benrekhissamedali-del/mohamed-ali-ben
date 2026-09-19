package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CatalogIngredient
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM catalog_ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<CatalogIngredient>>

    @Query("SELECT * FROM catalog_ingredients WHERE category = :category ORDER BY name ASC")
    fun getIngredientsByCategory(category: String): Flow<List<CatalogIngredient>>

    @Query("SELECT DISTINCT category FROM catalog_ingredients ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: CatalogIngredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<CatalogIngredient>)

    @Update
    suspend fun updateIngredient(ingredient: CatalogIngredient)

    @Delete
    suspend fun deleteIngredient(ingredient: CatalogIngredient)

    @Query("SELECT COUNT(*) FROM catalog_ingredients")
    suspend fun getIngredientCount(): Int
}
