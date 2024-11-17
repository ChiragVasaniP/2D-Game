package com.example.fallingobjectgame.gameView.gameUtils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun Bitmap.flipHorizontally(): Bitmap {
    val matrix = Matrix().apply { postScale(-1f, 1f, width / 2f, height / 2f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

// To flip vertically:
fun Bitmap.flipVertically(): Bitmap {
    val matrix = Matrix().apply { postScale(1f, -1f, width / 2f, height / 2f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}


fun Int.toPx() =
    (this * Resources.getSystem().displayMetrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT

fun Float.toPx() =
    (this * Resources.getSystem().displayMetrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT

fun Float.dpToPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, toFloat(), Resources.getSystem().displayMetrics
    ).toInt()
}

@ColorInt
fun Context.getColorCompat(@ColorRes res: Int) = ContextCompat.getColor(this, res)
fun Context.getColorListCompat(@ColorRes res: Int) = ContextCompat.getColorStateList(this, res)
fun Context.getDrawableCompat(@DrawableRes res: Int) = ContextCompat.getDrawable(this, res)!!


@ColorInt
fun View.getColor(@ColorRes colorRes: Int) = context.getColorCompat(colorRes)
fun View.getColorList(@ColorRes res: Int) = context.getColorListCompat(res)
fun View.getDrawable(@DrawableRes res: Int) = context.getDrawableCompat(res)
