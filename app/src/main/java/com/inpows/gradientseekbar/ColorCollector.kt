package com.inpows.gradientseekbar

import androidx.annotation.ColorInt

interface ColorCollector {

    fun collectSectionTrackColor(@ColorInt colorIntArr: IntArray?): Boolean
}