package com.example.vezdekodfinal.ui.editor

import android.app.Activity
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.daasuu.mp4compose.composer.Mp4Composer
import com.example.vezdekodfinal.ui.filter.GlStickerFilter
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.lang.Exception

class VideoEditorEffectsViewModel : ViewModel() {

    val isSaving = MutableStateFlow(false)
    private lateinit var tmpSessionFile: File
    private lateinit var tmpEffectsFile: File

    fun init(tmpSessionFile: File, tmpEffectsFile: File) {
        this.tmpSessionFile = tmpSessionFile
        this.tmpEffectsFile = tmpEffectsFile
    }

    fun updateTmpEffectsFile(speed: Float) {
        isSaving.compareAndSet(false, true)
        Mp4Composer(tmpSessionFile.path, tmpEffectsFile.path)
            .timeScale(speed)
            .listener(object : Mp4Composer.Listener {
                override fun onProgress(progress: Double) {

                }

                override fun onCurrentWrittenVideoTime(timeUs: Long) {

                }

                override fun onCompleted() {
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

    fun saveChanges() {
        tmpEffectsFile.inputStream().use { ins ->
            tmpSessionFile.outputStream().use { ous ->
                ins.copyTo(ous)
            }
        }
    }
}