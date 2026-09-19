package com.example.data.local

import com.example.data.model.CatalogIngredient
import com.example.data.model.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeType

object SampleData {
    val sampleIngredients = listOf(
        // Alcools & Spiritueux
        CatalogIngredient(
            name = "Rhum Blanc Agricole 50°",
            category = "Spiritueux",
            defaultUnit = "cl",
            packPrice = 22.50,
            packQuantity = 70.0,
            packUnit = "cl",
            defaultWastePercent = 1.0,
            supplier = "Distillerie Martinique"
        ),
        CatalogIngredient(
            name = "Gin Artisanal London Dry",
            category = "Spiritueux",
            defaultUnit = "cl",
            packPrice = 28.00,
            packQuantity = 70.0,
            packUnit = "cl",
            defaultWastePercent = 1.0,
            supplier = "Maison Botanique"
        ),
        CatalogIngredient(
            name = "Campari Bitter",
            category = "Spiritueux",
            defaultUnit = "cl",
            packPrice = 17.50,
            packQuantity = 100.0,
            packUnit = "cl",
            defaultWastePercent = 1.0,
            supplier = "Fournisseur Bar"
        ),
        CatalogIngredient(
            name = "Vermouth Rouge Italien",
            category = "Spiritueux",
            defaultUnit = "cl",
            packPrice = 15.00,
            packQuantity = 75.0,
            packUnit = "cl",
            defaultWastePercent = 2.0,
            supplier = "Fournisseur Bar"
        ),
        CatalogIngredient(
            name = "Prosecco DOC Treviso",
            category = "Spiritueux",
            defaultUnit = "cl",
            packPrice = 8.50,
            packQuantity = 75.0,
            packUnit = "cl",
            defaultWastePercent = 3.0,
            supplier = "Vins & Bulles"
        ),
        CatalogIngredient(
            name = "Apérol",
            category = "Spiritueux",
            defaultUnit = "cl",
            packPrice = 16.00,
            packQuantity = 100.0,
            packUnit = "cl",
            defaultWastePercent = 1.0,
            supplier = "Fournisseur Bar"
        ),
        CatalogIngredient(
            name = "Bitter Angostura Aromatic",
            category = "Spiritueux",
            defaultUnit = "dash",
            packPrice = 14.00,
            packQuantity = 200.0, // ~200 dashes par flacon 100ml
            packUnit = "dash",
            defaultWastePercent = 0.0,
            supplier = "Mixology Store"
        ),
        // Sirops, Jus & Softs
        CatalogIngredient(
            name = "Sirop de Sucre de Canne Maison",
            category = "Sirops & Jus",
            defaultUnit = "cl",
            packPrice = 3.20,
            packQuantity = 100.0,
            packUnit = "cl",
            defaultWastePercent = 1.0,
            supplier = "Préparation Maison"
        ),
        CatalogIngredient(
            name = "Jus de Citron Vert Frais (pressé)",
            category = "Sirops & Jus",
            defaultUnit = "cl",
            packPrice = 4.80,
            packQuantity = 100.0,
            packUnit = "cl",
            defaultWastePercent = 5.0,
            supplier = "Maraîcher"
        ),
        CatalogIngredient(
            name = "Eau Gazeuse Perrier",
            category = "Sirops & Jus",
            defaultUnit = "cl",
            packPrice = 1.20,
            packQuantity = 100.0,
            packUnit = "cl",
            defaultWastePercent = 2.0,
            supplier = "Grossiste Boissons"
        ),
        CatalogIngredient(
            name = "Ginger Beer Artisanale",
            category = "Sirops & Jus",
            defaultUnit = "cl",
            packPrice = 1.60,
            packQuantity = 33.0,
            packUnit = "cl",
            defaultWastePercent = 0.0,
            supplier = "Grossiste Boissons"
        ),
        CatalogIngredient(
            name = "Purée Passion Monin / Ravifruit",
            category = "Sirops & Jus",
            defaultUnit = "cl",
            packPrice = 11.50,
            packQuantity = 100.0,
            packUnit = "cl",
            defaultWastePercent = 2.0,
            supplier = "Metro"
        ),
        // Herbes, Fruits & Garnitures
        CatalogIngredient(
            name = "Menthe Fraîche Bouquet",
            category = "Garnitures & Épices",
            defaultUnit = "g",
            packPrice = 2.50,
            packQuantity = 100.0,
            packUnit = "g",
            defaultWastePercent = 15.0, // tiges non utilisées
            supplier = "Maraîcher Bio"
        ),
        CatalogIngredient(
            name = "Citron Vert Frais",
            category = "Garnitures & Épices",
            defaultUnit = "pce",
            packPrice = 0.45,
            packQuantity = 1.0,
            packUnit = "pce",
            defaultWastePercent = 10.0,
            supplier = "Marché Frais"
        ),
        CatalogIngredient(
            name = "Orange Bio pour Zestes",
            category = "Garnitures & Épices",
            defaultUnit = "pce",
            packPrice = 0.65,
            packQuantity = 1.0,
            packUnit = "pce",
            defaultWastePercent = 10.0,
            supplier = "Marché Frais"
        ),
        // Viandes & Poissons
        CatalogIngredient(
            name = "Filet de Bœuf Charolais Extra",
            category = "Viandes & Poissons",
            defaultUnit = "g",
            packPrice = 38.00,
            packQuantity = 1000.0, // 1 kg
            packUnit = "g",
            defaultWastePercent = 8.0, // parage gras et nerfs
            supplier = "Boucherie Rungis"
        ),
        CatalogIngredient(
            name = "Pavé de Saumon Label Rouge Frais",
            category = "Viandes & Poissons",
            defaultUnit = "g",
            packPrice = 26.50,
            packQuantity = 1000.0,
            packUnit = "g",
            defaultWastePercent = 5.0,
            supplier = "Marée Fraîche"
        ),
        CatalogIngredient(
            name = "Escalope de Foie Gras Cru IGP",
            category = "Viandes & Poissons",
            defaultUnit = "g",
            packPrice = 55.00,
            packQuantity = 1000.0,
            packUnit = "g",
            defaultWastePercent = 4.0,
            supplier = "Maison du Foie Gras"
        ),
        // Épicerie, Produits Laitiers & Bases
        CatalogIngredient(
            name = "Beurre Doux AOP Charentes-Poitou",
            category = "Épicerie & Laiterie",
            defaultUnit = "g",
            packPrice = 3.60,
            packQuantity = 250.0,
            packUnit = "g",
            defaultWastePercent = 0.0,
            supplier = "Laiterie"
        ),
        CatalogIngredient(
            name = "Crème Liquide 35% MG",
            category = "Épicerie & Laiterie",
            defaultUnit = "ml",
            packPrice = 4.90,
            packQuantity = 1000.0,
            packUnit = "ml",
            defaultWastePercent = 1.0,
            supplier = "Laiterie"
        ),
        CatalogIngredient(
            name = "Pommes de Terre Ratte du Touquet",
            category = "Légumes & Fruits",
            defaultUnit = "g",
            packPrice = 3.80,
            packQuantity = 1000.0,
            packUnit = "g",
            defaultWastePercent = 12.0, // épluchures
            supplier = "Maraîcher"
        ),
        CatalogIngredient(
            name = "Asperges Vertes Fraîches",
            category = "Légumes & Fruits",
            defaultUnit = "g",
            packPrice = 12.00,
            packQuantity = 1000.0,
            packUnit = "g",
            defaultWastePercent = 18.0, // queues dures
            supplier = "Maraîcher"
        ),
        CatalogIngredient(
            name = "Riz Arborio pour Risotto",
            category = "Épicerie & Laiterie",
            defaultUnit = "g",
            packPrice = 3.90,
            packQuantity = 1000.0,
            packUnit = "g",
            defaultWastePercent = 0.0,
            supplier = "Épicerie Fine"
        ),
        CatalogIngredient(
            name = "Parmigiano Reggiano AOP 24 mois",
            category = "Épicerie & Laiterie",
            defaultUnit = "g",
            packPrice = 24.00,
            packQuantity = 1000.0,
            packUnit = "g",
            defaultWastePercent = 2.0,
            supplier = "Affineur Italien"
        ),
        CatalogIngredient(
            name = "Huile d'Olive Vierge Extra Bio",
            category = "Épicerie & Laiterie",
            defaultUnit = "cl",
            packPrice = 12.50,
            packQuantity = 100.0,
            packUnit = "cl",
            defaultWastePercent = 1.0,
            supplier = "Moulin Provençal"
        ),
        CatalogIngredient(
            name = "Brisures de Truffe Noire du Périgord",
            category = "Épicerie & Laiterie",
            defaultUnit = "g",
            packPrice = 28.00,
            packQuantity = 50.0,
            packUnit = "g",
            defaultWastePercent = 0.0,
            supplier = "Truffière"
        )
    )

