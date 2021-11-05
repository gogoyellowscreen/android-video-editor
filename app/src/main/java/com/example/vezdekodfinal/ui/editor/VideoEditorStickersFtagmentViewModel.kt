package com.example.vezdekodfinal.ui.editor

import android.app.Activity
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.daasuu.mp4compose.composer.Mp4Composer
import com.daasuu.mp4compose.filter.GlWatermarkFilter
import com.example.vezdekodfinal.ui.filter.GlStickerFilter
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.lang.Exception

class VideoEditorStickersFtagmentViewModel : ViewModel() {

    val isSaving = MutableStateFlow(false)
    var openStickers = false
    private lateinit var activity: Activity

    fun init(activity: Activity) {
        this.activity = activity
    }

    fun saveVideoWithSticker(tmpFilePath: String, sticker: Bitmap, x: Float, y: Float) {
        isSaving.compareAndSet(false, true)
        val tmpDestFile = File("${tmpFilePath.substringBeforeLast('/')}/tmp_${tmpFilePath.substringAfterLast('/')}").apply {
            createNewFile()
        }
        Mp4Composer(tmpFilePath, tmpDestFile.path)
            .filter(GlStickerFilter(sticker, x, y))
            .listener(object : Mp4Composer.Listener {
                override fun onProgress(progress: Double) {

                }

                override fun onCurrentWrittenVideoTime(timeUs: Long) {

                }

                override fun onCompleted() {
                    tmpDestFile.inputStream().use { ins ->
                        File(tmpFilePath).outputStream().use { ous ->
                            ins.copyTo(ous)
                        }
                    }
                    isSaving.value = false
                }

                override fun onCanceled() {
                    isSaving.value = false
                }

                override fun onFailed(exception: Exception?) {
                    isSaving.value = false
                }

            })
            .start()
    }
}