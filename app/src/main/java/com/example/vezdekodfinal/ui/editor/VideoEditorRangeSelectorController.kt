package com.example.vezdekodfinal.ui.editor

import android.animation.ValueAnimator
import android.content.Context
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import com.example.vezdekodfinal.ui.editor.RangeSelectorController.Marker
import com.example.vezdekodfinal.ui.editor.RangeSelectorController.State
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.abs

interface RangeSelectorController {
    enum class State {
        IDLE,
        PRE_DRAG,
        DRAGGING
    }
    enum class Marker(val direction: Int = 1) {
        None,
        From,
        To,
    }
    val animator: ValueAnimator
    interface Listener {
        fun canDragMarker(marker: Marker): Boolean
        fun onRangeSelectionStart()
        fun onRangeSelectorEnd()
        fun onRangeChanged(marker: Marker, value: Float)
    }
    fun onTouchEvent(event: MotionEvent): Boolean
    fun setListener(listener: Listener?)
}

private const val PIXELS_PER_SECOND = 200
private const val MAX_FLING_DURATION_MS = 3000

class VideoEditorRangeSelectorController(
    private val context: Context,
    private val rangeSelectorView: VideoEditorRangeSelectorView,
    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop,
    private val horizontal: Boolean = true
) : RangeSelectorController {

    private var listener: RangeSelectorController.Listener? = null
    private val velocityTracker = VelocityTracker.obtain()
    override val animator: ValueAnimator by lazy {
        ValueAnimator().apply {
            interpolator = DecelerateInterpolator()
        }
    }
    private var state = State.IDLE
        set(value) {
            val prevState = field
            field = value
            if (prevState == State.IDLE && value != State.IDLE) {
                listener?.onRangeSelectionStart()
            } else if (prevState != State.IDLE && value == State.IDLE) {
                listener?.onRangeSelectorEnd()
            }
        }
    private var startCoord = 0f
    private var prevCoord = 0f
    private var startingPosition = 0f
    private var activeMarker = Marker.None
        set(value) {
            field = value
            rangeSelectorView.activeMarker = when (value) {
                Marker.From -> VideoEditorRangeSelectorView.Marker.FROM
                Marker.To -> VideoEditorRangeSelectorView.Marker.TO
                else -> null
            }
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        velocityTracker.addMovement(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activeMarker = getMarkerUnderEvent(event)
                if (activeMarker != Marker.None) {
                    startCoord = getCoord(event)
                    prevCoord = startCoord
                    startingPosition = when(activeMarker) {
                        Marker.From -> {
                            rangeSelectorView.valueFrom
                        }
                        Marker.To -> {
                            rangeSelectorView.valueTo
                        }
                        else -> 0f
                    }
                    return true
                }

            }
            MotionEvent.ACTION_UP -> {
                activeMarker = Marker.None
                if (state != State.IDLE) {
                    state = State.IDLE
                    return true
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (state != State.IDLE) {
                    listener?.onRangeChanged(activeMarker, startingPosition)
                    activeMarker = Marker.None
                    state = State.IDLE
                    return true
                } else {
                    activeMarker = Marker.None
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (state == State.DRAGGING || state == State.PRE_DRAG) {
                    val newCoord = getCoord(event)
                    val delta = (newCoord - startCoord)
                    val positionChange = delta / rangeSelectorView.activeDimension
                    val position = startingPosition + positionChange * activeMarker.direction

                    listener?.onRangeChanged(activeMarker, position)
                    prevCoord = newCoord
                } else if (activeMarker != Marker.None) {
                    val delta = getCoord(event) - startCoord
                    if (abs(delta) > touchSlop) {
                        state = State.DRAGGING
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun setListener(listener: RangeSelectorController.Listener?) {
        this.listener = listener
    }

    private fun getMarkerUnderEvent(event: MotionEvent): Marker = when {
        listener?.canDragMarker(Marker.From) == true &&
                rangeSelectorView.markerFromBounds.contains(event.x.toInt(), event.y.toInt()) -> {
            Marker.From
        }
        listener?.canDragMarker(Marker.To) == true &&
                rangeSelectorView.markerToBounds.contains(event.x.toInt(), event.y.toInt()) -> {
            Marker.To
        }
        else -> {
            Marker.None
        }
    }

    private fun getCoord(event: MotionEvent): Float = if (horizontal) {
        event.x
    } else {
        event.y
    }
}

internal fun VideoEditorRangeSelectorView.attach(
    context: Context,
    viewModel: VideoEditorCropViewModel,
    lifecycle: Lifecycle
) {
    val trimmerDragHelper =
        VideoEditorRangeSelectorController(context, this)
    trimmerDragHelper.setListener(viewModel.rangeSelectorListener)
    this.dragHelper = trimmerDragHelper

    viewModel.startMarkerPosition.onEach {
        this.valueFrom = it
    }.flowWithLifecycle(lifecycle).launchIn(lifecycle.coroutineScope)
    viewModel.endMarkerPosition.onEach {
        this.valueTo = it
    }.flowWithLifecycle(lifecycle).launchIn(lifecycle.coroutineScope)
}
