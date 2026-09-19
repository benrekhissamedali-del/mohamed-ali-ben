package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodCostHealth
import com.example.data.model.RecipeType
import com.example.ui.components.FoodCostBadge
import com.example.ui.components.KpiMiniCard
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatPercent
import com.example.ui.theme.ColorHealthCritical
import com.example.ui.theme.ColorHealthExcellent
import com.example.ui.theme.ColorHealthGood
import com.example.ui.theme.ColorHealthWarning
import java.util.Locale

enum class ExpressMode {
    TARGET_TO_PRICE, // Coût + Cible => Prix conseillé
    PRICE_TO_HEALTH  // Coût + Prix fixé => Food Cost réel
}

@Composable
fun ExpressCalculatorScreen(
    onCreateFromExpress: (RecipeType, Double, Double, Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(ExpressMode.TARGET_TO_PRICE) }
    var recipeType by remember { mutableStateOf(RecipeType.PLAT) }

    var costText by remember { mutableStateOf("4.50") }
    var targetFoodCostPercent by remember { mutableDoubleStateOf(28.0) }
    var vatPercent by remember { mutableDoubleStateOf(10.0) }
    var sellingPriceText by remember { mutableStateOf("16.00") }

    val cost = costText.toDoubleOrNull() ?: 0.0
    val vatRate = vatPercent / 100.0

    // Calculations for Mode 1 (Target to Price)
    val targetRatio = targetFoodCostPercent / 100.0
    val recommendedPriceHT = if (targetRatio > 0) cost / targetRatio else 0.0
    val recommendedPriceTTC = recommendedPriceHT * (1.0 + vatRate)
    val recommendedMarginHT = recommendedPriceHT - cost
    val multiplierCoeffTTC = if (cost > 0) recommendedPriceTTC / cost else 0.0

    // Calculations for Mode 2 (Price to Health)
    val fixedPriceTTC = sellingPriceText.toDoubleOrNull() ?: 0.0
    val fixedPriceHT = if (vatRate >= 0) fixedPriceTTC / (1.0 + vatRate) else fixedPriceTTC
    val actualFoodCostPercent = if (fixedPriceHT > 0) (cost / fixedPriceHT) * 100.0 else 0.0
    val actualMarginHT = fixedPriceHT - cost
    val actualMultiplierCoeff = if (cost > 0) fixedPriceTTC / cost else 0.0
    val actualHealth = when {
        actualFoodCostPercent <= 0 -> FoodCostHealth.BON
        actualFoodCostPercent <= targetFoodCostPercent -> FoodCostHealth.EXCELLENT
        actualFoodCostPercent <= targetFoodCostPercent + 3 -> FoodCostHealth.BON
        actualFoodCostPercent <= targetFoodCostPercent + 8 -> FoodCostHealth.ATTENTION
        else -> FoodCostHealth.CRITIQUE
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_express_calculator"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Simulateur Express de Rentabilité",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Calculez en quelques secondes le prix de vente idéal ou vérifiez le food cost d'un plat ou cocktail.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = mode == ExpressMode.TARGET_TO_PRICE,
                            onClick = { mode = ExpressMode.TARGET_TO_PRICE },
                            label = { Text("Prix Conseillé", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("chip_mode_target_to_price")
                        )

                        FilterChip(
                            selected = mode == ExpressMode.PRICE_TO_HEALTH,
                            onClick = { mode = ExpressMode.PRICE_TO_HEALTH },
                            label = { Text("Food Cost Réel", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier.weight(1f).testTag("chip_mode_price_to_health")
                        )
                    }
                }
            }
        }

        // Context / Type Preset (Dish vs Cocktail)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = recipeType == RecipeType.PLAT,
                    onClick = {
                        recipeType = RecipeType.PLAT
                        targetFoodCostPercent = 28.0
                        vatPercent = 10.0
                    },
                    label = { Text("🍽️ Cuisine (Plat)") },
                    modifier = Modifier.weight(1f).testTag("express_type_plat")
                )
                FilterChip(
                    selected = recipeType == RecipeType.COCKTAIL,
                    onClick = {
                        recipeType = RecipeType.COCKTAIL
                        targetFoodCostPercent = 18.0
                        vatPercent = 20.0
                    },
                    label = { Text("🍸 Bar (Cocktail)") },
                    modifier = Modifier.weight(1f).testTag("express_type_cocktail")
                )
            }
        }

        // Input Parameters Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Paramètres de calcul",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Material Cost Input
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        label = { Text("Coût matière unitaire (€)") },
                        placeholder = { Text("ex: 4.50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("express_input_cost")
                    )

                    if (mode == ExpressMode.PRICE_TO_HEALTH) {
                        // Selling Price Input
                        OutlinedTextField(
                            value = sellingPriceText,
                            onValueChange = { sellingPriceText = it },
                            label = { Text("Prix de vente TTC proposé (€)") },
                            placeholder = { Text("ex: 18.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("express_input_selling_price")
                        )
                    }

                    // Target Food Cost Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Food Cost cible :", fontSize = 13.sp)
                            Text(
                                formatPercent(targetFoodCostPercent),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Slider(
                            value = targetFoodCostPercent.toFloat(),
                            onValueChange = { targetFoodCostPercent = it.toDouble() },
                            valueRange = 12f..45f,
                            steps = 32,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("express_slider_target")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(18.0, 20.0, 25.0, 28.0, 32.0).forEach { preset ->
                                FilterChip(
                                    selected = targetFoodCostPercent.toInt() == preset.toInt(),
                                    onClick = { targetFoodCostPercent = preset },
                                    label = { Text("${preset.toInt()}%", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // TVA Selector
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Taux de TVA :", fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(Pair(10.0, "10% Food"), Pair(20.0, "20% Alcool"), Pair(5.5, "5.5%")).forEach { (tva, label) ->
                                FilterChip(
                                    selected = vatPercent == tva,
                                    onClick = { vatPercent = tva },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Result Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("express_results_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (mode == ExpressMode.TARGET_TO_PRICE) {
                        Text(
                            text = "Prix de Vente Recommandé",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = formatCurrency(recommendedPriceTTC),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${formatCurrency(recommendedPriceHT)} HT · TVA ${(vatRate * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.FRANCE, "Coeff x %.2f", multiplierCoeffTTC),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KpiMiniCard(
                                title = "Marge Brute HT",
                                value = formatCurrency(recommendedMarginHT),
                                subtitle = "Taux de marge: ${formatPercent(100.0 - targetFoodCostPercent)}",
                                accentColor = ColorHealthExcellent,
                                modifier = Modifier.weight(1f)
                            )
                            KpiMiniCard(
                                title = "Food Cost Atteint",
                                value = formatPercent(targetFoodCostPercent),
                                subtitle = "Conforme à la cible",
                                accentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // Mode: Price to Health
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rentabilité Constatée",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            FoodCostBadge(
                                actualPercent = actualFoodCostPercent,
                                targetPercent = targetFoodCostPercent,
                                health = actualHealth
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KpiMiniCard(
                                title = "Food Cost Réel",
                                value = formatPercent(actualFoodCostPercent),
                                subtitle = "Cible: ${formatPercent(targetFoodCostPercent)}",
                                accentColor = when (actualHealth) {
                                    FoodCostHealth.EXCELLENT -> ColorHealthExcellent
                                    FoodCostHealth.BON -> ColorHealthGood
                                    FoodCostHealth.ATTENTION -> ColorHealthWarning
                                    FoodCostHealth.CRITIQUE -> ColorHealthCritical
                                },
                                modifier = Modifier.weight(1f)
                            )
                            KpiMiniCard(
                                title = "Marge Brute",
                                value = formatCurrency(actualMarginHT),
                                subtitle = "sur prix HT ${formatCurrency(fixedPriceHT)}",
                                accentColor = if (actualMarginHT > 0) ColorHealthExcellent else ColorHealthCritical,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Value Breakdown Bar
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Répartition de l'encaissement TTC :",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val currentPriceTTC = if (mode == ExpressMode.TARGET_TO_PRICE) recommendedPriceTTC else fixedPriceTTC
                        val currentPriceHT = if (mode == ExpressMode.TARGET_TO_PRICE) recommendedPriceHT else fixedPriceHT
                        val currentMargin = if (mode == ExpressMode.TARGET_TO_PRICE) recommendedMarginHT else actualMarginHT
                        val tvaAmount = currentPriceTTC - currentPriceHT

                        if (currentPriceTTC > 0) {
                            val costWeight = (cost / currentPriceTTC).coerceIn(0.05, 0.9).toFloat()
                            val marginWeight = (currentMargin.coerceAtLeast(0.0) / currentPriceTTC).coerceIn(0.05, 0.9).toFloat()
                            val tvaWeight = (tvaAmount.coerceAtLeast(0.0) / currentPriceTTC).coerceIn(0.02, 0.3).toFloat()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(7.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(costWeight)
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(marginWeight)
                                        .fillMaxSize()
                                        .background(ColorHealthExcellent)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(tvaWeight)
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.outline)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Coût ${formatCurrency(cost)}", fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ColorHealthExcellent))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Marge ${formatCurrency(currentMargin)}", fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("TVA ${formatCurrency(tvaAmount)}", fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    // Button: Create technical sheet from this calculation
                    Button(
                        onClick = {
                            val priceToUse = if (mode == ExpressMode.TARGET_TO_PRICE) recommendedPriceTTC else fixedPriceTTC
                            onCreateFromExpress(recipeType, cost, priceToUse, targetFoodCostPercent, vatPercent)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_convert_to_recipe"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Créer une fiche technique avec ces valeurs")
                    }
                }
            }
        }
    }
}
