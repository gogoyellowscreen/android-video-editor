package com.example.vezdekodfinal.ui.editor

import android.content.Context
import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.lifecycle.Lifecycle
import kotlin.math.abs

interface VideoEditorCropVideoController {
    enum class State {
        IDLE,
        PRE_DRAG,
        DRAGGING
    }
    interface Listener {
        fun onRangeChanged(marker: VideoEditorCropVideoView.Pointer, valueX: Float, valueY: Float)
    }
    fun onTouchEvent(event: MotionEvent): Boolean
    fun setListener(listener: Listener?)
}

class VideoEditorCropVideoControllerImpl(
    private val view: VideoEditorCropVideoViewAbs
) : VideoEditorCropVideoController {

    private var listener: VideoEditorCropVideoController.Listener? = null
    private val velocityTracker = VelocityTracker.obtain()
    private var state = VideoEditorCropVideoController.State.IDLE

    private var activePointer = VideoEditorCropVideoView.Pointer.NONE

    private var xStartCoord: Int = 0
    private var prevXCoord: Int = 0
    private var yStartCoord: Int = 0
    private var prevYCoord: Int = 0

    private var valueX: Float = 0f
    private var valueY: Float = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        velocityTracker.addMovement(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointer = getPointer(event)
                if (activePointer != VideoEditorCropVideoView.Pointer.NONE) {
                    xStartCoord = event.x.toInt()
                    prevXCoord = xStartCoord
                    yStartCoord = event.y.toInt()
                    prevYCoord = yStartCoord
                    when (activePointer) {
                        VideoEditorCropVideoView.Pointer.LEFT_TOP -> {
                            valueX = view.xLeftRel
                            valueY = view.yTopRel
                        }
                        VideoEditorCropVideoView.Pointer.RIGHT_TOP -> {
                            valueX = view.xRightRel
                            valueY = view.yTopRel
                        }
                        VideoEditorCropVideoView.Pointer.LEFT_BOTTOM -> {
                            valueX = view.xLeftRel
                            valueY = view.yBottomRel
                        }
                        VideoEditorCropVideoView.Pointer.RIGHT_BOTTOM -> {
                            valueX = view.xRightRel
                            valueY = view.yBottomRel
                        }
                        VideoEditorCropVideoView.Pointer.NONE -> Unit
                    }
                    return true
                }

            }
            MotionEvent.ACTION_UP -> {
                activePointer = VideoEditorCropVideoView.Pointer.NONE
                if (state != VideoEditorCropVideoController.State.IDLE) {
                    state = VideoEditorCropVideoController.State.IDLE
                    return true
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (state != VideoEditorCropVideoController.State.IDLE) {
                    listener?.onRangeChanged(activePointer, valueX, valueY)
                    activePointer = VideoEditorCropVideoView.Pointer.NONE
                    state = VideoEditorCropVideoController.State.IDLE
                    return true
                } else {
                    activePointer = VideoEditorCropVideoView.Pointer.NONE
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (state == VideoEditorCropVideoController.State.DRAGGING || state == VideoEditorCropVideoController.State.PRE_DRAG) {
                    val newXCoord = event.x.toInt()
                    prevXCoord = xStartCoord
                    val newYCoord = event.y.toInt()
                    prevYCoord = yStartCoord
                    val deltaX = newXCoord - xStartCoord
                    val deltaY = newYCoord - yStartCoord
                    val positionXChange = deltaX / view.width
                    val positionYChange = deltaY / view.height
                    //val newCoord = getCoord(event)
                    //val delta = (newCoord - startCoord)
                    //val positionChange = delta / rangeSelectorView.activeDimension
                    //val position = startingPosition + positionChange * activeMarker.direction

                    listener?.onRangeChanged(activePointer, valueX + positionXChange, valueY + positionYChange)
                    // prevCoord = newCoord
                    prevXCoord = newXCoord
                    prevYCoord = newYCoord
                } else if (activePointer != VideoEditorCropVideoView.Pointer.NONE) {
                    state = VideoEditorCropVideoController.State.DRAGGING
                    return true
                }
            }
        }
        return false
    }

    override fun setListener(listener: VideoEditorCropVideoController.Listener?) {
        this.listener = listener
    }

    private fun getPointer(event: MotionEvent) = when {
        view.leftBottomBounds.contains(event.x.toInt(), event.y.toInt()) -> {
            VideoEditorCropVideoView.Pointer.LEFT_TOP
        }
        view.rightTopBounds.contains(event.x.toInt(), event.y.toInt()) -> {
            VideoEditorCropVideoView.Pointer.RIGHT_TOP
        }
        view.leftBottomBounds.contains(event.x.toInt(), event.y.toInt()) -> {
            VideoEditorCropVideoView.Pointer.LEFT_BOTTOM
        }
        view.rightBottomBounds.contains(event.x.toInt(), event.y.toInt()) -> {
            VideoEditorCropVideoView.Pointer.RIGHT_BOTTOM
        }
        else -> VideoEditorCropVideoView.Pointer.NONE
    }
}

internal fun VideoEditorCropVideoView.attach(
    context: Context,
    viewModel: VideoEditorCropViewModel,
    lifecycle: Lifecycle
) {

}
