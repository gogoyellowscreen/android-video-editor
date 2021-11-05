package com.example.vezdekodfinal.ui

import android.app.Activity
import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.Dimension
import java.io.FileDescriptor

fun dpToPx(@Dimension dp: Int, context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}

fun FileDescriptor.findFirstTrack(trackType: String): Int {
    val extractor = MediaExtractor()
    extractor.setDataSource(this)
    var trackIdx = -1
    for (i in 0 until extractor.trackCount) {
        val mediaFormat = extractor.getTrackFormat(i)
        val mimeType = mediaFormat.getString(MediaFormat.KEY_MIME) ?: continue
        if (mimeType.startsWith(trackType)) {
            trackIdx = i
        }
    }
    extractor.release()
    return trackIdx
}

fun windowHeight(context: Activity): Int {
    val displayMetrics = DisplayMetrics()
    context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}

fun windowWidth(context: Activity): Int {
    val displayMetrics = DisplayMetrics()
    context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}
