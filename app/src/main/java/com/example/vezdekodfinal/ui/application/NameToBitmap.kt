package com.example.vezdekodfinal.ui.application

import android.graphics.Bitmap

object NameToBitmap {

    private val cache = mutableMapOf<String, Bitmap>()

    fun put(name: String, bitmap: Bitmap) {
        cache[name] = bitmap
    }

    fun getIfExists(name: String): Bitmap? {
        return cache[name]
    }
}
