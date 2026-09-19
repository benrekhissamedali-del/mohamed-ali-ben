package com.example.data.repository

import com.example.data.local.IngredientDao
import com.example.data.local.RecipeDao
import com.example.data.local.SampleData
import com.example.data.model.CatalogIngredient
import com.example.data.model.RecipeEntity
import com.example.data.model.RecipeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FoodCostRepository(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao
) {
    val allRecipes: Flow<List<RecipeEntity>> = recipeDao.getAllRecipes()
    val allCatalogIngredients: Flow<List<CatalogIngredient>> = ingredientDao.getAllIngredients()
    val allIngredientCategories: Flow<List<String>> = ingredientDao.getAllCategories()

    fun getRecipesByType(type: RecipeType): Flow<List<RecipeEntity>> {
        return recipeDao.getRecipesByType(type)
    }

    suspend fun getRecipeById(id: Long): RecipeEntity? = withContext(Dispatchers.IO) {
        recipeDao.getRecipeById(id)
    }

    suspend fun saveRecipe(recipe: RecipeEntity): Long = withContext(Dispatchers.IO) {
        val updated = recipe.copy(updatedAt = System.currentTimeMillis())
        if (recipe.id == 0L) {
            recipeDao.insertRecipe(updated)
        } else {
            recipeDao.updateRecipe(updated)
            recipe.id
        }
    }

    suspend fun deleteRecipeById(id: Long) = withContext(Dispatchers.IO) {
        recipeDao.deleteRecipeById(id)
    }

    suspend fun saveCatalogIngredient(ingredient: CatalogIngredient): Long = withContext(Dispatchers.IO) {
        val updated = ingredient.copy(updatedAt = System.currentTimeMillis())
        if (ingredient.id == 0L) {
            ingredientDao.insertIngredient(updated)
        } else {
            ingredientDao.updateIngredient(updated)
            ingredient.id
        }
    }

    suspend fun deleteCatalogIngredient(ingredient: CatalogIngredient) = withContext(Dispatchers.IO) {
        ingredientDao.deleteIngredient(ingredient)
    }

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val recipeCount = recipeDao.getRecipeCount()
        if (recipeCount == 0) {
            recipeDao.insertRecipes(SampleData.sampleRecipes)
        }

        val ingredientCount = ingredientDao.getIngredientCount()
        if (ingredientCount == 0) {
            ingredientDao.insertIngredients(SampleData.sampleIngredients)
        }
    }
}
