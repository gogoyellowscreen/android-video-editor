package com.example.vezdekodfinal.ui.editor

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.storage.StorageManager
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random


class VideoEditorViewModel : ViewModel() {

    private lateinit var fileUri: Uri
    private lateinit var tmpFile: File
    private lateinit var tmpFileUri: Uri
    private lateinit var context: Context
    private lateinit var storageManager: StorageManager

    fun init(pathUri: Uri, context: Context) {
        fileUri = pathUri
        this.context = context
        storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        val cR = context.contentResolver
        val mime = MimeTypeMap.getSingleton()
        val type = mime.getExtensionFromMimeType(cR.getType(fileUri))
        // TODO: more adequate extensions!!!!
        val tmpFileName = "tmp_${Random.nextInt()}"

        tmpFile = File.createTempFile(tmpFileName, ".$type", createTmpDirIfNotExists())
        tmpFileUri = tmpFile.toUri()

        copyFileToFile(fileUri, tmpFileUri)
    }

    fun createTmpDirIfNotExists(): File {
        val directory = File(context.cacheDir, "tmp")

        if (!directory.exists()) {
            directory.mkdir()
        }

        storageManager.setCacheBehaviorGroup(directory, true)
        return directory
    }

    fun copyFileToFile(fromUri: Uri, toUri: Uri) {
        context.contentResolver.openInputStream(fromUri).use { src ->
            context.contentResolver.openOutputStream(toUri)?.use { dst ->
                src?.copyTo(dst)
            }
        }
    }

    fun saveVideo() {
        val destinationDir = File(
            context.getExternalFilesDir(null),
            "vezdekod"
        )
        destinationDir.mkdirs()
        val destinationFile = File(destinationDir, tmpFile.name)
        if (!destinationFile.exists()) {
            destinationFile.createNewFile()
        }
        tmpFile.copyRecursively(destinationFile, true)
        MediaScannerConnection.scanFile(
            context,
            arrayOf(destinationFile.absolutePath),
            null,
            null
        )
        MediaScannerConnection.scanFile(
            context,
            destinationFile.list()?.map {
                File(destinationFile, it).absolutePath
            }?.toTypedArray() ?: emptyArray(),
            null,
            null
        )

        Toast.makeText(context, "saved at: ${destinationFile.path}", LENGTH_SHORT).show()
    }
}