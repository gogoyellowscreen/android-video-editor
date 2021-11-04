package com.example.vezdekodfinal.ui.main

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel


class MainViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    fun getPath(uri: Uri, contentResolver: ContentResolver): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index: Int = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else null
    }
}