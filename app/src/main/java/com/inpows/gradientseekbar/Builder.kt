package com.inpows.gradientseekbar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes

class Builder internal constructor(context: Context) {

    val context: Context

    //seek bar
    var max = 100f
    var min = 0f
    var progress = 0f
    var progressValueFloat = false
    var seekSmoothly = false
    var r2l = false
    var userSeekable = true
    var onlyThumbDraggable = false
    var clearPadding = false

    //indicator
    var showIndicatorType = IndicatorType.ROUNDED_RECTANGLE
    var indicatorColor: Int = Color.parseColor("#FF4081")
    var indicatorTextColor: Int = Color.parseColor("#FFFFFF")
    var indicatorTextSize = 0
    var indicatorContentView: View? = null
    var indicatorTopContentView: View? = null

    //track
    var trackBackgroundSize = 0
    var trackBackgroundColor: Int = Color.parseColor("#D7D7D7")
    var trackProgressSize = 0
    var trackProgressColor: Int = Color.parseColor("#FF4081")
    var trackRoundedCorners = false
    var trackBackgroundType = BackgroundType.SINGLE_COLOR

    //thumbText
    var thumbTextColor: Int = Color.parseColor("#FF4081")
    var showThumbText = false

    //thumb
    var thumbSize = 0
    var thumbColor: Int = Color.parseColor("#FF4081")
    var thumbColorStateList: ColorStateList? = null
    var thumbDrawable: Drawable? = null

    //tickTexts
    var showTickText = false
    var tickTextsColor: Int = Color.parseColor("#FF4081")
    var tickTextsSize = 0
    var tickTextsCustomArray: Array<String>? = null
    var tickTextsTypeFace = Typeface.DEFAULT
    var tickTextsColorStateList: ColorStateList? = null

    //tickMarks
    var tickCount = 0
    var showTickMarksType: Int = TickMarkType.NONE
    var tickMarksColor: Int = Color.parseColor("#FF4081")
    var tickMarksSize = 0
    var tickMarksDrawable: Drawable? = null
    var tickMarksEndsHide = false
    var tickMarksSweptHide = false
    var tickMarksColorStateList: ColorStateList? = null

    //ruler
    var mRulerWidth = 0
    var mRulerSize = 0
    var mRulerOffset = 0
    var mRulerColor = Color.LTGRAY

    init {
        this.context = context
        indicatorTextSize = spToPx(14f)
        trackBackgroundSize = spToPx(2f)
        trackProgressSize = spToPx(2f)
        tickMarksSize = spToPx(10f)
        tickTextsSize = spToPx(13f)
        thumbSize = spToPx(14f)
        mRulerSize = dpToPx(4f)
        mRulerWidth = dpToPx(1.1f)
        mRulerOffset = dpToPx(14f)
    }

    fun build(): GradientSeekBar {
        return GradientSeekBar(this)
    }

    fun max(max: Float): Builder {
        this.max = max
        return this
    }

    fun min(min: Float): Builder {
        this.min = min
        return this
    }

    fun progress(progress: Float): Builder {
        this.progress = progress
        return this
    }

    fun progressValueFloat(isFloatProgress: Boolean): Builder {
        progressValueFloat = isFloatProgress
        return this
    }

    fun seekSmoothly(seekSmoothly: Boolean): Builder {
        this.seekSmoothly = seekSmoothly
        return this
    }

    fun r2l(r2l: Boolean): Builder {
        this.r2l = r2l
        return this
    }

    fun clearPadding(clearPadding: Boolean): Builder {
        this.clearPadding = clearPadding
        return this
    }

    fun userSeekable(userSeekable: Boolean): Builder {
        this.userSeekable = userSeekable
        return this
    }

    fun onlyThumbDraggable(onlyThumbDraggable: Boolean): Builder {
        this.onlyThumbDraggable = onlyThumbDraggable
        return this
    }

    fun showIndicatorType(showIndicatorType: Int): Builder {
        this.showIndicatorType = showIndicatorType
        return this
    }

    fun indicatorColor(@ColorInt indicatorColor: Int): Builder {
        this.indicatorColor = indicatorColor
        return this
    }

    fun indicatorTextColor(@ColorInt indicatorTextColor: Int): Builder {
        this.indicatorTextColor = indicatorTextColor
        return this
    }

    fun indicatorTextSize(indicatorTextSize: Int): Builder {
        this.indicatorTextSize = spToPx(indicatorTextSize.toFloat())
        return this
    }

    fun indicatorContentView(indicatorContentView: View): Builder {
        this.indicatorContentView = indicatorContentView
        return this
    }

