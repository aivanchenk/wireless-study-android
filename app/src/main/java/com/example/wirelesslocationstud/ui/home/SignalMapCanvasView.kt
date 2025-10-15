package com.example.wirelesslocationstud.ui.home

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.wirelesslocationstud.R
import com.example.wirelesslocationstud.data.local.entity.MapCellEntity

class SignalMapCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "SignalMapCanvasView"
    }

    private var mapCells: List<MapCellEntity> = emptyList()
    private var minX: Int = 0
    private var maxX: Int = 0
    private var minY: Int = 0
    private var maxY: Int = 0

    private val cellWithStrengthPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.cell_with_strength)
        style = Paint.Style.FILL
    }

    private val cellWithoutStrengthPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.cell_without_strength)
        style = Paint.Style.FILL
    }

    private val gridLinePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.grid_line_color)
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val axisPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.axis_color)
        style = Paint.Style.FILL
        textSize = 20f
        isAntiAlias = true
    }

    private val loadingTextPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.text_color)
        style = Paint.Style.FILL
        textSize = 28f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val gridPaddingTop = 40f
    private val gridPaddingBottom = 50f
    private val gridPaddingLeft = 40f
    private val gridPaddingRight = 20f

    // Cell size for scrollable view
    private val minCellSize = 80f // Minimum size to make scrolling useful

    /**
     * Check if device is in portrait orientation
     */
    private fun isPortrait(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    /**
     * Update the map data to be displayed
     */
    fun setMapData(cells: List<MapCellEntity>) {
        if (cells.isEmpty()) {
            Log.d(TAG, "Map data is empty - waiting for background sync to complete")
            mapCells = emptyList()
            invalidate()
            return
        }

        mapCells = cells
        minX = cells.minOf { it.x }
        maxX = cells.maxOf { it.x }
        minY = cells.minOf { it.y }
        maxY = cells.maxOf { it.y }

        Log.d(TAG, "Map data loaded: ${cells.size} cells, X range: [$minX, $maxX], Y range: [$minY, $maxY]")
        Log.d(TAG, "Orientation: ${if (isPortrait()) "Portrait" else "Landscape"}")

        // Request layout to recalculate dimensions
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mapCells.isEmpty()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val portrait = isPortrait()

        val gridWidth: Int
        val gridHeight: Int

        if (portrait) {
            gridWidth = maxX - minX + 1
            gridHeight = maxY - minY + 1
        } else {
            gridWidth = maxY - minY + 1
            gridHeight = maxX - minX + 1
        }

        // Get screen dimensions
        val screenWidth = MeasureSpec.getSize(widthMeasureSpec)
        val screenHeight = MeasureSpec.getSize(heightMeasureSpec)

        val totalWidth: Int
        val totalHeight: Int

        if (portrait) {
            // Portrait: width based on screen width / number of cells
            //          height: use larger cell size to enable scrolling
            val cellWidth = (screenWidth - gridPaddingLeft - gridPaddingRight) / gridWidth
            // Make cells larger to enable scrolling - use minimum 50dp per cell
            val minCellHeight = 50f
            val cellHeight = maxOf(cellWidth / 2f, minCellHeight)

            totalWidth = (gridWidth * cellWidth + gridPaddingLeft + gridPaddingRight).toInt()
            totalHeight = (gridHeight * cellHeight + gridPaddingTop + gridPaddingBottom).toInt()

            Log.d(TAG, "Portrait: cellWidth=$cellWidth, cellHeight=$cellHeight, total canvas height=$totalHeight, screen height=$screenHeight")
        } else {
            // Landscape: height based on available screen height divided by number of cells
            //           width based on screen width divided by number of cells
            val cellHeight = (screenHeight - gridPaddingTop - gridPaddingBottom) / gridHeight
            val cellWidth = (screenWidth - gridPaddingLeft - gridPaddingRight) / gridWidth
            totalWidth = (gridWidth * cellWidth + gridPaddingLeft + gridPaddingRight).toInt()
            totalHeight = (gridHeight * cellHeight + gridPaddingTop + gridPaddingBottom).toInt()

            Log.d(TAG, "Landscape: cellWidth=$cellWidth, cellHeight=$cellHeight")
        }

        Log.d(TAG, "Canvas size: ${totalWidth}x${totalHeight}, Scrollable: ${portrait && totalHeight > screenHeight}")

        setMeasuredDimension(totalWidth, totalHeight)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        // Redraw when orientation changes
        Log.d(TAG, "Orientation changed, redrawing canvas")
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mapCells.isEmpty()) {
            // Draw loading state with helpful message
            canvas.drawText(
                "Loading map data...",
                width / 2f,
                height / 2f - 30f,
                loadingTextPaint
            )

            loadingTextPaint.textSize = 18f
            canvas.drawText(
                "Fetching from API in background",
                width / 2f,
                height / 2f + 10f,
                loadingTextPaint
            )
            loadingTextPaint.textSize = 28f

            Log.d(TAG, "Drawing loading state - map data not yet available")
            return
        }

        val portrait = isPortrait()

        // In landscape mode, swap X and Y axes
        val gridWidth: Int
        val gridHeight: Int

        if (portrait) {
            // Portrait: Normal X horizontal, Y vertical
            gridWidth = maxX - minX + 1
            gridHeight = maxY - minY + 1
            Log.d(TAG, "Portrait mode: Normal axes - Grid: ${gridWidth}x${gridHeight}")
        } else {
            // Landscape: Y becomes horizontal (width), X becomes vertical (height)
            gridWidth = maxY - minY + 1
            gridHeight = maxX - minX + 1
            Log.d(TAG, "Landscape mode: Swapping axes - Grid: ${gridWidth}x${gridHeight}")
        }

        // Calculate available space for grid (excluding padding for axes)
        val availableWidth = width - gridPaddingLeft - gridPaddingRight
        val availableHeight = height - gridPaddingTop - gridPaddingBottom

        // RESPONSIVE: Cell width fills the screen width, height adjusts to fit
        val cellWidth = availableWidth / gridWidth
        val cellHeight = availableHeight / gridHeight

        Log.d(TAG, "Cell dimensions: width=$cellWidth, height=$cellHeight")

        // Calculate starting position
        val startX = gridPaddingLeft
        val startY = gridPaddingTop

        // Create a map for quick lookup
        val cellMap = mapCells.associateBy { it.x to it.y }

        var cellsWithStrength = 0
        var cellsWithoutStrength = 0

        // Draw grid cells
        if (portrait) {
            // Portrait: X is horizontal, Y is vertical (normal)
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    val cell = cellMap[x to y]

                    val paint = if (cell != null && hasStrength(cell)) {
                        cellsWithStrength++
                        cellWithStrengthPaint
                    } else {
                        cellsWithoutStrength++
                        cellWithoutStrengthPaint
                    }

                    // Calculate cell position (invert Y axis so it increases upward)
                    val left = startX + (x - minX) * cellWidth
                    val top = startY + (maxY - y) * cellHeight
                    val right = left + cellWidth
                    val bottom = top + cellHeight

                    // Draw cell
                    canvas.drawRect(left, top, right, bottom, paint)
                    canvas.drawRect(left, top, right, bottom, gridLinePaint)
                }
            }

            // Draw X-axis labels (bottom)
            axisPaint.textSize = 14f
            for (x in minX..maxX) {
                val xPos = startX + (x - minX) * cellWidth + cellWidth / 2
                val yPos = height - 15f
                canvas.drawText(x.toString(), xPos - 8f, yPos, axisPaint)
            }

            // Draw Y-axis labels (left)
            for (y in minY..maxY) {
                val xPos = 15f
                val yPos = startY + (maxY - y) * cellHeight + cellHeight / 2 + 6f
                canvas.drawText(y.toString(), xPos, yPos, axisPaint)
            }

            // Draw axis titles
            axisPaint.textSize = 20f
            canvas.drawText("X", width / 2f - 8f, height - 5f, axisPaint)
            canvas.save()
            canvas.rotate(-90f, 10f, height / 2f)
            canvas.drawText("Y", 10f, height / 2f, axisPaint)
            canvas.restore()

        } else {
            // Landscape: Y is horizontal (columns), X is vertical (rows)
            for (yVal in minY..maxY) {
                for (xVal in minX..maxX) {
                    val cell = cellMap[xVal to yVal]

                    val paint = if (cell != null && hasStrength(cell)) {
                        cellsWithStrength++
                        cellWithStrengthPaint
                    } else {
                        cellsWithoutStrength++
                        cellWithoutStrengthPaint
                    }

                    // Calculate cell position
                    val left = startX + (yVal - minY) * cellWidth
                    val top = startY + (maxX - xVal) * cellHeight
                    val right = left + cellWidth
                    val bottom = top + cellHeight

                    // Draw cell
                    canvas.drawRect(left, top, right, bottom, paint)
                    canvas.drawRect(left, top, right, bottom, gridLinePaint)
                }
            }

            // Draw Y-axis labels (bottom - now horizontal)
            axisPaint.textSize = 14f
            for (yVal in minY..maxY) {
                val xPos = startX + (yVal - minY) * cellWidth + cellWidth / 2
                val yPos = height - 15f
                canvas.drawText(yVal.toString(), xPos - 8f, yPos, axisPaint)
            }

            // Draw X-axis labels (left - now vertical)
            for (xVal in minX..maxX) {
                val xPos = 15f
                val yPos = startY + (maxX - xVal) * cellHeight + cellHeight / 2 + 6f
                canvas.drawText(xVal.toString(), xPos, yPos, axisPaint)
            }

            // Draw axis titles
            axisPaint.textSize = 20f
            canvas.drawText("Y", width / 2f - 8f, height - 5f, axisPaint)
            canvas.save()
            canvas.rotate(-90f, 10f, height / 2f)
            canvas.drawText("X", 10f, height / 2f, axisPaint)
            canvas.restore()
        }

        Log.d(TAG, "Drew grid: $cellsWithStrength cells with strength (green), $cellsWithoutStrength cells without strength (red)")
    }

    /**
     * Check if a cell has any non-zero strength values
     */
    private fun hasStrength(cell: MapCellEntity): Boolean {
        return (cell.strength1 ?: 0) > 0 ||
               (cell.strength2 ?: 0) > 0 ||
               (cell.strength3 ?: 0) > 0
    }
}
