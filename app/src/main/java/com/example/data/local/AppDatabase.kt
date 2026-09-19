package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.CatalogIngredient
import com.example.data.model.RecipeEntity
import com.example.data.model.RecipeType

class DatabaseConverters {
    @TypeConverter
    fun fromRecipeType(type: RecipeType): String = type.name

    @TypeConverter
    fun toRecipeType(value: String): RecipeType = try {
        RecipeType.valueOf(value)
    } catch (e: Exception) {
        RecipeType.PLAT
    }
}

@Database(
    entities = [RecipeEntity::class, CatalogIngredient::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "foodcost_pro_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
