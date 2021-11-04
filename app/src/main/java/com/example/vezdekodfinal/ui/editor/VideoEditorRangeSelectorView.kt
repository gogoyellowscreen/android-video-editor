package com.example.vezdekodfinal.ui.editor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import com.example.vezdekodfinal.R
import com.example.vezdekodfinal.ui.dpToPx
import com.example.vezdekodfinal.ui.editor.VideoEditorRangeSelectorView.Marker
import kotlin.math.ceil
import kotlin.math.roundToInt

interface VideoEditorRangeSelectorView {
    enum class Marker {
        FROM,
        TO,
    }
    var dragHelper: RangeSelectorController?
    var valueFrom: Float
    var valueTo: Float
    var activeMarker: Marker?
    val markerFromBounds: Rect
    val markerToBounds: Rect
    val activeDimension: Int
    val markerFromWidth: Int
    val markerToWidth: Int
}

class VideoEditorRangeSelectorViewAbs @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    View(context, attrs, defStyleAttr, defStyleRes),
    VideoEditorRangeSelectorView {

    var markerFrom: Drawable = ContextCompat.getDrawable(context, R.drawable.video_trimmer_left_marker)!!
        set(value) {
            field = value
            invalidate()
        }
    var markerTo: Drawable = ContextCompat.getDrawable(context, R.drawable.video_trimmer_right_marker)!!
        set(value) {
            field = value
            invalidate()
        }

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    val fillColor: Int
    val fillPaint: Paint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
        }
    }
    val fadePaint = Paint()
    val workingRect = Rect()
    val workingRectF = RectF()
    val clipPath = Path()
    val cornerRadius: Float

    private val markerValueShadowRadius = dpToPx(10, context).toFloat()
    private val markerValueShadowRect: Rect = Rect(0, 0, 0, 0)
    private val markerValueShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        setShadowLayer(markerValueShadowRadius, 0f, 0f, Color.BLACK)
    }
    private val markerValueShadowWidth = dpToPx(2, context)

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.VideoEditorRangeSelectorViewAbs,
            defStyleAttr,
            defStyleRes
        ).apply {
            markerFrom = getDrawable(
                R.styleable.VideoEditorRangeSelectorViewAbs_android_drawableLeft
            ) ?: ContextCompat.getDrawable(context, R.drawable.video_trimmer_left_marker)!!
            markerTo = getDrawable(
                R.styleable.VideoEditorRangeSelectorViewAbs_android_drawableRight
            ) ?: ContextCompat.getDrawable(context, R.drawable.video_trimmer_right_marker)!!
            valueFrom =
                getFloat(R.styleable.VideoEditorRangeSelectorViewAbs_android_valueFrom, 0f)
            valueTo = getFloat(R.styleable.VideoEditorRangeSelectorViewAbs_android_valueTo, 1f)
            cornerRadius = getDimensionPixelSize(
                R.styleable.VideoEditorRangeSelectorViewAbs_cornerRadius, 0).toFloat()
            strokePaint.color =
                getColor(R.styleable.VideoEditorRangeSelectorViewAbs_rangeStrokeColor, 0)
            strokePaint.strokeWidth = getDimensionPixelSize(
                R.styleable.VideoEditorRangeSelectorViewAbs_rangeStrokeWidth, 0).toFloat()
            fadePaint.color =
                getColor(R.styleable.VideoEditorRangeSelectorViewAbs_fadeColor, 0)
            fillColor = getColor(R.styleable.VideoEditorRangeSelectorViewAbs_fillColor,
                Color.TRANSPARENT)
            if (fillColor != Color.TRANSPARENT) {
                fillPaint.color = fillColor
            }
            val trimEnabled = getBoolean(R.styleable.VideoEditorRangeSelectorViewAbs_trimEnabled, true)
        }.recycle()
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    paddingLeft,
                    paddingTop,
                    view.width - paddingRight - paddingLeft,
                    view.height - paddingBottom - paddingTop,
                    cornerRadius
                )
            }
        }
        clipToOutline = false
    }

    override var dragHelper: RangeSelectorController? = null

    override var activeMarker: Marker? = null
        set(value) {
            field = value
            markerFrom.state = if (value == Marker.FROM) PRESSED_STATE_SET else EMPTY_STATE_SET
            markerTo.state = if (value == Marker.TO) PRESSED_STATE_SET else EMPTY_STATE_SET
            invalidate()
        }

    @FloatRange(from = 0.0, to = 1.0)
    override var valueFrom: Float = 0f
        set(newValue) {
            if (newValue == field) return
            field = newValue.coerceIn(0f, valueTo)
            invalidate()
        }

    @FloatRange(from = 0.0, to = 1.0)
    override var valueTo: Float = 1f
        set(newValue) {
            if (newValue == field) return
            field = newValue.coerceIn(valueFrom, 1f)
            invalidate()
        }
    override val activeDimension: Int
        get() = (width - (paddingLeft + paddingRight)) - (markerFromWidth + markerToWidth)
    override val markerFromWidth: Int
        get() = if (markerFrom.isVisible) {
            markerFrom.intrinsicWidth
        } else {
            0
        }
    override val markerToWidth: Int
        get() = if (markerTo.isVisible) {
            markerTo.intrinsicWidth
        } else {
            0
        }
    override val markerFromBounds get() = if (markerFrom.isVisible) {
        markerFrom.bounds
    } else {
        Rect()
    }
    override val markerToBounds get() = if (markerTo.isVisible) {
        markerTo.bounds
    } else {
        Rect()
    }
    val timelineWidthPx: Int
        get() = (width - (paddingLeft + paddingRight))

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) {
            return
        }

        workingRectF.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            timelineWidthPx - paddingRight.toFloat(),
            height - paddingBottom.toFloat(),
        )
        workingRect.set(
            paddingLeft,
            paddingTop,
            timelineWidthPx - paddingRight,
            height - paddingBottom,
        )

        drawTimeline(canvas)
        updateMarkerBounds(workingRect.top, workingRect.bottom)
        drawFillOrStroke(canvas)

        // Draw markers
        if (markerFrom.isVisible) {
            markerFrom.draw(canvas)
        }
        if (markerTo.isVisible) {
            markerTo.draw(canvas)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isEnabled && dragHelper?.onTouchEvent(event) == true) {
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun updateMarkerBounds(top: Int, bottom: Int) {
        val activeZoneLeftPadding = markerFromWidth + paddingLeft
        val activeWidthPx = timelineWidthPx - (markerFromWidth + markerToWidth)
        val leftPositionPx =
            (activeZoneLeftPadding + activeWidthPx * (valueFrom)).roundToInt()
        markerFrom.setBounds(
            leftPositionPx - markerFromWidth,
            top,
            leftPositionPx,
            bottom
        )
        val rightPositionPx =
            (activeZoneLeftPadding + activeWidthPx * (valueTo)).roundToInt()
        markerTo.setBounds(
            rightPositionPx,
            top,
            rightPositionPx + markerToWidth,
            bottom
        )
    }

    private fun drawFillOrStroke(canvas: Canvas) {
        if (markerFrom.isVisible && markerTo.isVisible) {
            val left = markerFrom.bounds.right.toFloat()
            val right = markerTo.bounds.left.toFloat()

            if (fillColor != Color.TRANSPARENT) {
                val top = workingRectF.top
                val bottom = workingRectF.bottom
                canvas.drawRect(left, top, right, bottom, fillPaint)
            } else {
                val top = workingRectF.top + strokePaint.strokeWidth / 2
                val bottom = workingRectF.bottom - strokePaint.strokeWidth / 2
                canvas.drawRect(left, top, right, bottom, strokePaint)
            }
        }
    }

    fun drawTimeline(canvas: Canvas) {
        canvas.save()

        // Clip timeline to round rect
        clipPath.apply {
            reset()
            addRoundRect(
                paddingLeft.toFloat(),
                workingRectF.top,
                width - paddingRight - 0f,
                workingRectF.bottom,
                cornerRadius,
                cornerRadius,
                Path.Direction.CW
            )
        }
        canvas.clipPath(clipPath)

        // Draw black background on timeline
        canvas.drawRect(
            paddingLeft.toFloat(),
            workingRectF.top,
            width - paddingRight - 0f,
            workingRectF.bottom,
            Paint().apply {
                @Suppress("MagicNumber")
                color = Color.argb(255, 0, 0, 0)
            }
        )

        /* TODO: add thumbnails!!!*/
        canvas.restore()
    }
}
