package com.example.wirelesslocationstud.ui.home

import android.content.Context
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
        textSize = 24f
        isAntiAlias = true
    }

    private val loadingTextPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.text_color)
        style = Paint.Style.FILL
        textSize = 32f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val axisLabelPadding = 40f
    private val gridPadding = 60f

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
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mapCells.isEmpty()) {
            // Draw loading state with helpful message
            canvas.drawText(
                "Loading map data...",
                width / 2f,
                height / 2f - 40f,
                loadingTextPaint
            )

            loadingTextPaint.textSize = 20f
            canvas.drawText(
                "Fetching from API in background",
                width / 2f,
                height / 2f + 20f,
                loadingTextPaint
            )
            loadingTextPaint.textSize = 32f

            Log.d(TAG, "Drawing loading state - map data not yet available")
            return
        }

        // Calculate grid dimensions
        val gridWidth = maxX - minX + 1
        val gridHeight = maxY - minY + 1

        // Calculate available space for grid (excluding padding for axes)
        val availableWidth = width - gridPadding * 2
        val availableHeight = height - gridPadding * 2

        // Calculate cell size to fit the screen without scrolling
        val cellWidth = availableWidth / gridWidth
        val cellHeight = availableHeight / gridHeight
        val cellSize = minOf(cellWidth, cellHeight)

        // Calculate starting position to center the grid
        val startX = gridPadding
        val startY = gridPadding

        // Create a map for quick lookup
        val cellMap = mapCells.associateBy { it.x to it.y }

        var cellsWithStrength = 0
        var cellsWithoutStrength = 0

        // Draw grid cells
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                val cell = cellMap[x to y]

                // Determine cell color based on whether it has strength data
                val paint = if (cell != null && hasStrength(cell)) {
                    cellsWithStrength++
                    cellWithStrengthPaint
                } else {
                    cellsWithoutStrength++
                    cellWithoutStrengthPaint
                }

                // Calculate cell position (invert Y axis so it increases upward)
                val left = startX + (x - minX) * cellSize
                val top = startY + (maxY - y) * cellSize
                val right = left + cellSize
                val bottom = top + cellSize

                // Draw cell
                canvas.drawRect(left, top, right, bottom, paint)

                // Draw cell border
                canvas.drawRect(left, top, right, bottom, gridLinePaint)
            }
        }

        Log.d(TAG, "Drew grid: $cellsWithStrength cells with strength (green), $cellsWithoutStrength cells without strength (red)")

        // Draw X-axis labels (bottom)
        for (x in minX..maxX) {
            val xPos = startX + (x - minX) * cellSize + cellSize / 2
            val yPos = height - gridPadding / 2
            canvas.drawText(x.toString(), xPos - 10f, yPos, axisPaint)
        }

        // Draw Y-axis labels (left)
        for (y in minY..maxY) {
            val xPos = gridPadding / 2
            val yPos = startY + (maxY - y) * cellSize + cellSize / 2 + 8f
            canvas.drawText(y.toString(), xPos - 10f, yPos, axisPaint)
        }

        // Draw axis titles
        axisPaint.textSize = 28f
        canvas.drawText("X", width / 2f - 10f, height - 10f, axisPaint)
        canvas.save()
        canvas.rotate(-90f, 20f, height / 2f)
        canvas.drawText("Y", 20f, height / 2f, axisPaint)
        canvas.restore()
        axisPaint.textSize = 24f
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
