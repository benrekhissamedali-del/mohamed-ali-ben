package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinancialAnalysis
import com.example.data.model.FoodCostHealth
import com.example.data.model.RecipeType
import com.example.ui.theme.ColorHealthCritical
import com.example.ui.theme.ColorHealthExcellent
import com.example.ui.theme.ColorHealthGood
import com.example.ui.theme.ColorHealthWarning
import java.util.Locale

fun formatCurrency(amount: Double): String {
    return String.format(Locale.FRANCE, "%.2f €", amount)
}

fun formatPercent(percent: Double): String {
    return String.format(Locale.FRANCE, "%.1f %%", percent)
}

@Composable
fun FoodCostBadge(
    actualPercent: Double,
    targetPercent: Double,
    health: FoodCostHealth,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (health) {
        FoodCostHealth.EXCELLENT -> Triple(
            ColorHealthExcellent.copy(alpha = 0.15f),
            ColorHealthExcellent,
            Icons.Default.CheckCircle
        )
        FoodCostHealth.BON -> Triple(
            ColorHealthGood.copy(alpha = 0.15f),
            ColorHealthGood,
            Icons.Default.CheckCircle
        )
        FoodCostHealth.ATTENTION -> Triple(
            ColorHealthWarning.copy(alpha = 0.18f),
            ColorHealthWarning,
            Icons.Default.Warning
        )
        FoodCostHealth.CRITIQUE -> Triple(
            ColorHealthCritical.copy(alpha = 0.15f),
            ColorHealthCritical,
            Icons.Default.Info
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "FC ${formatPercent(actualPercent)}",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = "(cible ${formatPercent(targetPercent)})",
                color = textColor.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun RecipeTypeTag(
    type: RecipeType,
    category: String,
    modifier: Modifier = Modifier
) {
    val isDish = type == RecipeType.PLAT
    val tintColor = if (isDish) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val containerColor = if (isDish) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(containerColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isDish) Icons.Default.Restaurant else Icons.Default.LocalBar,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = category.ifBlank { type.label },
            color = tintColor,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
        )
    }
}

@Composable
fun FinancialKpiGrid(
    financials: FinancialAnalysis,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiMiniCard(
                title = "Coût Matière / Portion",
                value = formatCurrency(financials.costPerPortion),
                subtitle = "Total: ${formatCurrency(financials.totalCost)}",
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f).testTag("kpi_cost_per_portion")
            )
            KpiMiniCard(
                title = "Prix de Vente TTC",
                value = formatCurrency(financials.sellingPriceTTC),
                subtitle = "HT: ${formatCurrency(financials.sellingPriceHT)} (TVA ${(financials.vatRate * 100).toInt()}%)",
                accentColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f).testTag("kpi_selling_price")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val marginHealthColor = if (financials.grossMarginPerPortion > 0) ColorHealthExcellent else ColorHealthCritical
            KpiMiniCard(
                title = "Marge Brute / Portion",
                value = formatCurrency(financials.grossMarginPerPortion),
                subtitle = "Taux de marge: ${if (financials.sellingPriceHT > 0) formatPercent((financials.grossMarginPerPortion / financials.sellingPriceHT) * 100) else "0%"}",
                accentColor = marginHealthColor,
                modifier = Modifier.weight(1f).testTag("kpi_gross_margin")
            )
            KpiMiniCard(
                title = "Coeff. Multiplicateur",
                value = if (financials.multiplierCoefficientTTC > 0) String.format(Locale.FRANCE, "x %.2f", financials.multiplierCoefficientTTC) else "x 0",
                subtitle = "TTC / Coût matière",
                accentColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f).testTag("kpi_multiplier")
            )
        }
    }
}

@Composable
fun KpiMiniCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
