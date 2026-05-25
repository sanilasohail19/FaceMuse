package com.example.facemuse

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.View

class CameraOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val backgroundPaint: Paint = Paint().apply {
        alpha = 200 // Semi-transparent
    }
    private val circlePaint: Paint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val crosshairPaint: Paint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 3f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw the semi-transparent background
        canvas.drawColor(Color.BLACK)

        // Carve out the circle
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width * 0.4f
        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        // Draw crosshairs
        canvas.drawLine(centerX - radius - 20, centerY, centerX - radius + 20, centerY, crosshairPaint)
        canvas.drawLine(centerX + radius - 20, centerY, centerX + radius + 20, centerY, crosshairPaint)
        canvas.drawLine(centerX, centerY - radius - 20, centerX, centerY - radius + 20, crosshairPaint)
        canvas.drawLine(centerX, centerY + radius - 20, centerX, centerY + radius + 20, crosshairPaint)
    }
}
