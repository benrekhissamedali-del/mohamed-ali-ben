package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.FoodCostHealth
import com.example.data.model.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PdfExportTest {

    @Test
    fun testRecipeFinancialCalculationsForPdf() {
        val ingredients = listOf(
            RecipeIngredient(
                name = "Filet de bœuf Aubrac",
                quantity = 180.0,
                unit = "g",
                packPrice = 32.0,
                packQuantity = 1.0,
                packUnit = "kg",
                wastePercentage = 5.0 // (180 / 1000) * 32 / (1 - 0.05) = 5.76 / 0.95 = 6.0631 €
            ),
            RecipeIngredient(
                name = "Pommes de terre grenailles",
                quantity = 120.0,
                unit = "g",
                packPrice = 3.5,
                packQuantity = 1.0,
                packUnit = "kg",
                wastePercentage = 10.0 // (120 / 1000) * 3.5 / 0.9 = 0.4667 €
            ),
            RecipeIngredient(
                name = "Beurre demi-sel AOP",
                quantity = 25.0,
                unit = "g",
                packPrice = 2.8,
                packQuantity = 250.0,
                packUnit = "g",
                wastePercentage = 0.0 // (25 / 250) * 2.8 = 0.28 €
            )
        )

        val recipe = RecipeEntity(
            id = 101,
            name = "Filet de Bœuf Saisi & Grenailles",
            type = RecipeType.PLAT,
            category = "Plat",
            portions = 1,
            prepTimeMinutes = 20,
            sellingPriceTTC = 28.0,
            vatPercent = 10.0,
            targetFoodCostPercent = 25.0,
            notes = "Marquer le filet à feu vif, arroser généreusement de beurre noisette.",
            ingredientsJson = RecipeEntity.encodeIngredients(ingredients)
        )

        val financials = recipe.computeFinancials()
        val parsedIngredients = recipe.parseIngredients()

        assertEquals(3, parsedIngredients.size)
        assertTrue("Le coût total doit être calculé", financials.totalCost > 6.0)
        assertEquals(financials.totalCost, financials.costPerPortion, 0.001)

        // Prix de vente HT = 28 / 1.10 = 25.45 €
        assertEquals(28.0 / 1.10, financials.sellingPriceHT, 0.01)

        // Food Cost % = (Coût / Prix HT) * 100
        val expectedRatio = (financials.costPerPortion / financials.sellingPriceHT) * 100.0
        assertEquals(expectedRatio, financials.actualFoodCostPercent, 0.01)

        // Ratio (26.75%) > target (25%) but <= target + 3%, should be BON
        assertEquals(FoodCostHealth.BON, financials.health)
        assertTrue("Le coefficient multiplicateur TTC doit être > 1", financials.multiplierCoefficientTTC > 1.0)
    }

    @Test
    fun testCocktailFinancialCalculationsForPdf() {
        val ingredients = listOf(
            RecipeIngredient(
                name = "Rhum Blanc Agricole 50°",
                quantity = 5.0,
                unit = "cl",
                packPrice = 19.50,
                packQuantity = 70.0,
                packUnit = "cl",
                wastePercentage = 2.0
            ),
            RecipeIngredient(
                name = "Citron Vert Frais",
                quantity = 0.5,
                unit = "pièce",
                packPrice = 0.60,
                packQuantity = 1.0,
                packUnit = "pièce",
                wastePercentage = 10.0
            ),
            RecipeIngredient(
                name = "Sirop de Sucre de Canne",
                quantity = 2.0,
                unit = "cl",
                packPrice = 4.20,
                packQuantity = 70.0,
                packUnit = "cl",
                wastePercentage = 0.0
            )
        )

        val cocktail = RecipeEntity(
            id = 202,
            name = "Ti Punch Artisanal",
            type = RecipeType.COCKTAIL,
            category = "Signature",
            portions = 1,
            prepTimeMinutes = 3,
            sellingPriceTTC = 9.50,
            vatPercent = 20.0,
            targetFoodCostPercent = 18.0,
            notes = "Écraser délicatement le quartier de citron vert avec le sirop.",
            ingredientsJson = RecipeEntity.encodeIngredients(ingredients)
        )

        val financials = cocktail.computeFinancials()
        assertTrue(financials.totalCost > 1.0)
        assertEquals(9.50 / 1.20, financials.sellingPriceHT, 0.01)
        assertTrue("La marge brute par verre doit être positive", financials.grossMarginPerPortion > 0.0)
        assertNotNull(financials.health)
    }

    @Test
    fun testPdfCacheDirectoryConfiguration() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fichesDir = File(context.cacheDir, "fiches")
        if (!fichesDir.exists()) {
            fichesDir.mkdirs()
        }

        assertTrue("Le dossier de cache pour les fiches doit exister", fichesDir.exists())
        assertTrue("Le dossier doit être un répertoire", fichesDir.isDirectory)
    }
}
