package com.example.vezdekodfinal.ui.editor

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class VideoEditorCropViewModel : ViewModel() {

    val startMarkerPosition = MutableStateFlow(0f)
    val endMarkerPosition = MutableStateFlow(1f)

    val startPositionMs = MutableStateFlow(0L)
    private lateinit var endPositionMs: MutableStateFlow<Long>

    var durationMs = 0L

    fun init(videoDuration: Long) {
        durationMs = videoDuration
        endPositionMs = MutableStateFlow(videoDuration)
    }

    val rangeSelectorListener = object : RangeSelectorController.Listener {
        override fun canDragMarker(marker: RangeSelectorController.Marker): Boolean {
            return true // TODO: cases when I cant???
        }

        override fun onRangeSelectionStart() {
            // TODO: сбросить начало видоса
        }

        override fun onRangeSelectorEnd() {
            // TODO: chtoto
        }

        override fun onRangeChanged(marker: RangeSelectorController.Marker, value: Float) {
            // TODO: обновляю  отрезок в flow value, когда говорим кропать - кропаем
            setMarkerPosition(marker, value.coerceIn(0f, 1f))
        }
    }

    private fun setMarkerPosition(marker: RangeSelectorController.Marker, value: Float) {

        when (marker) {
            RangeSelectorController.Marker.From -> {
                val newStart = value.coerceIn(0f, endMarkerPosition.value)

                startMarkerPosition.value = newStart
                startPositionMs.value = (newStart * durationMs).toLong()
            }
            RangeSelectorController.Marker.To -> {
                val newEnd = value.coerceIn(startMarkerPosition.value, 1f)

                endMarkerPosition.value = newEnd
                endPositionMs.value = (newEnd * durationMs).toLong()
            }
            else -> Log.d("POSOSI", "KEKEKEKEKEKEKE")
        }
    }
}