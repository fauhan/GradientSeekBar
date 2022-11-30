package com.inpows.gradientseekbar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CircleBubbleView : View {

    private var mIndicatorTextColor = 0
    private var mIndicatorColor = 0
    private var mIndicatorTextSize = 0f
    private var mContext: Context? = null
    private var mPath: Path? = null
    private var mPaint: Paint? = null
    private var mIndicatorWidth = 0f
    private var mIndicatorHeight = 0f
    private var mTextHeight = 0f
    private var mProgress: String? = null

    constructor(context: Context?) : this(context, null) {
        init(MAX_LENGTH_TEXT)
    }
    constructor(context: Context?, attrs: AttributeSet?) : this(
        context,
        attrs,
        0
    ) {
        init(MAX_LENGTH_TEXT)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(MAX_LENGTH_TEXT)
    }

    internal constructor(
        context: Context?,
        indicatorTextSize: Float,
        indicatorTextColor: Int,
        indicatorColor: Int,
        maxLengthText: String
    ) : super(context, null, 0) {
        mContext = context
        mIndicatorTextSize = indicatorTextSize
        mIndicatorTextColor = indicatorTextColor
        mIndicatorColor = indicatorColor
        init(maxLengthText)
    }

    private fun init(maxLengthText: String) {
        mPaint = Paint()
        mPaint?.isAntiAlias = true
        mPaint?.strokeWidth = 1f
        mPaint?.textAlign = Paint.Align.CENTER
        mPaint?.textSize = mIndicatorTextSize
        val mRect = Rect()
        mPaint?.getTextBounds(maxLengthText, 0, maxLengthText.length, mRect)
        mIndicatorWidth = (mRect.width() + dpToPx(4f)).toFloat()
        val minWidth: Int = dpToPx(36f)
        if (mIndicatorWidth < minWidth) {
            mIndicatorWidth = minWidth.toFloat()
        }
        mTextHeight = mRect.height().toFloat()
        mIndicatorHeight = mIndicatorWidth * 1.2f
        initPath()
    }

    private fun initPath() {
        mPath = Path()
        val rectF = RectF(0f, 0f, mIndicatorWidth, mIndicatorWidth)
        mPath?.arcTo(rectF, 135f, 270f)
        mPath?.lineTo(mIndicatorWidth / 2, mIndicatorHeight)
        mPath?.close()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(mIndicatorWidth.toInt(), mIndicatorHeight.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        mPaint?.apply {
            color = mIndicatorColor
            color = mIndicatorTextColor
            mPath?.let { canvas.drawPath(it, this) }
            canvas.drawText(
                mProgress.orEmpty(),
                mIndicatorWidth / 2f,
                mIndicatorHeight / 2 + mTextHeight / 4,
                this
            )
        }
    }

    fun setProgress(progress: String?) {
        mProgress = progress
        invalidate()
    }

    companion object {
        const val MAX_LENGTH_TEXT = "100"
    }
}