    fun indicatorContentViewLayoutId(@LayoutRes layoutId: Int): Builder {
        indicatorContentView = View.inflate(context, layoutId, null)
        return this
    }

    fun indicatorTopContentView(topContentView: View): Builder {
        indicatorTopContentView = topContentView
        return this
    }

    fun indicatorTopContentViewLayoutId(@LayoutRes layoutId: Int): Builder {
        indicatorTopContentView = View.inflate(context, layoutId, null)
        return this
    }

    fun trackBackgroundSize(trackBackgroundSize: Int): Builder {
        this.trackBackgroundSize = dpToPx(trackBackgroundSize.toFloat())
        return this
    }

    fun trackBackgroundColor(@ColorInt trackBackgroundColor: Int): Builder {
        this.trackBackgroundColor = trackBackgroundColor
        return this
    }

    fun trackProgressSize(trackProgressSize: Int): Builder {
        this.trackProgressSize = dpToPx(trackProgressSize.toFloat())
        return this
    }

    fun trackProgressColor(@ColorInt trackProgressColor: Int): Builder {
        this.trackProgressColor = trackProgressColor
        return this
    }

    fun trackRoundedCorners(trackRoundedCorners: Boolean): Builder {
        this.trackRoundedCorners = trackRoundedCorners
        return this
    }

    fun thumbTextColor(@ColorInt thumbTextColor: Int): Builder {
        this.thumbTextColor = thumbTextColor
        return this
    }

    fun showThumbText(showThumbText: Boolean): Builder {
        this.showThumbText = showThumbText
        return this
    }

    fun thumbColor(@ColorInt thumbColor: Int): Builder {
        this.thumbColor = thumbColor
        return this
    }

    fun thumbColorStateList(thumbColorStateList: ColorStateList): Builder {
        this.thumbColorStateList = thumbColorStateList
        return this
    }

    fun thumbSize(thumbSize: Int): Builder {
        this.thumbSize = dpToPx(thumbSize.toFloat())
        return this
    }

    fun thumbDrawable(thumbDrawable: Drawable): Builder {
        this.thumbDrawable = thumbDrawable
        return this
    }

    fun thumbDrawable(thumbStateListDrawable: StateListDrawable): Builder {
        thumbDrawable = thumbStateListDrawable
        return this
    }

    fun showTickTexts(showTickText: Boolean): Builder {
        this.showTickText = showTickText
        return this
    }

    fun tickTextsColor(@ColorInt tickTextsColor: Int): Builder {
        this.tickTextsColor = tickTextsColor
        return this
    }

    fun tickTextsColorStateList(tickTextsColorStateList: ColorStateList): Builder {
        this.tickTextsColorStateList = tickTextsColorStateList
        return this
    }

    fun tickTextsSize(tickTextsSize: Int): Builder {
        this.tickTextsSize = spToPx(tickTextsSize.toFloat())
        return this
    }

    fun tickTextsArray(tickTextsArray: Array<String>?): Builder {
        tickTextsCustomArray = tickTextsArray
        return this
    }

    fun tickTextsArray(@ArrayRes tickTextsArray: Int): Builder {
        tickTextsCustomArray = context.resources.getStringArray(tickTextsArray)
        return this
    }

    fun tickTextsTypeFace(tickTextsTypeFace: Typeface): Builder {
        this.tickTextsTypeFace = tickTextsTypeFace
        return this
    }

    fun tickCount(tickCount: Int): Builder {
        this.tickCount = tickCount
        return this
    }

    fun showTickMarksType(tickMarksType: Int): Builder {
        showTickMarksType = tickMarksType
        return this
    }

    fun tickMarksColor(@ColorInt tickMarksColor: Int): Builder {
        this.tickMarksColor = tickMarksColor
        return this
    }

    fun tickMarksColor(tickMarksColorStateList: ColorStateList): Builder {
        this.tickMarksColorStateList = tickMarksColorStateList
        return this
    }

    fun tickMarksSize(tickMarksSize: Int): Builder {
        this.tickMarksSize = dpToPx(tickMarksSize.toFloat())
        return this
    }

    fun tickMarksDrawable(tickMarksDrawable: Drawable): Builder {
        this.tickMarksDrawable = tickMarksDrawable
        return this
    }

    fun tickMarksDrawable(tickMarksStateListDrawable: StateListDrawable): Builder {
        tickMarksDrawable = tickMarksStateListDrawable
        return this
    }

    fun tickMarksEndsHide(tickMarksEndsHide: Boolean): Builder {
        this.tickMarksEndsHide = tickMarksEndsHide
        return this
    }

    fun tickMarksSweptHide(tickMarksSweptHide: Boolean): Builder {
        this.tickMarksSweptHide = tickMarksSweptHide
        return this
    }
}