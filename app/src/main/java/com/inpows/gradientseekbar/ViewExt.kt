package com.inpows.gradientseekbar

import android.content.res.Resources
import android.util.TypedValue

fun dpToPx(dp: Float): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, dp,
    Resources.getSystem().displayMetrics).toInt()

fun pxToDp(px: Int): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_PX, px.toFloat(),
    Resources.getSystem().displayMetrics)

fun spToPx(sp: Float): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP, sp,
    Resources.getSystem().displayMetrics).toInt()

fun pxTosp(pxValue: Float): Float {
    return (pxValue / Resources.getSystem().displayMetrics.scaledDensity + 0.5f)
}