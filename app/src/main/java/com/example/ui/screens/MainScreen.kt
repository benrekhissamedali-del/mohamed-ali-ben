package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeType
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FoodCostViewModel
import com.example.ui.viewmodel.MainTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: FoodCostViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val rawRecipes by viewModel.rawRecipes.collectAsStateWithLifecycle()
    val filteredRecipes by viewModel.filteredRecipes.collectAsStateWithLifecycle()
    val dashboardStats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val catalogIngredients by viewModel.allCatalogIngredients.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackBarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    when (val screen = currentScreen) {
        is AppScreen.List -> {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.RestaurantMenu,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "FoodCost Pro",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == MainTab.RECIPES,
                            onClick = { viewModel.selectTab(MainTab.RECIPES) },
                            icon = { Icon(Icons.Default.Restaurant, contentDescription = "Recettes") },
                            label = { Text("Recettes", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("tab_recipes")
                        )

                        NavigationBarItem(
                            selected = currentTab == MainTab.EXPRESS_CALC,
                            onClick = { viewModel.selectTab(MainTab.EXPRESS_CALC) },
                            icon = { Icon(Icons.Default.Calculate, contentDescription = "Express") },
                            label = { Text("Simulateur", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("tab_express_calc")
                        )

                        NavigationBarItem(
                            selected = currentTab == MainTab.MERCURIALE,
                            onClick = { viewModel.selectTab(MainTab.MERCURIALE) },
                            icon = { Icon(Icons.Default.Inventory2, contentDescription = "Mercuriale") },
                            label = { Text("Mercuriale", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("tab_mercuriale")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        MainTab.RECIPES -> {
                            RecipeListScreen(
                                recipes = filteredRecipes,
                                dashboardStats = dashboardStats,
                                searchQuery = searchQuery,
                                typeFilter = typeFilter,
                                sortOrder = sortOrder,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onTypeFilterChange = { viewModel.setTypeFilter(it) },
                                onSortOrderChange = { viewModel.setSortOrder(it) },
                                onRecipeClick = { id -> viewModel.navigateTo(AppScreen.Detail(id)) },
                                onCreateRecipe = { type -> viewModel.navigateTo(AppScreen.Edit(initialType = type)) }
                            )
                        }
                        MainTab.EXPRESS_CALC -> {
                            ExpressCalculatorScreen(
                                onCreateFromExpress = { type, cost, price, targetFC, vat ->
                                    val simulatedRecipe = RecipeEntity(
                                        name = if (type == RecipeType.PLAT) "Nouveau Plat Express" else "Nouveau Cocktail Express",
                                        type = type,
                                        category = if (type == RecipeType.PLAT) "Plat Principal" else "Signature",
                                        portions = 1,
                                        targetFoodCostPercent = targetFC,
                                        vatPercent = vat,
                                        sellingPriceTTC = price,
                                        ingredientsJson = RecipeEntity.encodeIngredients(
                                            listOf(
                                                RecipeIngredient(
                                                    name = "Base matière principale",
                                                    quantity = 1.0,
                                                    unit = if (type == RecipeType.PLAT) "portion" else "verre",
                                                    packPrice = cost,
                                                    packQuantity = 1.0,
                                                    packUnit = if (type == RecipeType.PLAT) "portion" else "verre"
                                                )
                                            )
                                        )
                                    )
                                    viewModel.saveRecipe(simulatedRecipe) { newId ->
                                        viewModel.navigateTo(AppScreen.Edit(recipeId = newId, initialType = type))
                                    }
                                }
                            )
                        }
                        MainTab.MERCURIALE -> {
                            IngredientCatalogScreen(
                                ingredients = catalogIngredients,
                                onSaveIngredient = { viewModel.saveCatalogIngredient(it) },
                                onDeleteIngredient = { viewModel.deleteCatalogIngredient(it) }
                            )
                        }
                    }
                }
            }
        }

        is AppScreen.Detail -> {
            val recipe = rawRecipes.find { it.id == screen.recipeId }
            if (recipe != null) {
                RecipeDetailScreen(
                    recipe = recipe,
                    onBack = { viewModel.navigateBack() },
                    onEdit = { viewModel.navigateTo(AppScreen.Edit(recipeId = recipe.id, initialType = recipe.type)) },
                    onDuplicate = { viewModel.duplicateRecipe(recipe) },
                    onDelete = { viewModel.deleteRecipe(recipe.id) }
                )
            } else {
                LaunchedEffect(Unit) {
                    viewModel.navigateBack()
                }
            }
        }

        is AppScreen.Edit -> {
            val existingRecipe = screen.recipeId?.let { id -> rawRecipes.find { it.id == id } }
            RecipeEditScreen(
                existingRecipe = existingRecipe,
                initialType = screen.initialType,
                catalogIngredients = catalogIngredients,
                onSave = { savedRecipe ->
                    viewModel.saveRecipe(savedRecipe) { id ->
                        viewModel.navigateTo(AppScreen.Detail(id))
                    }
                },
                onCancel = {
                    if (existingRecipe != null) {
                        viewModel.navigateTo(AppScreen.Detail(existingRecipe.id))
                    } else {
                        viewModel.navigateBack()
                    }
                }
            )
        }
    }
}
