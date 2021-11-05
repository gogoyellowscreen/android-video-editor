package com.example.vezdekodfinal.ui.editor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.vezdekodfinal.R
import com.example.vezdekodfinal.ui.dpToPx

interface VideoEditorCropVideoView {
    enum class Pointer {
        LEFT_TOP,
        RIGHT_TOP,
        LEFT_BOTTOM,
        RIGHT_BOTTOM,
        NONE,
    }
    var controller: VideoEditorCropVideoController?
    var xLeft: Int
    var xLeftRel: Float
    var leftTopBounds: Rect
    var xRight: Int
    var xRightRel: Float
    var rightTopBounds: Rect
    var yTop: Int
    var yTopRel: Float
    var leftBottomBounds: Rect
    var yBottom: Int
    var yBottomRel: Float
    var rightBottomBounds: Rect
}

class VideoEditorCropVideoViewAbs @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes), VideoEditorCropVideoView {

    override var controller: VideoEditorCropVideoController? = null

    override var xLeft: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    override var xLeftRel: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var leftTopDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.crop_corner_pointer2)!!
    override var leftTopBounds: Rect = Rect(0, 0, 0, 0)
        get() = Rect(
            xLeft + (xLeftRel * (xRight - xLeft)).toInt() - dpToPx(9, context),
            yTop + (yTopRel * (yBottom - yTop)).toInt() - dpToPx(9, context),
            xLeft + (xLeftRel * (xRight - xLeft)).toInt() + dpToPx(9, context),
            yTop + (yTopRel * (yBottom - yTop)).toInt() + dpToPx(9, context),
        )

    override var xRight: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    override var xRightRel: Float = 1f
        set(value) {
            field = value
            invalidate()
        }
    var rightTopDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.crop_corner_pointer2)!!
    override var rightTopBounds: Rect = Rect(0, 0, 0, 0)
        get() = Rect(
            xLeft + (xRightRel * (xRight - xLeft)).toInt() - dpToPx(9, context),
            yTop + (yTopRel * (yBottom - yTop)).toInt() - dpToPx(9, context),
            xLeft + (xRightRel * (xRight - xLeft)).toInt() + dpToPx(9, context),
            yTop + (yTopRel * (yBottom - yTop)).toInt() + dpToPx(9, context),
        )

    override var yTop: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    override var yTopRel: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var leftBottomDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.crop_corner_pointer2)!!
    override var leftBottomBounds: Rect = Rect(0, 0, 0, 0)
        get() = Rect(
            xLeft + (xLeftRel * (xRight - xLeft)).toInt() - dpToPx(9, context),
            yTop + (yBottomRel * (yBottom - yTop)).toInt()- dpToPx(9, context),
            xLeft + (xLeftRel * (xRight - xLeft)).toInt() + dpToPx(9, context),
            yTop + (yBottomRel * (yBottom - yTop)).toInt() + dpToPx(9, context),
        )

    override var yBottom: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    override var yBottomRel: Float = 1f
        set(value) {
            field = value
            invalidate()
        }
    var rightBottomDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.crop_corner_pointer2)!!
    override var rightBottomBounds: Rect = Rect(0, 0, 0, 0)
        get() = Rect(
            xLeft + (xRightRel * (xRight - xLeft)).toInt() - dpToPx(9, context),
            yTop + (yBottomRel * (yBottom - yTop)).toInt() - dpToPx(9, context),
            xLeft + (xRightRel * (xRight - xLeft)).toInt() + dpToPx(9, context),
            yTop + (yBottomRel * (yBottom - yTop)).toInt() + dpToPx(9, context),
        )

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return controller?.onTouchEvent(event) ?: false
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        canvas ?: return

        leftTopDrawable.bounds = leftTopBounds
        leftTopDrawable.draw(canvas)

        rightTopDrawable.bounds = rightTopBounds
        rightTopDrawable.draw(canvas)

        leftBottomDrawable.bounds = leftBottomBounds
        leftBottomDrawable.draw(canvas)

        rightBottomDrawable.bounds = rightBottomBounds
        rightBottomDrawable.draw(canvas)

        canvas.drawRect(
            Rect(
                xLeft + (xLeftRel * (xRight - xLeft)).toInt(),
                yTop + (yTopRel * (yBottom - yTop)).toInt(),
                xLeft + (xRightRel * (xRight - xLeft)).toInt(),
                yTop + (yBottomRel * (yBottom - yTop)).toInt()
            ), Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = Color.WHITE
            })
    }
}
