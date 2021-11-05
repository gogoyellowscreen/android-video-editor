package com.example.vezdekodfinal.ui.editor

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.lifecycle.ViewModel
import com.daasuu.mp4compose.FillMode
import com.daasuu.mp4compose.FillModeCustomItem
import com.daasuu.mp4compose.Rotation
import com.daasuu.mp4compose.composer.Mp4Composer
import com.daasuu.mp4compose.composer.Mp4ComposerFork
import com.daasuu.mp4compose.filter.GlFilter
import com.daasuu.mp4compose.filter.GlFilterGroup
import com.daasuu.mp4compose.filter.GlMonochromeFilter
import com.daasuu.mp4compose.filter.GlVignetteFilter
import com.google.android.exoplayer2.MetadataRetriever
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.lang.Exception

class VideoEditorCropViewModel : ViewModel() {

    val startMarkerPosition = MutableStateFlow(0f)
    val endMarkerPosition = MutableStateFlow(1f)

    val startPositionMs = MutableStateFlow(0L)
    lateinit var endPositionMs: MutableStateFlow<Long>
    val applyChangesProgress = MutableStateFlow(0.0)
    val isApplyingChanges = MutableStateFlow(false)
    private lateinit var context: Context

    private lateinit var path: String
    private lateinit var tmpPath: String

    var width: Int = 0
    var height: Int = 0

    val xLeftRel = MutableStateFlow(0f)
    val xRightRel = MutableStateFlow(1f)
    val yTopRel = MutableStateFlow(0f)
    val yBottomRel = MutableStateFlow(1f)

    var durationMs = 0L

    fun init(path: String, videoDuration: Long, context: Context) {
        this.path = path
        durationMs = videoDuration
        this.context = context
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

    val cropVideoListener = object : VideoEditorCropVideoController.Listener {
        override fun onRangeChanged(
            marker: VideoEditorCropVideoView.Pointer,
            valueX: Float,
            valueY: Float
        ) {
            when (marker) {
                VideoEditorCropVideoView.Pointer.LEFT_TOP -> {
                    val newXStart = valueX.coerceIn(0f, xRightRel.value)
                    xLeftRel.value = newXStart
                    val newYTop = valueY.coerceIn(0f, yBottomRel.value)
                    yTopRel.value = newYTop
                }
                VideoEditorCropVideoView.Pointer.RIGHT_TOP -> {
                    val newXEnd = valueX.coerceIn(xLeftRel.value, 1f)
                    xRightRel.value = newXEnd
                    val newYTop = valueY.coerceIn(0f, yBottomRel.value)
                    yTopRel.value = newYTop
                }
                VideoEditorCropVideoView.Pointer.LEFT_BOTTOM -> {
                    val newXStart = valueX.coerceIn(0f, xRightRel.value)
                    xLeftRel.value = newXStart
                    val newYBottom = valueY.coerceIn(yTopRel.value, 1f)
                    yBottomRel.value = newYBottom
                }
                VideoEditorCropVideoView.Pointer.RIGHT_BOTTOM -> {
                    val newXEnd = valueX.coerceIn(xLeftRel.value, 1f)
                    xRightRel.value = newXEnd
                    val newYBottom = valueY.coerceIn(yTopRel.value, 1f)
                    yBottomRel.value = newYBottom
                }
                VideoEditorCropVideoView.Pointer.NONE -> Unit
            }
        }
    }

    private fun setMarkerPosition(marker: RangeSelectorController.Marker, value: Float) {

        when (marker) {
            RangeSelectorController.Marker.From -> {
                val newStart = value.coerceIn(0f, endMarkerPosition.value)

                startMarkerPosition.value = newStart
                Log.d("XYU 2228", "$newStart")
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

    fun applyChanges() {
        isApplyingChanges.value = true
        val tmpDestFile = File("${path.substringBeforeLast('/')}/tmp_${path.substringAfterLast('/')}").apply {
            createNewFile()
        }
        this.tmpPath = tmpDestFile.path
        Mp4Composer(path, tmpDestFile.path)
            .fillMode(FillMode.CUSTOM)
            .customFillMode(FillModeCustomItem(
                1 / (xRightRel.value - xLeftRel.value),
                0f, xLeftRel.value * width, yTopRel.value * height,
                (xRightRel.value - xLeftRel.value) * width,
                (yBottomRel.value - yTopRel.value) * height
            ))
            .trim(startPositionMs.value, endPositionMs.value)
            .listener(object : Mp4Composer.Listener {
                override fun onProgress(progress: Double) {
                    applyChangesProgress.value = progress
                }

                override fun onCurrentWrittenVideoTime(timeUs: Long) {
                    // TODO("Not yet implemented")
                }

                override fun onCompleted() {
                    isApplyingChanges.value = false
                }

                override fun onCanceled() {
                    isApplyingChanges.value = false
                }

                override fun onFailed(exception: Exception?) {
                    isApplyingChanges.value = false
                }
            })
            .start()
    }

    fun afterApplyingChanges() {
        File(tmpPath).inputStream().use { ins ->
            File(path).outputStream().use { ous ->
                ins.copyTo(ous)
            }
        }
    }

    fun extractSingleThumbnail(
        width: Int = 40,
        height: Int = 40
    ): Bitmap? {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(path)
        return metadataRetriever.getScaledFrameAtTime(
            0L,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
            width,
            height
        )
    }
}