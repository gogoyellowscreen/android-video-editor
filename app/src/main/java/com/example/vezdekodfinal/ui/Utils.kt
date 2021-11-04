package com.example.vezdekodfinal.ui

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.Dimension

fun dpToPx(@Dimension dp: Int, context: Context): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
}