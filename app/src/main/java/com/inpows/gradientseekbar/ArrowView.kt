package com.inpows.gradientseekbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

class ArrowView: View {

    private var mWidth: Float = 0f
    private var mHeight: Float = 0f
    private lateinit var mPath: Path
    private lateinit var mPaint: Paint

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        mWidth = dpToPx(12f).toFloat()
        mHeight = dpToPx(7f).toFloat()
        mPath = Path()
        mPath.moveTo(0f, 0f)
        mPath.lineTo(mWidth, 0f)
        mPath.lineTo(mWidth / 2f, mHeight)
        mPath.close()
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.color = ContextCompat.getColor(context, R.color.blue_50)
        mPaint.strokeWidth = 1f
    }

    fun setColor(color: Int) {
        mPaint.color = color
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(mWidth.toInt(), mHeight.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(mPath, mPaint)
    }
}