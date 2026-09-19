package com.example.data.model

enum class RecipeType(val label: String, val iconName: String) {
    PLAT("Plat", "restaurant"),
    COCKTAIL("Cocktail", "local_bar")
}

enum class DishCategory(val label: String) {
    ENTREE("Entrée"),
    PLAT("Plat Principal"),
    DESSERT("Dessert"),
    SAUCE_BASE("Sauce & Base"),
    SNACK("Snack & Tapas");

    companion object {
        fun allLabels(): List<String> = entries.map { it.label }
    }
}

enum class CocktailCategory(val label: String) {
    SIGNATURE("Signature"),
    CLASSIQUE("Classique"),
    MOCKTAIL("Sans Alcool"),
    LONG_DRINK("Long Drink"),
    SHOT_DIGESTIF("Digestif & Shot");

    companion object {
        fun allLabels(): List<String> = entries.map { it.label }
    }
}

enum class IngredientUnit(val symbol: String, val displayName: String, val baseType: UnitBaseType) {
    // Masse
    G("g", "Gramme", UnitBaseType.WEIGHT),
    KG("kg", "Kilogramme", UnitBaseType.WEIGHT),
    
    // Volume
    ML("ml", "Millilitre", UnitBaseType.VOLUME),
    CL("cl", "Centilitre", UnitBaseType.VOLUME),
    L("L", "Litre", UnitBaseType.VOLUME),
    
    // Unités de service / comptage
    PIECE("pce", "Pièce / Unité", UnitBaseType.COUNT),
    DASH("dash", "Trait (dash)", UnitBaseType.COUNT),
    BARSPOON("c.à.c", "Cuillère de bar", UnitBaseType.COUNT),
    BUNCH("botte", "Botte / Bouquet", UnitBaseType.COUNT),
    PINCH("pincée", "Pincée", UnitBaseType.COUNT);

    companion object {
        fun fromSymbol(symbol: String): IngredientUnit {
            return entries.find { it.symbol.equals(symbol, ignoreCase = true) } ?: G
        }
    }
}

enum class UnitBaseType {
    WEIGHT,
    VOLUME,
    COUNT
}

data class RecipeIngredient(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val quantity: Double,
    val unit: String,
    val packPrice: Double,
    val packQuantity: Double,
    val packUnit: String,
    val wastePercentage: Double = 0.0 // % de perte (épluchures, parures, garniture)
) {
    /**
     * Calcule le coût réel de l'ingrédient pour la recette en tenant compte des conversions d'unités
     * et du taux de perte/parage.
     */
    fun calculateCost(): Double {
        if (packQuantity <= 0 || packPrice <= 0 || quantity <= 0) return 0.0

        val qtyInBase = convertToBase(quantity, unit)
        val packInBase = convertToBase(packQuantity, packUnit)

        val baseCost = if (packInBase > 0) {
            (qtyInBase / packInBase) * packPrice
        } else {
            0.0
        }

        // Taux de perte (ex: 10% de perte => le coût effectif augmente de 1 / (1 - 0.10))
        val wasteMultiplier = if (wastePercentage in 0.0..90.0) {
            1.0 / (1.0 - (wastePercentage / 100.0))
        } else {
            1.0
        }

        return baseCost * wasteMultiplier
    }

    private fun convertToBase(value: Double, unitSymbol: String): Double {
        return when (unitSymbol.lowercase().trim()) {
            "kg" -> value * 1000.0 // base: gramme
            "g" -> value
            "l" -> value * 1000.0 // base: ml
            "cl" -> value * 10.0 // base: ml
            "ml" -> value
            "dash" -> value * 0.8 // ~0.8 ml par dash
            "c.à.c", "barspoon" -> value * 5.0 // ~5 ml
            else -> value // pour pièces, bottes, etc.
        }
    }
}

enum class FoodCostHealth {
    EXCELLENT, // <= cible
    BON,       // <= cible + 3%
    ATTENTION, // <= cible + 8%
    CRITIQUE   // > cible + 8%
}

data class FinancialAnalysis(
    val totalCost: Double,
    val costPerPortion: Double,
    val sellingPriceTTC: Double,
    val vatRate: Double,
    val sellingPriceHT: Double,
    val grossMarginPerPortion: Double,
    val actualFoodCostPercent: Double,
    val targetFoodCostPercent: Double,
    val recommendedSellingPriceHT: Double,
    val recommendedSellingPriceTTC: Double,
    val multiplierCoefficientTTC: Double,
    val multiplierCoefficientHT: Double,
    val health: FoodCostHealth
)
