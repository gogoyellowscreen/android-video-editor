package com.example.vezdekodfinal.ui.filter

import android.graphics.Bitmap
import android.graphics.Canvas
import com.daasuu.mp4compose.filter.GlWatermarkFilter

class GlStickerFilter(private val bitmap: Bitmap, private val x: Float, private val y: Float) : GlWatermarkFilter(bitmap) {
    override fun drawCanvas(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x * canvas.width, y * canvas.height, null)
    }
}
