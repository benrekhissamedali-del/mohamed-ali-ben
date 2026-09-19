package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: RecipeType, // PLAT or COCKTAIL
    val category: String,
    val portions: Int = 1,
    val targetFoodCostPercent: Double = 28.0,
    val vatPercent: Double = 10.0,
    val sellingPriceTTC: Double = 0.0,
    val ingredientsJson: String = "[]",
    val notes: String = "",
    val prepTimeMinutes: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun parseIngredients(): List<RecipeIngredient> {
        val list = mutableListOf<RecipeIngredient>()
        try {
            val jsonArray = JSONArray(ingredientsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    RecipeIngredient(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = obj.optString("name", ""),
                        quantity = obj.optDouble("quantity", 0.0),
                        unit = obj.optString("unit", "g"),
                        packPrice = obj.optDouble("packPrice", 0.0),
                        packQuantity = obj.optDouble("packQuantity", 1.0),
                        packUnit = obj.optString("packUnit", "kg"),
                        wastePercentage = obj.optDouble("wastePercentage", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun computeFinancials(): FinancialAnalysis {
        val ingredients = parseIngredients()
        val totalCost = ingredients.sumOf { it.calculateCost() }
        val effectivePortions = if (portions > 0) portions else 1
        val costPerPortion = totalCost / effectivePortions

        val vatRate = vatPercent / 100.0
        val sellingPriceHT = if (vatRate >= 0) sellingPriceTTC / (1.0 + vatRate) else sellingPriceTTC
        val grossMarginPerPortion = sellingPriceHT - costPerPortion

        val actualFoodCostPercent = if (sellingPriceHT > 0.0) {
            (costPerPortion / sellingPriceHT) * 100.0
        } else {
            0.0
        }

        val targetRatio = targetFoodCostPercent / 100.0
        val recommendedSellingPriceHT = if (targetRatio > 0.0) {
            costPerPortion / targetRatio
        } else {
            0.0
        }
        val recommendedSellingPriceTTC = recommendedSellingPriceHT * (1.0 + vatRate)

        val multiplierCoeffTTC = if (costPerPortion > 0.0) {
            sellingPriceTTC / costPerPortion
        } else {
            0.0
        }

        val multiplierCoeffHT = if (costPerPortion > 0.0) {
            sellingPriceHT / costPerPortion
        } else {
            0.0
        }

        val health = when {
            actualFoodCostPercent <= 0.0 -> FoodCostHealth.BON
            actualFoodCostPercent <= targetFoodCostPercent -> FoodCostHealth.EXCELLENT
            actualFoodCostPercent <= targetFoodCostPercent + 3.0 -> FoodCostHealth.BON
            actualFoodCostPercent <= targetFoodCostPercent + 8.0 -> FoodCostHealth.ATTENTION
            else -> FoodCostHealth.CRITIQUE
        }

        return FinancialAnalysis(
            totalCost = totalCost,
            costPerPortion = costPerPortion,
            sellingPriceTTC = sellingPriceTTC,
            vatRate = vatRate,
            sellingPriceHT = sellingPriceHT,
            grossMarginPerPortion = grossMarginPerPortion,
            actualFoodCostPercent = actualFoodCostPercent,
            targetFoodCostPercent = targetFoodCostPercent,
            recommendedSellingPriceHT = recommendedSellingPriceHT,
            recommendedSellingPriceTTC = recommendedSellingPriceTTC,
            multiplierCoefficientTTC = multiplierCoeffTTC,
            multiplierCoefficientHT = multiplierCoeffHT,
            health = health
        )
    }

    companion object {
        fun encodeIngredients(ingredients: List<RecipeIngredient>): String {
            val jsonArray = JSONArray()
            for (item in ingredients) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("name", item.name)
                obj.put("quantity", item.quantity)
                obj.put("unit", item.unit)
                obj.put("packPrice", item.packPrice)
                obj.put("packQuantity", item.packQuantity)
                obj.put("packUnit", item.packUnit)
                obj.put("wastePercentage", item.wastePercentage)
                jsonArray.put(obj)
            }
            return jsonArray.toString()
        }
    }
}
