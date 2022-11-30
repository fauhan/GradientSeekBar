package com.inpows.gradientseekbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

class IndicatorStayLayout(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayout(context, attrs, defStyleAttr) {

    constructor(context: Context?) : this(context, null) {}
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {}

    init {
        orientation = VERTICAL
    }

    override fun onFinishInflate() {
        val childCount = childCount
        for (i in childCount - 1 downTo 0) {
            layoutIndicator(getChildAt(i), i)
        }
        super.onFinishInflate()
    }

    @JvmOverloads fun attachTo(seekBar: GradientSeekBar?, index: Int = -2) {
        if (seekBar == null) {
            throw NullPointerException(
                "the seek bar wanna attach to IndicatorStayLayout " +
                        "can not be null value."
            )
        }
        layoutIndicator(seekBar, index)
        addView(seekBar, index + 1)
    }

    private fun layoutIndicator(child: View, index: Int) {
        if (child is GradientSeekBar) {
            child.setIndicatorStayAlways(true)
            val contentView = child.getIndicatorContentView()
                ?: throw IllegalStateException(
                    "Can not find any indicator in the IndicatorSeekBar, please " +
                            "make sure you have called the attr: SHOW_INDICATOR_TYPE for IndicatorSeekBar and the value is not IndicatorType.NONE."
                )
            check(contentView !is GradientSeekBar) { "IndicatorSeekBar can not be a contentView for Indicator in case this inflating loop." }
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val layoutParams = MarginLayoutParams(params)
            layoutParams.setMargins(
                layoutParams.leftMargin, layoutParams.topMargin,
                layoutParams.rightMargin, dpToPx(2f) - child.paddingTop
            )
            addView(contentView, index, layoutParams)
            child.showStayIndicator()
        }
    }

    override fun setOrientation(orientation: Int) {
        require(orientation == VERTICAL) {
            ("IndicatorStayLayout is always vertical and does"
                    + " not support horizontal orientation")
        }
        super.setOrientation(orientation)
    }
}