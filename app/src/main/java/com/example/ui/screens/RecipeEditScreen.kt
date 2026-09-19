package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CatalogIngredient
import com.example.data.model.CocktailCategory
import com.example.data.model.DishCategory
import com.example.data.model.IngredientUnit
import com.example.data.model.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeType
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatPercent
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    existingRecipe: RecipeEntity?,
    initialType: RecipeType,
    catalogIngredients: List<CatalogIngredient>,
    onSave: (RecipeEntity) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var type by remember { mutableStateOf(existingRecipe?.type ?: initialType) }
    var name by remember { mutableStateOf(existingRecipe?.name ?: "") }
    var category by remember {
        mutableStateOf(
            existingRecipe?.category ?: if (type == RecipeType.PLAT) DishCategory.PLAT.label else CocktailCategory.SIGNATURE.label
        )
    }
    var portions by remember { mutableIntStateOf(existingRecipe?.portions ?: 1) }
    var prepTimeMinutes by remember { mutableIntStateOf(existingRecipe?.prepTimeMinutes ?: 15) }
    var targetFoodCostPercent by remember {
        mutableDoubleStateOf(existingRecipe?.targetFoodCostPercent ?: if (type == RecipeType.PLAT) 28.0 else 18.0)
    }
    var vatPercent by remember {
        mutableDoubleStateOf(existingRecipe?.vatPercent ?: if (type == RecipeType.PLAT) 10.0 else 20.0)
    }
    var sellingPriceText by remember {
        mutableStateOf(if (existingRecipe != null && existingRecipe.sellingPriceTTC > 0) String.format(Locale.US, "%.2f", existingRecipe.sellingPriceTTC) else "")
    }
    var notes by remember { mutableStateOf(existingRecipe?.notes ?: "") }

    val ingredients = remember {
        mutableStateListOf<RecipeIngredient>().apply {
            if (existingRecipe != null) {
                addAll(existingRecipe.parseIngredients())
            }
        }
    }

    // Dialog state for adding an ingredient
    var showAddIngredientDialog by remember { mutableStateOf(false) }
    var showCatalogPicker by remember { mutableStateOf(false) }
    var editingIngredientIndex by remember { mutableStateOf<Int?>(null) }

    // Live financial computations
    val totalCost by remember {
        derivedStateOf { ingredients.sumOf { it.calculateCost() } }
    }
    val costPerPortion by remember {
        derivedStateOf { if (portions > 0) totalCost / portions else totalCost }
    }
    val parsedSellingPrice by remember {
        derivedStateOf { sellingPriceText.toDoubleOrNull() ?: 0.0 }
    }
    val vatRate by remember {
        derivedStateOf { vatPercent / 100.0 }
    }
    val sellingPriceHT by remember {
        derivedStateOf { if (vatRate >= 0) parsedSellingPrice / (1.0 + vatRate) else parsedSellingPrice }
    }
    val actualFoodCostPercent by remember {
        derivedStateOf {
            if (sellingPriceHT > 0) (costPerPortion / sellingPriceHT) * 100.0 else 0.0
        }
    }
    val grossMarginPerPortion by remember {
        derivedStateOf { sellingPriceHT - costPerPortion }
    }
    val recommendedSellingPriceHT by remember {
        derivedStateOf {
            val targetRatio = targetFoodCostPercent / 100.0
            if (targetRatio > 0) costPerPortion / targetRatio else 0.0
        }
    }
    val recommendedSellingPriceTTC by remember {
        derivedStateOf { recommendedSellingPriceHT * (1.0 + vatRate) }
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    val categories = if (type == RecipeType.PLAT) DishCategory.allLabels() else CocktailCategory.allLabels()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingRecipe == null) {
                            if (type == RecipeType.PLAT) "Nouveau Plat" else "Nouveau Cocktail"
                        } else {
                            "Modifier la Fiche"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel, modifier = Modifier.testTag("btn_edit_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Annuler")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                val updatedRecipe = RecipeEntity(
                                    id = existingRecipe?.id ?: 0,
                                    name = name.trim(),
                                    type = type,
                                    category = category,
                                    portions = portions.coerceAtLeast(1),
                                    targetFoodCostPercent = targetFoodCostPercent,
                                    vatPercent = vatPercent,
                                    sellingPriceTTC = parsedSellingPrice,
                                    ingredientsJson = RecipeEntity.encodeIngredients(ingredients),
                                    notes = notes.trim(),
                                    prepTimeMinutes = prepTimeMinutes.coerceAtLeast(0),
                                    createdAt = existingRecipe?.createdAt ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(updatedRecipe)
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("btn_save_recipe")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Enregistrer", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        bottomBar = {
            // Live financial sticky bar
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().testTag("sticky_summary_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Coût / Portion", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            formatCurrency(costPerPortion),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Food Cost Réel", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (actualFoodCostPercent > 0) formatPercent(actualFoodCostPercent) else "-- %",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (actualFoodCostPercent in 0.1..targetFoodCostPercent) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Marge / Portion", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (parsedSellingPrice > 0) formatCurrency(grossMarginPerPortion) else "-- €",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type toggle (Plat vs Cocktail)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == RecipeType.PLAT,
                        onClick = {
                            type = RecipeType.PLAT
                            category = DishCategory.PLAT.label
                            if (vatPercent == 20.0) vatPercent = 10.0
                            if (targetFoodCostPercent == 18.0) targetFoodCostPercent = 28.0
                        },
                        label = { Text("🍽️ Plat Cuisine") },
                        leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("chip_type_plat")
                    )
                    FilterChip(
                        selected = type == RecipeType.COCKTAIL,
                        onClick = {
                            type = RecipeType.COCKTAIL
                            category = CocktailCategory.SIGNATURE.label
                            if (vatPercent == 10.0) vatPercent = 20.0
                            if (targetFoodCostPercent == 28.0) targetFoodCostPercent = 18.0
                        },
                        label = { Text("🍸 Cocktail Bar") },
                        leadingIcon = { Icon(Icons.Default.LocalBar, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier.weight(1f).testTag("chip_type_cocktail")
                    )
                }
            }

            // General Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nom de la création *") },
                            placeholder = { Text(if (type == RecipeType.PLAT) "ex: Filet de Bœuf Rossini" else "ex: Mojito Signature") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_recipe_name")
                        )

                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Catégorie") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            category = cat
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Portions and Prep Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = portions.toString(),
                                onValueChange = { portions = it.toIntOrNull() ?: 1 },
                                label = { Text(if (type == RecipeType.PLAT) "Portions" else "Verres") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("input_portions")
                            )

                            OutlinedTextField(
                                value = prepTimeMinutes.toString(),
                                onValueChange = { prepTimeMinutes = it.toIntOrNull() ?: 0 },
                                label = { Text("Temps (min)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("input_prep_time")
                            )
                        }
                    }
                }
            }

            // Financial Setup Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Objectifs Financiers & Tarification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Target Food Cost Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Food Cost Cible :", fontSize = 13.sp)
                                Text(
                                    formatPercent(targetFoodCostPercent),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = targetFoodCostPercent.toFloat(),
                                onValueChange = { targetFoodCostPercent = it.toDouble() },
                                valueRange = 10f..45f,
                                steps = 34,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("slider_target_food_cost")
                            )

                            // Quick target presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(18.0, 22.0, 25.0, 28.0, 30.0).forEach { preset ->
                                    FilterChip(
                                        selected = (targetFoodCostPercent.toInt() == preset.toInt()),
                                        onClick = { targetFoodCostPercent = preset },
                                        label = { Text("${preset.toInt()}%", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // TVA presets
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Taux de TVA applicable :", fontSize = 13.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(Pair(10.0, "10% (Food/Soft)"), Pair(20.0, "20% (Alcool)"), Pair(5.5, "5.5% (Emporté)")).forEach { (tva, label) ->
                                    FilterChip(
                                        selected = vatPercent == tva,
                                        onClick = { vatPercent = tva },
                                        label = { Text(label, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Selling Price & Magic Apply Recommended Price
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = sellingPriceText,
                                onValueChange = { sellingPriceText = it },
                                label = { Text("Prix de vente TTC fixé (€)") },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                trailingIcon = {
                                    if (recommendedSellingPriceTTC > 0) {
                                        TextButton(
                                            onClick = {
                                                sellingPriceText = String.format(Locale.US, "%.2f", recommendedSellingPriceTTC)
                                            },
                                            modifier = Modifier.testTag("btn_apply_recommended_price")
                                        ) {
                                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Conseillé", fontSize = 11.sp)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("input_selling_price")
                            )

                            if (recommendedSellingPriceTTC > 0) {
                                Text(
                                    text = "Prix conseillé pour atteindre ${formatPercent(targetFoodCostPercent)} : ${formatCurrency(recommendedSellingPriceTTC)} TTC (${formatCurrency(recommendedSellingPriceHT)} HT)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Ingredients Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ingrédients de la Recette (${ingredients.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total : ${formatCurrency(totalCost)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Quick add buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showCatalogPicker = true },
                        modifier = Modifier.weight(1f).testTag("btn_pick_from_catalog"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("De la Mercuriale", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            editingIngredientIndex = null
                            showAddIngredientDialog = true
                        },
                        modifier = Modifier.weight(1f).testTag("btn_add_custom_ingredient")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nouvel Ingrédient", fontSize = 12.sp)
                    }
                }
            }

            // Ingredient Items
            if (ingredients.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ajoutez des ingrédients pour calculer automatiquement le coût matière.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                itemsIndexed(ingredients) { index, ingredient ->
                    val lineCost = ingredient.calculateCost()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ingredient.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${String.format(Locale.FRANCE, "%.1f", ingredient.quantity)} ${ingredient.unit} · ${formatCurrency(ingredient.packPrice)} / ${String.format(Locale.FRANCE, "%.1f", ingredient.packQuantity)} ${ingredient.packUnit}${if (ingredient.wastePercentage > 0) " (perte ${ingredient.wastePercentage.toInt()}%)" else ""}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = formatCurrency(lineCost),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = {
                                        editingIngredientIndex = index
                                        showAddIngredientDialog = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Modifier", modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { ingredients.removeAt(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Supprimer",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notes field
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes de préparation / Dressage / Service") },
                    placeholder = { Text("Conseils pour la brigade ou les barmans...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("input_recipe_notes"),
                    maxLines = 4
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Catalog Picker Dialog
    if (showCatalogPicker) {
        AlertDialog(
            onDismissRequest = { showCatalogPicker = false },
            title = { Text("Sélectionner dans la Mercuriale") },
            text = {
                LazyColumn(
                    modifier = Modifier.height(350.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(catalogIngredients.size) { i ->
                        val catIng = catalogIngredients[i]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Add to ingredients with default 1 unit and open edit if desired
                                    val defaultQty = when (catIng.defaultUnit) {
                                        "g" -> 100.0
                                        "cl" -> 4.0
                                        "ml" -> 50.0
                                        "dash" -> 2.0
                                        else -> 1.0
                                    }
                                    ingredients.add(
                                        RecipeIngredient(
                                            name = catIng.name,
                                            quantity = defaultQty,
                                            unit = catIng.defaultUnit,
                                            packPrice = catIng.packPrice,
                                            packQuantity = catIng.packQuantity,
                                            packUnit = catIng.packUnit,
                                            wastePercentage = catIng.defaultWastePercent
                                        )
                                    )
                                    showCatalogPicker = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(catIng.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    "${formatCurrency(catIng.packPrice)} / ${String.format(Locale.FRANCE, "%.1f", catIng.packQuantity)} ${catIng.packUnit} (${catIng.category})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCatalogPicker = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Ingredient Edit / Add Dialog
    if (showAddIngredientDialog) {
        val editing = editingIngredientIndex?.let { ingredients.getOrNull(it) }

        var ingName by remember { mutableStateOf(editing?.name ?: "") }
        var ingQty by remember { mutableStateOf(if (editing != null) String.format(Locale.US, "%.2f", editing.quantity) else "1.0") }
        var ingUnit by remember { mutableStateOf(editing?.unit ?: if (type == RecipeType.PLAT) "g" else "cl") }
        var ingPackPrice by remember { mutableStateOf(if (editing != null) String.format(Locale.US, "%.2f", editing.packPrice) else "10.0") }
        var ingPackQty by remember { mutableStateOf(if (editing != null) String.format(Locale.US, "%.2f", editing.packQuantity) else if (type == RecipeType.PLAT) "1000.0" else "70.0") }
        var ingPackUnit by remember { mutableStateOf(editing?.packUnit ?: if (type == RecipeType.PLAT) "g" else "cl") }
        var ingWaste by remember { mutableStateOf(if (editing != null) String.format(Locale.US, "%.0f", editing.wastePercentage) else "0") }

        AlertDialog(
            onDismissRequest = { showAddIngredientDialog = false },
            title = { Text(if (editing == null) "Ajouter un Ingrédient" else "Modifier l'Ingrédient") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        OutlinedTextField(
                            value = ingName,
                            onValueChange = { ingName = it },
                            label = { Text("Nom de l'ingrédient *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("dialog_ing_name")
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = ingQty,
                                onValueChange = { ingQty = it },
                                label = { Text("Quantité") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("dialog_ing_qty")
                            )
                            OutlinedTextField(
                                value = ingUnit,
                                onValueChange = { ingUnit = it },
                                label = { Text("Unité (g, cl, pce)") },
                                modifier = Modifier.weight(1f).testTag("dialog_ing_unit")
                            )
                        }
                    }

                    item {
                        Text("Conditionnement d'achat du fournisseur :", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = ingPackPrice,
                                onValueChange = { ingPackPrice = it },
                                label = { Text("Prix (€)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("dialog_ing_pack_price")
                            )
                            OutlinedTextField(
                                value = ingPackQty,
                                onValueChange = { ingPackQty = it },
                                label = { Text("Contenance") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("dialog_ing_pack_qty")
                            )
                            OutlinedTextField(
                                value = ingPackUnit,
                                onValueChange = { ingPackUnit = it },
                                label = { Text("Unité") },
                                modifier = Modifier.weight(0.8f).testTag("dialog_ing_pack_unit")
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = ingWaste,
                            onValueChange = { ingWaste = it },
                            label = { Text("Perte / Parage (%)") },
                            placeholder = { Text("ex: 10% pour épluchures") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("dialog_ing_waste")
                        )
                    }

                    // Cost estimation preview
                    item {
                        val previewIngredient = RecipeIngredient(
                            name = ingName,
                            quantity = ingQty.toDoubleOrNull() ?: 0.0,
                            unit = ingUnit,
                            packPrice = ingPackPrice.toDoubleOrNull() ?: 0.0,
                            packQuantity = ingPackQty.toDoubleOrNull() ?: 1.0,
                            packUnit = ingPackUnit,
                            wastePercentage = ingWaste.toDoubleOrNull() ?: 0.0
                        )
                        val previewCost = previewIngredient.calculateCost()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Coût calculé pour la recette : ${formatCurrency(previewCost)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ingName.isNotBlank()) {
                            val newIng = RecipeIngredient(
                                id = editing?.id ?: java.util.UUID.randomUUID().toString(),
                                name = ingName.trim(),
                                quantity = ingQty.toDoubleOrNull() ?: 1.0,
                                unit = ingUnit.trim(),
                                packPrice = ingPackPrice.toDoubleOrNull() ?: 0.0,
                                packQuantity = ingPackQty.toDoubleOrNull() ?: 1.0,
                                packUnit = ingPackUnit.trim(),
                                wastePercentage = ingWaste.toDoubleOrNull() ?: 0.0
                            )
                            val idx = editingIngredientIndex
                            if (idx != null && idx in ingredients.indices) {
                                ingredients[idx] = newIng
                            } else {
                                ingredients.add(newIng)
                            }
                            showAddIngredientDialog = false
                        }
                    },
                    enabled = ingName.isNotBlank()
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddIngredientDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
