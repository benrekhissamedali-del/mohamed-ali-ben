package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.FoodCostHealth
import com.example.data.model.RecipeEntity
import com.example.data.model.RecipeType
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    /**
     * Generates a high-quality, professional A4 technical sheet PDF for the given recipe.
     * Returns the generated File in the app's cache directory.
     */
    fun generateRecipePdf(context: Context, recipe: RecipeEntity): File {
        val fichesDir = File(context.cacheDir, "fiches").apply {
            if (!exists()) mkdirs()
        }
        val safeName = recipe.name.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
        val pdfFile = File(fichesDir, "Fiche_${safeName}_${recipe.id}.pdf")

        val document = PdfDocument()
        val financials = recipe.computeFinancials()
        val ingredients = recipe.parseIngredients()
        val isDish = recipe.type == RecipeType.PLAT

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

        // Palette
        val colorPrimary = Color.rgb(15, 76, 129) // Deep Slate Blue / Navy
        val colorDark = Color.rgb(30, 41, 59)
        val colorGray = Color.rgb(100, 116, 139)
        val colorLightGray = Color.rgb(241, 245, 249)
        val colorZebra = Color.rgb(248, 250, 252)
        val colorBorder = Color.rgb(226, 232, 240)
        val colorSuccess = Color.rgb(22, 101, 52)
        val colorWarning = Color.rgb(180, 83, 9)
        val colorDanger = Color.rgb(185, 28, 28)

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        var y = MARGIN

        // --- 1. HEADER BRANDING ---
        // Header background ribbon
        paint.color = colorPrimary
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + 46f), 6f, 6f, paint)

        // Title in banner
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        canvas.drawText("FICHE TECHNIQUE DE FABRICATION & RENTABILITÉ", MARGIN + 14f, y + 22f, paint)

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("FoodCost Pro · Contrôle de Gestion Gastronomique & Bar", MARGIN + 14f, y + 36f, paint)

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date())
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Édition : $dateStr", MARGIN + CONTENT_WIDTH - 14f, y + 28f, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 56f

        // --- 2. RECIPE IDENTITY CARD ---
        val cardHeight = 72f
        paint.color = colorLightGray
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + cardHeight), 6f, 6f, paint)

        paint.color = colorBorder
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + cardHeight), 6f, 6f, paint)

        // Recipe Name
        paint.style = Paint.Style.FILL
        paint.color = colorDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        canvas.drawText(recipe.name, MARGIN + 14f, y + 24f, paint)

        // Tag Type & Category
        val typeBadgeText = if (isDish) "🍽️ PLAT CUISINE" else "🍸 COCKTAIL BAR"
        val categoryText = "$typeBadgeText  |  Catégorie : ${recipe.category}"
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = colorPrimary
        canvas.drawText(categoryText, MARGIN + 14f, y + 42f, paint)

        // Meta info line
        paint.color = colorGray
        paint.textSize = 9.5f
        val prepText = if (recipe.prepTimeMinutes > 0) " · Temps prép : ${recipe.prepTimeMinutes} min" else ""
        val metaLine = "Rendement : ${recipe.portions} ${if (isDish) "portion(s)" else "verre(s)"}$prepText · TVA applicable : ${(financials.vatRate * 100).toInt()}%"
        canvas.drawText(metaLine, MARGIN + 14f, y + 58f, paint)

        // Food Cost Badge on right side
        val badgeWidth = 140f
        val badgeX = MARGIN + CONTENT_WIDTH - badgeWidth - 14f
        val badgeY = y + 14f
        val badgeHeight = 44f
        val (healthBg, healthFg, healthLabel) = when (financials.health) {
            FoodCostHealth.EXCELLENT -> Triple(Color.rgb(220, 252, 231), colorSuccess, "EXCELLENT")
            FoodCostHealth.BON -> Triple(Color.rgb(220, 252, 231), colorSuccess, "CONFORME")
            FoodCostHealth.ATTENTION -> Triple(Color.rgb(254, 243, 199), colorWarning, "ATTENTION")
            FoodCostHealth.CRITIQUE -> Triple(Color.rgb(254, 226, 226), colorDanger, "CRITIQUE")
        }

        paint.color = healthBg
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(badgeX, badgeY, badgeX + badgeWidth, badgeY + badgeHeight), 4f, 4f, paint)

        paint.color = healthFg
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(badgeX, badgeY, badgeX + badgeWidth, badgeY + badgeHeight), 4f, 4f, paint)

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        canvas.drawText("RATIO COÛT MATIÈRE", badgeX + (badgeWidth / 2), badgeY + 14f, paint)

        paint.textSize = 13f
        val ratioText = String.format(Locale.FRANCE, "%.1f %%  (%s)", financials.actualFoodCostPercent, healthLabel)
        canvas.drawText(ratioText, badgeX + (badgeWidth / 2), badgeY + 30f, paint)

        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(String.format(Locale.FRANCE, "Cible fixée : %.1f %%", financials.targetFoodCostPercent), badgeX + (badgeWidth / 2), badgeY + 40f, paint)

        paint.textAlign = Paint.Align.LEFT
        y += cardHeight + 12f

        // --- 3. FINANCIAL SUMMARY GRID (6 KPIs) ---
        val kpiCols = 3
        val kpiWidth = (CONTENT_WIDTH - 12f) / kpiCols
        val kpiHeight = 44f

        val kpiData = listOf(
            Triple("Coût Matière / Portion", String.format(Locale.FRANCE, "%.2f €", financials.costPerPortion), colorPrimary),
            Triple("Prix Vente TTC Fixé", if (financials.sellingPriceTTC > 0) String.format(Locale.FRANCE, "%.2f €", financials.sellingPriceTTC) else "-- €", colorDark),
            Triple("Marge Brute / Portion", if (financials.grossMarginPerPortion != 0.0) String.format(Locale.FRANCE, "%.2f €", financials.grossMarginPerPortion) else "-- €", colorSuccess),
            Triple("Prix Conseillé TTC", String.format(Locale.FRANCE, "%.2f €", financials.recommendedSellingPriceTTC), colorPrimary),
            Triple("Prix Vente HT Fixé", if (financials.sellingPriceHT > 0) String.format(Locale.FRANCE, "%.2f €", financials.sellingPriceHT) else "-- €", colorDark),
            Triple("Coeff. Multiplicateur", if (financials.multiplierCoefficientTTC > 0) String.format(Locale.FRANCE, "x %.2f", financials.multiplierCoefficientTTC) else "--", colorDark)
        )

        for (i in kpiData.indices) {
            val col = i % kpiCols
            val row = i / kpiCols
            val kx = MARGIN + col * (kpiWidth + 6f)
            val ky = y + row * (kpiHeight + 6f)
            val (kTitle, kValue, kColor) = kpiData[i]

            paint.color = colorLightGray
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(RectF(kx, ky, kx + kpiWidth, ky + kpiHeight), 4f, 4f, paint)

            paint.color = colorBorder
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawRoundRect(RectF(kx, ky, kx + kpiWidth, ky + kpiHeight), 4f, 4f, paint)

            paint.style = Paint.Style.FILL
            paint.color = colorGray
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f
            canvas.drawText(kTitle, kx + 8f, ky + 14f, paint)

            paint.color = kColor
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 13f
            canvas.drawText(kValue, kx + 8f, ky + 34f, paint)
        }

        y += (kpiHeight * 2) + 18f

        // --- 4. INGREDIENTS TABLE ---
        paint.color = colorDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("MERCURIALE & DÉCOMPTE DES COÛTS MATIÈRE (${ingredients.size} ingrédients)", MARGIN, y, paint)
        y += 8f

        // Table Column specifications (Total = 523)
        // Ingrédient (163), Qté (45), Unité (40), Prix Achat (65), Contenance (65), Perte % (45), Coût Revient (55), % Coût (45)
        val colWidths = floatArrayOf(163f, 45f, 40f, 65f, 65f, 45f, 55f, 45f)
        val colHeaders = arrayOf("Ingrédient", "Quantité", "Unité", "Prix Achat", "Format Achat", "Perte %", "Coût Ligne", "% Total")
        val colAlignRight = booleanArrayOf(false, true, false, true, false, true, true, true)

        // Draw Table Header
        val tableHeaderHeight = 20f
        paint.color = colorDark
        paint.style = Paint.Style.FILL
        canvas.drawRect(MARGIN, y, MARGIN + CONTENT_WIDTH, y + tableHeaderHeight, paint)

        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        var curX = MARGIN
        for (c in colHeaders.indices) {
            val w = colWidths[c]
            val textX = if (colAlignRight[c]) curX + w - 4f else curX + 4f
            if (colAlignRight[c]) paint.textAlign = Paint.Align.RIGHT else paint.textAlign = Paint.Align.LEFT
            canvas.drawText(colHeaders[c], textX, y + 13f, paint)
            curX += w
        }
        paint.textAlign = Paint.Align.LEFT
        y += tableHeaderHeight

        val rowHeight = 18f
        paint.textSize = 8.5f

        for (idx in ingredients.indices) {
            val ing = ingredients[idx]
            val lineCost = ing.calculateCost()
            val costPercent = if (financials.totalCost > 0) (lineCost / financials.totalCost) * 100.0 else 0.0

            // Check if page overflow
            if (y + rowHeight + 100f > PAGE_HEIGHT - MARGIN) {
                // Finish current page and start next page
                drawFooter(canvas, pageNumber)
                document.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN + 20f

                // Re-draw table header on new page
                paint.color = colorDark
                paint.style = Paint.Style.FILL
                canvas.drawRect(MARGIN, y, MARGIN + CONTENT_WIDTH, y + tableHeaderHeight, paint)
                paint.color = Color.WHITE
                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                curX = MARGIN
                for (c in colHeaders.indices) {
                    val w = colWidths[c]
                    val textX = if (colAlignRight[c]) curX + w - 4f else curX + 4f
                    if (colAlignRight[c]) paint.textAlign = Paint.Align.RIGHT else paint.textAlign = Paint.Align.LEFT
                    canvas.drawText(colHeaders[c], textX, y + 13f, paint)
                    curX += w
                }
                paint.textAlign = Paint.Align.LEFT
                y += tableHeaderHeight
            }

            // Zebra background
            paint.color = if (idx % 2 == 0) Color.WHITE else colorZebra
            paint.style = Paint.Style.FILL
            canvas.drawRect(MARGIN, y, MARGIN + CONTENT_WIDTH, y + rowHeight, paint)

            // Border line
            paint.color = colorBorder
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawLine(MARGIN, y + rowHeight, MARGIN + CONTENT_WIDTH, y + rowHeight, paint)

            // Row text
            paint.style = Paint.Style.FILL
            paint.color = colorDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            val values = arrayOf(
                ing.name.take(28),
                String.format(Locale.FRANCE, "%.1f", ing.quantity),
                ing.unit,
                String.format(Locale.FRANCE, "%.2f €", ing.packPrice),
                "${String.format(Locale.FRANCE, "%.1f", ing.packQuantity)} ${ing.packUnit}",
                if (ing.wastePercentage > 0) "${ing.wastePercentage.toInt()} %" else "0 %",
                String.format(Locale.FRANCE, "%.2f €", lineCost),
                String.format(Locale.FRANCE, "%.1f %%", costPercent)
            )

            curX = MARGIN
            for (c in values.indices) {
                val w = colWidths[c]
                val textX = if (colAlignRight[c]) curX + w - 4f else curX + 4f
                if (colAlignRight[c]) paint.textAlign = Paint.Align.RIGHT else paint.textAlign = Paint.Align.LEFT

                // Highlight ingredient name or line cost
                if (c == 0) paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                else if (c == 6) {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = colorPrimary
                } else {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = colorDark
                }

                canvas.drawText(values[c], textX, y + 12f, paint)
                curX += w
            }
            paint.textAlign = Paint.Align.LEFT
            y += rowHeight
        }

        // Table Total Row
        paint.color = colorLightGray
        paint.style = Paint.Style.FILL
        canvas.drawRect(MARGIN, y, MARGIN + CONTENT_WIDTH, y + 22f, paint)

        paint.color = colorDark
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(MARGIN, y, MARGIN + CONTENT_WIDTH, y + 22f, paint)

        paint.style = Paint.Style.FILL
        paint.color = colorDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        canvas.drawText("TOTAL COÛT MATIÈRE RECETTE (${recipe.portions} ${if (isDish) "portions" else "verres"})", MARGIN + 8f, y + 14f, paint)

        paint.color = colorPrimary
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 10f
        val totalCostText = String.format(Locale.FRANCE, "%.2f €  (%.2f € / portion)", financials.totalCost, financials.costPerPortion)
        canvas.drawText(totalCostText, MARGIN + CONTENT_WIDTH - 8f, y + 14f, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 32f

        // --- 5. NOTES & DRESSING / SERVICE SECTION ---
        if (recipe.notes.isNotBlank() && y + 90f < PAGE_HEIGHT - MARGIN) {
            paint.color = colorDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            canvas.drawText("CONSIGNES DE FABRICATION, DRESSAGE & SERVICE", MARGIN, y, paint)
            y += 8f

            textPaint.color = colorDark
            textPaint.textSize = 9f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            val notesWidth = CONTENT_WIDTH.toInt() - 20
            val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(recipe.notes, 0, recipe.notes.length, textPaint, notesWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1.2f)
                    .setIncludePad(false)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(recipe.notes, textPaint, notesWidth, Layout.Alignment.ALIGN_NORMAL, 1.2f, 0f, false)
            }

            val boxHeight = (staticLayout.height + 16f).coerceAtMost(100f)

            paint.color = colorLightGray
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + boxHeight), 4f, 4f, paint)

            paint.color = colorBorder
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + boxHeight), 4f, 4f, paint)

            canvas.save()
            canvas.translate(MARGIN + 10f, y + 8f)
            staticLayout.draw(canvas)
            canvas.restore()

            y += boxHeight + 14f
        }

        // --- 6. VALIDATION & SIGNATURE BOXES ---
        if (y + 50f < PAGE_HEIGHT - MARGIN - 25f) {
            val sigBoxWidth = (CONTENT_WIDTH - 20f) / 2
            val sigBoxHeight = 44f

            // Box 1: Chef / Bar Manager
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + sigBoxWidth, y + sigBoxHeight), 4f, 4f, paint)
            paint.color = colorBorder
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + sigBoxWidth, y + sigBoxHeight), 4f, 4f, paint)

            paint.style = Paint.Style.FILL
            paint.color = colorGray
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("VISA CHEF DE CUISINE / BAR MANAGER", MARGIN + 8f, y + 13f, paint)

            // Box 2: Direction / Contrôle
            val sig2X = MARGIN + sigBoxWidth + 20f
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(RectF(sig2X, y, sig2X + sigBoxWidth, y + sigBoxHeight), 4f, 4f, paint)
            paint.color = colorBorder
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawRoundRect(RectF(sig2X, y, sig2X + sigBoxWidth, y + sigBoxHeight), 4f, 4f, paint)

            paint.style = Paint.Style.FILL
            paint.color = colorGray
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("VISA CONTRÔLE DE GESTION / DIRECTION", sig2X + 8f, y + 13f, paint)
        }

        // Draw Footer on final page
        drawFooter(canvas, pageNumber)
        document.finishPage(page)

        // Write to output file
        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    private fun drawFooter(canvas: Canvas, pageNumber: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(148, 163, 184)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.5f
        canvas.drawLine(MARGIN, PAGE_HEIGHT - MARGIN - 12f, MARGIN + CONTENT_WIDTH, PAGE_HEIGHT - MARGIN - 12f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("FoodCost Pro · Fiche technique officielle d'exploitation · Document réservé à l'usage interne", MARGIN, PAGE_HEIGHT - MARGIN, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page $pageNumber", MARGIN + CONTENT_WIDTH, PAGE_HEIGHT - MARGIN, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    /**
     * Directly launches Android PrintManager dialog to print or save the PDF.
     */
    fun printPdf(context: Context, pdfFile: File, jobName: String): Boolean {
        return try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) return false

            val adapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    metadata: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder("${pdfFile.name}")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build()
                    callback?.onLayoutFinished(info, newAttributes != oldAttributes)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    try {
                        FileInputStream(pdfFile).use { input ->
                            FileOutputStream(destination?.fileDescriptor).use { output ->
                                input.copyTo(output)
                            }
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            }

            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build()

            printManager.print(jobName, adapter, printAttributes)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Opens Android system share sheet with the PDF file.
     */
    fun sharePdf(context: Context, pdfFile: File, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Partager la fiche technique PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Opens the generated PDF in an external viewer.
     */
    fun viewPdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(viewIntent, "Ouvrir la fiche technique PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
