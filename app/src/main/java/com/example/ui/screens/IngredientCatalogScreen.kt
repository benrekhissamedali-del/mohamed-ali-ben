package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.components.formatCurrency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientCatalogScreen(
    ingredients: List<CatalogIngredient>,
    onSaveIngredient: (CatalogIngredient) -> Unit,
    onDeleteIngredient: (CatalogIngredient) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingIngredient by remember { mutableStateOf<CatalogIngredient?>(null) }

    val categories = listOf(
        "Spiritueux",
        "Sirops & Jus",
        "Garnitures & Épices",
        "Viandes & Poissons",
        "Légumes & Fruits",
        "Épicerie & Laiterie"
    )

    val filtered = ingredients.filter { ing ->
        val matchesCategory = selectedCategory == null || ing.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                ing.name.lowercase().contains(searchQuery.lowercase()) ||
                ing.supplier.lowercase().contains(searchQuery.lowercase()) ||
                ing.category.lowercase().contains(searchQuery.lowercase())
        matchesCategory && matchesSearch
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("catalog_info_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Mercuriale des Prix d'Achat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gérez vos coûts d'approvisionnement (bouteilles de bar, viandes, crèmerie, fruits). Ces ingrédients s'intègrent en 1 clic dans vos fiches techniques.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher un ingrédient, fournisseur...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_catalog_search")
                )
            }

            // Category filter chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("Toutes (${ingredients.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                            )
                        )
                    }
                }
            }

            // Items or empty state
            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Aucun ingrédient trouvé dans la mercuriale.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filtered, key = { it.id }) { ingredient ->
                    CatalogIngredientCard(
                        ingredient = ingredient,
                        onEdit = {
                            editingIngredient = ingredient
                            showEditDialog = true
                        },
                        onDelete = { onDeleteIngredient(ingredient) }
                    )
                }
            }
        }

        // FAB
        ExtendedFloatingActionButton(
            onClick = {
                editingIngredient = null
                showEditDialog = true
            },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Ajouter Ingrédient") },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_catalog_ingredient")
        )
    }

    // Add / Edit Dialog
    if (showEditDialog) {
        var name by remember { mutableStateOf(editingIngredient?.name ?: "") }
        var category by remember { mutableStateOf(editingIngredient?.category ?: "Épicerie & Laiterie") }
        var defaultUnit by remember { mutableStateOf(editingIngredient?.defaultUnit ?: "g") }
        var packPriceText by remember { mutableStateOf(if (editingIngredient != null) String.format(Locale.US, "%.2f", editingIngredient!!.packPrice) else "10.00") }
        var packQtyText by remember { mutableStateOf(if (editingIngredient != null) String.format(Locale.US, "%.2f", editingIngredient!!.packQuantity) else "1000.00") }
        var packUnit by remember { mutableStateOf(editingIngredient?.packUnit ?: "g") }
        var wasteText by remember { mutableStateOf(if (editingIngredient != null) String.format(Locale.US, "%.0f", editingIngredient!!.defaultWastePercent) else "0") }
        var supplier by remember { mutableStateOf(editingIngredient?.supplier ?: "") }
        var categoryExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(if (editingIngredient == null) "Nouvel Ingrédient Mercuriale" else "Modifier Ingrédient") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nom de l'ingrédient *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("catalog_dialog_name")
                        )
                    }

                    item {
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
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = packPriceText,
                                onValueChange = { packPriceText = it },
                                label = { Text("Prix Achat (€)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("catalog_dialog_pack_price")
                            )
                            OutlinedTextField(
                                value = packQtyText,
                                onValueChange = { packQtyText = it },
                                label = { Text("Quantité Achat") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("catalog_dialog_pack_qty")
                            )
                            OutlinedTextField(
                                value = packUnit,
                                onValueChange = { packUnit = it },
                                label = { Text("Unité") },
                                modifier = Modifier.weight(0.8f).testTag("catalog_dialog_pack_unit")
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = defaultUnit,
                                onValueChange = { defaultUnit = it },
                                label = { Text("Unité Recette (ex: cl, g)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = wasteText,
                                onValueChange = { wasteText = it },
                                label = { Text("Perte (%)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = supplier,
                            onValueChange = { supplier = it },
                            label = { Text("Fournisseur / Grossiste") },
                            placeholder = { Text("ex: Metro, Rungis, Fournisseur Bar...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val ing = CatalogIngredient(
                                id = editingIngredient?.id ?: 0,
                                name = name.trim(),
                                category = category,
                                defaultUnit = defaultUnit.trim(),
                                packPrice = packPriceText.toDoubleOrNull() ?: 0.0,
                                packQuantity = packQtyText.toDoubleOrNull() ?: 1.0,
                                packUnit = packUnit.trim(),
                                defaultWastePercent = wasteText.toDoubleOrNull() ?: 0.0,
                                supplier = supplier.trim(),
                                updatedAt = System.currentTimeMillis()
                            )
                            onSaveIngredient(ing)
                            showEditDialog = false
                        }
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun CatalogIngredientCard(
    ingredient: CatalogIngredient,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val unitCost = if (ingredient.packQuantity > 0) ingredient.packPrice / ingredient.packQuantity else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = ingredient.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ingredient.category,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${formatCurrency(ingredient.packPrice)} le conditionnement de ${String.format(Locale.FRANCE, "%.1f", ingredient.packQuantity)} ${ingredient.packUnit} (${String.format(Locale.FRANCE, "%.3f € / %s", unitCost, ingredient.packUnit)})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (ingredient.supplier.isNotBlank() || ingredient.defaultWastePercent > 0) {
                    Text(
                        text = "${if (ingredient.supplier.isNotBlank()) "Fournisseur: ${ingredient.supplier} · " else ""}Perte: ${ingredient.defaultWastePercent.toInt()}%",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
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
