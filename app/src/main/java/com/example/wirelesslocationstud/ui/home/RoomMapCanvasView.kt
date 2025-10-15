package com.example.wirelesslocationstud.ui.home

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.wirelesslocationstud.R

/**
 * Canvas view that displays a floor plan with rooms and corridors
 * Responsive to orientation changes and scrollable
 */
class RoomMapCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "RoomMapCanvasView"
    }

    // Room data structure
    data class Room(
        val id: String,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val color: Int,
        val isRoom: Boolean = true // false for corridors
    )

    private val wallPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val roomPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 24f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val corridorColor = Color.parseColor("#87CEEB") // Light blue for corridors
    private val roomColors = listOf(
        Color.parseColor("#E6F3FF"), // Light blue
        Color.parseColor("#FFE6E6"), // Light pink
        Color.parseColor("#E6FFE6"), // Light green
        Color.parseColor("#FFF5E6"), // Light orange
        Color.parseColor("#F0E6FF"), // Light purple
        Color.parseColor("#FFFACD")  // Light yellow
    )

    // Define the floor plan based on the image
    private val rooms = mutableListOf<Room>()

    init {
        setupFloorPlan()
    }

    /**
     * Check if device is in portrait orientation
     */
    private fun isPortrait(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    private fun setupFloorPlan() {
        // Portrait layout (default)
        // Left column of rooms (stacked vertically)
        rooms.add(Room("401A", 0.05f, 0.02f, 0.15f, 0.07f, roomColors[0]))
        rooms.add(Room("401B", 0.05f, 0.10f, 0.15f, 0.07f, roomColors[1]))
        rooms.add(Room("401C", 0.05f, 0.18f, 0.15f, 0.07f, roomColors[2]))
        rooms.add(Room("402", 0.05f, 0.26f, 0.15f, 0.07f, roomColors[3]))
        rooms.add(Room("403", 0.05f, 0.34f, 0.15f, 0.07f, roomColors[4]))
        rooms.add(Room("404", 0.05f, 0.42f, 0.15f, 0.07f, roomColors[5]))
        rooms.add(Room("405", 0.05f, 0.50f, 0.15f, 0.07f, roomColors[0]))
        rooms.add(Room("406", 0.05f, 0.58f, 0.15f, 0.07f, roomColors[1]))
        rooms.add(Room("407", 0.05f, 0.66f, 0.15f, 0.07f, roomColors[2]))

        // Top corridor extension - extends RIGHT to cover the top (where 408 would be)
        rooms.add(Room("CorridorTop", 0.21f, 0.02f, 0.34f, 0.07f, corridorColor, false))

        // Central corridor - TALL vertical corridor in the middle
        rooms.add(Room("Corridor", 0.21f, 0.10f, 0.18f, 0.64f, corridorColor, false))

        // Right column of rooms (stacked vertically) - starting BELOW the top corridor
        rooms.add(Room("409", 0.40f, 0.10f, 0.15f, 0.07f, roomColors[4]))
        rooms.add(Room("410", 0.40f, 0.18f, 0.15f, 0.07f, roomColors[5]))
        rooms.add(Room("411", 0.40f, 0.26f, 0.15f, 0.07f, roomColors[0]))
        rooms.add(Room("412", 0.40f, 0.34f, 0.15f, 0.07f, roomColors[1]))
        rooms.add(Room("413", 0.40f, 0.42f, 0.15f, 0.07f, roomColors[2]))
        rooms.add(Room("414", 0.40f, 0.50f, 0.15f, 0.07f, roomColors[3]))
        rooms.add(Room("415", 0.40f, 0.58f, 0.15f, 0.07f, roomColors[4]))
        rooms.add(Room("416", 0.40f, 0.66f, 0.15f, 0.07f, roomColors[5]))

        // Bottom row - far right rooms (horizontal layout)
        rooms.add(Room("417", 0.40f, 0.74f, 0.15f, 0.10f, roomColors[0]))
        rooms.add(Room("418", 0.56f, 0.74f, 0.15f, 0.10f, roomColors[1]))

        // Bottom LEFT corridor extension - extends LEFT from the central corridor (below 407)
        rooms.add(Room("CorridorBottomLeft", 0.05f, 0.74f, 0.16f, 0.10f, corridorColor, false))

        // Bottom CENTER corridor extension - continues from central corridor (does NOT extend right)
        rooms.add(Room("CorridorBottom", 0.21f, 0.74f, 0.18f, 0.10f, corridorColor, false))

        // Top right corner room (408) - above the corridor
        rooms.add(Room("408", 0.56f, 0.02f, 0.15f, 0.07f, roomColors[3]))

        Log.d(TAG, "Floor plan initialized with ${rooms.size} rooms")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Get screen dimensions
        val screenWidth = MeasureSpec.getSize(widthMeasureSpec)
        val screenHeight = MeasureSpec.getSize(heightMeasureSpec)

        val portrait = isPortrait()

        // Make the canvas larger to enable scrolling, similar to SignalMapCanvasView
        val totalWidth: Int
        val totalHeight: Int

        if (portrait) {
            // Portrait: make it scrollable vertically
            // Use screen width as base, calculate height based on aspect ratio
            // Floor plan aspect ratio is roughly 0.6 width : 1.0 height
            totalWidth = screenWidth
            // Make height large enough to show detail and enable scrolling
            totalHeight = (screenWidth * 1.8f).toInt() // Taller canvas for better room visibility
        } else {
            // Landscape: swap axes - floor plan rotates 90 degrees
            // Make it wider to accommodate the rotated layout
            totalHeight = (screenHeight * 0.9f).toInt() // Use most of screen height
            totalWidth = (totalHeight * 1.8f).toInt() // Width proportional to height for rotated view
        }

        Log.d(TAG, "Canvas size: ${totalWidth}x${totalHeight}, orientation: ${if (portrait) "Portrait" else "Landscape"}, screen: ${screenWidth}x${screenHeight}")

        setMeasuredDimension(totalWidth, totalHeight)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "Orientation changed, redrawing room canvas")
        requestLayout()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val canvasWidth = width.toFloat()
        val canvasHeight = height.toFloat()
        val portrait = isPortrait()

        Log.d(TAG, "Drawing floor plan, canvas size: ${canvasWidth}x${canvasHeight}, orientation: ${if (portrait) "Portrait" else "Landscape"}")

        // Draw each room
        for (room in rooms) {
            val rect: RectF

            if (portrait) {
                // Portrait: Normal orientation
                rect = RectF(
                    room.x * canvasWidth,
                    room.y * canvasHeight,
                    (room.x + room.width) * canvasWidth,
                    (room.y + room.height) * canvasHeight
                )
            } else {
                // Landscape: Swap axes (rotate 90 degrees clockwise)
                // x becomes y, y becomes (1-x-width)
                rect = RectF(
                    room.y * canvasWidth,
                    (1f - room.x - room.width) * canvasHeight,
                    (room.y + room.height) * canvasWidth,
                    (1f - room.x) * canvasHeight
                )
            }

            // Draw room fill
            roomPaint.color = room.color
            canvas.drawRect(rect, roomPaint)

            // Draw walls (black outline)
            canvas.drawRect(rect, wallPaint)

            // Draw room label if it's a room (not corridor)
            if (room.isRoom) {
                val centerX = rect.centerX()
                val centerY = rect.centerY() + textPaint.textSize / 3

                // Adjust text size for smaller rooms in landscape
                val adjustedTextSize = if (portrait) 24f else 18f
                textPaint.textSize = adjustedTextSize

                canvas.drawText(room.id, centerX, centerY, textPaint)
            }
        }

        Log.d(TAG, "Finished drawing ${rooms.size} rooms in ${if (portrait) "portrait" else "landscape"} mode")
    }
}
