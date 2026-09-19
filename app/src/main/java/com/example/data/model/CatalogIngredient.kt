package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_ingredients")
data class CatalogIngredient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // "Viandes & Poissons", "Légumes & Fruits", "Épicerie & Laiterie", "Spiritueux", "Sirops & Jus", "Garnitures & Épices"
    val defaultUnit: String, // "g", "cl", "pièce"
    val packPrice: Double, // Prix d'achat du conditionnement
    val packQuantity: Double, // Quantité du conditionnement
    val packUnit: String, // Unité du conditionnement (kg, L, cl, pièce)
    val defaultWastePercent: Double = 0.0,
    val supplier: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