    val sampleRecipes = listOf(
        RecipeEntity(
            name = "Filet de Bœuf Rossini & Purée Truffée",
            type = RecipeType.PLAT,
            category = "Plat Principal",
            portions = 1,
            targetFoodCostPercent = 28.0,
            vatPercent = 10.0,
            sellingPriceTTC = 38.00,
            notes = "Plat gastronomique signature. Cuisson bœuf saignante, foie gras poêlé flash 45s de chaque côté. Dressage assiette chaude.",
            prepTimeMinutes = 25,
            ingredientsJson = RecipeEntity.encodeIngredients(
                listOf(
                    RecipeIngredient(
                        name = "Filet de Bœuf Charolais Extra",
                        quantity = 180.0,
                        unit = "g",
                        packPrice = 38.00,
                        packQuantity = 1000.0,
                        packUnit = "g",
                        wastePercentage = 8.0
                    ),
                    RecipeIngredient(
                        name = "Escalope de Foie Gras Cru IGP",
                        quantity = 50.0,
                        unit = "g",
                        packPrice = 55.00,
                        packQuantity = 1000.0,
                        packUnit = "g",
                        wastePercentage = 4.0
                    ),
                    RecipeIngredient(
                        name = "Pommes de Terre Ratte",
                        quantity = 150.0,
                        unit = "g",
                        packPrice = 3.80,
                        packQuantity = 1000.0,
                        packUnit = "g",
                        wastePercentage = 12.0
                    ),
                    RecipeIngredient(
                        name = "Beurre Doux AOP",
                        quantity = 40.0,
                        unit = "g",
                        packPrice = 3.60,
                        packQuantity = 250.0,
                        packUnit = "g",
                        wastePercentage = 0.0
                    ),
                    RecipeIngredient(
                        name = "Crème Liquide 35%",
                        quantity = 30.0,
                        unit = "ml",
                        packPrice = 4.90,
                        packQuantity = 1000.0,
                        packUnit = "ml",
                        wastePercentage = 0.0
                    ),
                    RecipeIngredient(
                        name = "Brisures de Truffe Noire",
                        quantity = 3.0,
                        unit = "g",
                        packPrice = 28.00,
                        packQuantity = 50.0,
                        packUnit = "g",
                        wastePercentage = 0.0
                    )
                )
            )
        ),
        RecipeEntity(
            name = "Saumon Rôti, Risotto & Asperges Vertes",
            type = RecipeType.PLAT,
            category = "Plat Principal",
            portions = 1,
            targetFoodCostPercent = 25.0,
            vatPercent = 10.0,
            sellingPriceTTC = 24.50,
            notes = "Pavé de saumon cuit unilatéral peau croustillante. Risotto mantecato au parmesan et asperges juste blanchies et glacées.",
            prepTimeMinutes = 20,
            ingredientsJson = RecipeEntity.encodeIngredients(
                listOf(
                    RecipeIngredient(
                        name = "Pavé de Saumon Label Rouge",
                        quantity = 160.0,
                        unit = "g",
                        packPrice = 26.50,
                        packQuantity = 1000.0,
                        packUnit = "g",
                        wastePercentage = 5.0
                    ),
                    RecipeIngredient(
                        name = "Riz Arborio",
                        quantity = 70.0,
                        unit = "g",
                        packPrice = 3.90,
                        packQuantity = 1000.0,
                        packUnit = "g",
                        wastePercentage = 0.0
                    ),
                    RecipeIngredient(
                        name = "Asperges Vertes Fraîches",
                        quantity = 90.0,
                        unit = "g",
                        packPrice = 12.00,
                        packQuantity = 1000.0,
                        packUnit = "g",
                        wastePercentage = 18.0
                    ),
                    RecipeIngredient(
                        name = "Parmigiano Reggiano AOP",
                        quantity = 25.0,
                        unit = "g",
                        packPrice = 24.00,
                        packQuantity = 1000.0,
                        packUnit = "g",
                        wastePercentage = 2.0
                    ),
                    RecipeIngredient(
                        name = "Huile d'Olive Vierge Extra Bio",
                        quantity = 2.0,
                        unit = "cl",
                        packPrice = 12.50,
                        packQuantity = 100.0,
                        packUnit = "cl",
                        wastePercentage = 0.0
                    ),
                    RecipeIngredient(
                        name = "Beurre Doux AOP",
                        quantity = 15.0,
                        unit = "g",
                        packPrice = 3.60,
                        packQuantity = 250.0,
                        packUnit = "g",
                        wastePercentage = 0.0
                    )
                )
            )
        ),
        RecipeEntity(
            name = "Mojito Cubain Signature & Angostura",
            type = RecipeType.COCKTAIL,
            category = "Cocktail Signature",
            portions = 1,
            targetFoodCostPercent = 18.0,
            vatPercent = 20.0,
            sellingPriceTTC = 12.00,
            notes = "Directement au verre tumbler. Feuilles de menthe délicatement pressées sans écraser. Glace pilée abondante et 2 traits d'Angostura sur le dessus.",
            prepTimeMinutes = 4,
            ingredientsJson = RecipeEntity.encodeIngredients(
                listOf(
                    RecipeIngredient(
                        name = "Rhum Blanc Agricole 50°",
                        quantity = 5.0,
                        unit = "cl",
                        packPrice = 22.50,
                        packQuantity = 70.0,
                        packUnit = "cl",
                        wastePercentage = 1.0
                    ),
                    RecipeIngredient(
                        name = "Jus de Citron Vert Frais",
                        quantity = 3.0,
                        unit = "cl",
                        packPrice = 4.80,
                        packQuantity = 100.0,
                        packUnit = "cl",
                        wastePercentage = 5.0
                    ),
                    RecipeIngredient(
                        name = "Sirop de Canne Maison",
                        quantity = 2.0,
                        unit = "cl",
                        packPrice = 3.20,
                        packQuantity = 100.0,
                        packUnit = "cl",
                        wastePercentage = 1.0
                    ),
                    RecipeIngredient(
                        name = "Menthe Fraîche Bouquet",
                        quantity = 8.0,
                        unit = "g",
                        packPrice = 2.50,
                        packQuantity = 100.0,
                        packUnit = "g",
                        wastePercentage = 15.0
                    ),
                    RecipeIngredient(
                        name = "Eau Gazeuse Perrier",
                        quantity = 6.0,
                        unit = "cl",
                        packPrice = 1.20,
                        packQuantity = 100.0,
                        packUnit = "cl",
                        wastePercentage = 2.0
                    ),
                    RecipeIngredient(
                        name = "Bitter Angostura",
                        quantity = 2.0,
                        unit = "dash",
                        packPrice = 14.00,
                        packQuantity = 200.0,
                        packUnit = "dash",
                        wastePercentage = 0.0
                    )
                )
            )
        ),
        RecipeEntity(
            name = "Negroni Spécial Réserve",
            type = RecipeType.COCKTAIL,
            category = "Classique",
            portions = 1,
            targetFoodCostPercent = 18.0,
            vatPercent = 20.0,
            sellingPriceTTC = 13.50,
            notes = "Verre à mélange avec glaçon pur cristal. Stir pendant 30 secondes. Servir sur gros cube de glace dans un verre Old Fashioned. Exprimer le zeste d'orange.",
            prepTimeMinutes = 3,
            ingredientsJson = RecipeEntity.encodeIngredients(
                listOf(
                    RecipeIngredient(
                        name = "Gin Artisanal London Dry",
                        quantity = 3.0,
                        unit = "cl",
                        packPrice = 28.00,
                        packQuantity = 70.0,
                        packUnit = "cl",
                        wastePercentage = 1.0
                    ),
                    RecipeIngredient(
                        name = "Campari Bitter",
                        quantity = 3.0,
                        unit = "cl",
                        packPrice = 17.50,
                        packQuantity = 100.0,
                        packUnit = "cl",
                        wastePercentage = 1.0
                    ),
                    RecipeIngredient(
                        name = "Vermouth Rouge Italien",
                        quantity = 3.0,
                        unit = "cl",
                        packPrice = 15.00,
                        packQuantity = 75.0,
                        packUnit = "cl",
                        wastePercentage = 2.0
                    ),
                    RecipeIngredient(
                        name = "Orange Bio pour Zestes",
                        quantity = 0.25,
                        unit = "pce",
                        packPrice = 0.65,
                        packQuantity = 1.0,
                        packUnit = "pce",
                        wastePercentage = 10.0
                    )
                )
            )
        ),
        RecipeEntity(
            name = "Passion Spritz Sunset",
            type = RecipeType.COCKTAIL,
            category = "Cocktail Signature",
            portions = 1,
            targetFoodCostPercent = 20.0,
            vatPercent = 20.0,
            sellingPriceTTC = 11.50,
            notes = "Verre ballon généreusement rempli de glaçons. Verser la purée de passion, l'Apérol, le Prosecco puis un trait d'eau gazeuse. Remuer délicatement.",
            prepTimeMinutes = 3,
            ingredientsJson = RecipeEntity.encodeIngredients(
                listOf(
                    RecipeIngredient(
                        name = "Prosecco DOC Treviso",
                        quantity = 9.0,
                        unit = "cl",
                        packPrice = 8.50,
                        packQuantity = 75.0,
                        packUnit = "cl",
                        wastePercentage = 3.0
                    ),
                    RecipeIngredient(
                        name = "Apérol",
                        quantity = 6.0,
                        unit = "cl",
                        packPrice = 16.00,
                        packQuantity = 100.0,
                        packUnit = "cl",
                        wastePercentage = 1.0
                    ),
                    RecipeIngredient(
                        name = "Purée Passion Monin / Ravifruit",
                        quantity = 2.0,
                        unit = "cl",
                        packPrice = 11.50,
                        packQuantity = 100.0,
                        packUnit = "cl",
                        wastePercentage = 2.0
                    ),
                    RecipeIngredient(
                        name = "Eau Gazeuse Perrier",
                        quantity = 3.0,
                        unit = "cl",
                        packPrice = 1.20,
                        packQuantity = 100.0,
                        packUnit = "cl",
                        wastePercentage = 2.0
                    )
                )
            )
        )
    )
}
