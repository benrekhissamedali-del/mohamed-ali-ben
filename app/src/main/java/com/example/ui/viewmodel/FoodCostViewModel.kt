package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CatalogIngredient
import com.example.data.model.RecipeEntity
import com.example.data.model.RecipeType
import com.example.data.repository.FoodCostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab(val label: String) {
    RECIPES("Recettes"),
    EXPRESS_CALC("Calculateur Express"),
    MERCURIALE("Mercuriale Ingrédients")
}

enum class SortOrder(val label: String) {
    RECENT("Plus récents"),
    NAME("Nom A-Z"),
    FOOD_COST_ASC("Food Cost croissant"),
    FOOD_COST_DESC("Food Cost décroissant"),
    MARGIN_DESC("Marge (€) décroissante"),
    PRICE_DESC("Prix TTC décroissant")
}

sealed interface AppScreen {
    data object List : AppScreen
    data class Detail(val recipeId: Long) : AppScreen
    data class Edit(val recipeId: Long? = null, val initialType: RecipeType = RecipeType.PLAT) : AppScreen
}

data class DashboardStats(
    val totalRecipes: Int = 0,
    val dishCount: Int = 0,
    val cocktailCount: Int = 0,
    val averageFoodCostPercent: Double = 0.0,
    val averageGrossMargin: Double = 0.0
)

class FoodCostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FoodCostRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FoodCostRepository(db.recipeDao(), db.ingredientDao())
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    private val _currentTab = MutableStateFlow(MainTab.RECIPES)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.List)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow<RecipeType?>(null) // null = all
    val typeFilter: StateFlow<RecipeType?> = _typeFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.RECENT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _snackBarMessage = MutableStateFlow<String?>(null)
    val snackBarMessage: StateFlow<String?> = _snackBarMessage.asStateFlow()

    val allCatalogIngredients: StateFlow<List<CatalogIngredient>> = repository.allCatalogIngredients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawRecipes: StateFlow<List<RecipeEntity>> = repository.allRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRecipes: StateFlow<List<RecipeEntity>> = combine(
        rawRecipes,
        _searchQuery,
        _typeFilter,
        _sortOrder
    ) { recipes, query, type, sort ->
        var list = recipes

        if (type != null) {
            list = list.filter { it.type == type }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { recipe ->
                recipe.name.lowercase().contains(q) ||
                        recipe.category.lowercase().contains(q) ||
                        recipe.ingredientsJson.lowercase().contains(q)
            }
        }

        when (sort) {
            SortOrder.RECENT -> list.sortedByDescending { it.updatedAt }
            SortOrder.NAME -> list.sortedBy { it.name.lowercase() }
            SortOrder.FOOD_COST_ASC -> list.sortedBy { it.computeFinancials().actualFoodCostPercent }
            SortOrder.FOOD_COST_DESC -> list.sortedByDescending { it.computeFinancials().actualFoodCostPercent }
            SortOrder.MARGIN_DESC -> list.sortedByDescending { it.computeFinancials().grossMarginPerPortion }
            SortOrder.PRICE_DESC -> list.sortedByDescending { it.sellingPriceTTC }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<DashboardStats> = rawRecipes.combine(_typeFilter) { recipes, _ ->
        if (recipes.isEmpty()) {
            DashboardStats()
        } else {
            val dishes = recipes.filter { it.type == RecipeType.PLAT }
            val cocktails = recipes.filter { it.type == RecipeType.COCKTAIL }
            val avgFoodCost = recipes.map { it.computeFinancials().actualFoodCostPercent }.average()
            val avgMargin = recipes.map { it.computeFinancials().grossMarginPerPortion }.average()

            DashboardStats(
                totalRecipes = recipes.size,
                dishCount = dishes.size,
                cocktailCount = cocktails.size,
                averageFoodCostPercent = if (avgFoodCost.isNaN()) 0.0 else avgFoodCost,
                averageGrossMargin = if (avgMargin.isNaN()) 0.0 else avgMargin
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        if (_currentScreen.value !is AppScreen.List) {
            _currentScreen.value = AppScreen.List
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun navigateBack() {
        _currentScreen.value = AppScreen.List
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: RecipeType?) {
        _typeFilter.value = type
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun showMessage(msg: String) {
        _snackBarMessage.value = msg
    }

    fun clearMessage() {
        _snackBarMessage.value = null
    }

    fun saveRecipe(recipe: RecipeEntity, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveRecipe(recipe)
            showMessage("Recette \"${recipe.name}\" enregistrée !")
            onComplete(id)
        }
    }

    fun deleteRecipe(id: Long) {
        viewModelScope.launch {
            repository.deleteRecipeById(id)
            showMessage("Recette supprimée.")
            _currentScreen.value = AppScreen.List
        }
    }

    fun duplicateRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            val duplicated = recipe.copy(
                id = 0,
                name = "${recipe.name} (Copie)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val newId = repository.saveRecipe(duplicated)
            showMessage("Recette dupliquée avec succès !")
            _currentScreen.value = AppScreen.Detail(newId)
        }
    }

    fun saveCatalogIngredient(ingredient: CatalogIngredient) {
        viewModelScope.launch {
            repository.saveCatalogIngredient(ingredient)
            showMessage("Ingrédient \"${ingredient.name}\" mis à jour.")
        }
    }

    fun deleteCatalogIngredient(ingredient: CatalogIngredient) {
        viewModelScope.launch {
            repository.deleteCatalogIngredient(ingredient)
            showMessage("Ingrédient supprimé de la mercuriale.")
        }
    }
}
