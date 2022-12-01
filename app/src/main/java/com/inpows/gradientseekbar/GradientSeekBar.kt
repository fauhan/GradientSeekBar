package com.inpows.gradientseekbar

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import com.inpows.gradientseekbar.FormatUtils.fastFormat
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.roundToInt

class GradientSeekBar : View {

    private var mContext: Context
    private var mStockPaint : Paint? = null
    private var mTextPaint : TextPaint? = null
    private var mSeekChangeListener: OnSeekChangeListener? = null
    private var mRect: Rect? = null
    private var mCustomDrawableMaxHeight = 0f
    private var lastProgress = 0f
    private var mFaultTolerance = -1f
    private var mScreenWidth = -1f
    private var mClearPadding = false
    private var mSeekParams : SeekParams? = null

    //seek bar
    private var mPaddingLeft = 0
    private var mPaddingRight = 0
    private var mMeasuredWidth = 0
    private var mPaddingTop = 0
    private var mSeekLength = 0f
    private var mSeekBlockLength = 0f
    private var mIsTouching = false
    private var mMax = 0f
    private var mMin = 0f
    private var mProgress = 0f
    private var mIsFloatProgress = false
    private var mScale = 1
    private var mUserSeekable = false
    private var mOnlyThumbDraggable = false
    private var mSeekSmoothly = false
    private var mProgressArr : FloatArray = floatArrayOf()
    private var mR2L = false

    //tick texts
    private var mShowTickText = false
    private var mShowBothTickTextsOnly = false
    private var mTickTextsHeight = 0
    private var mTickTextsArr : Array<String>? = null
    private var mTickTextsWidth : FloatArray = floatArrayOf()
    private var mTextCenterX : FloatArray = floatArrayOf()
    private var mTickTextY = 0f
    private var mTickTextsSize = 0
    private var mTextsTypeface : Typeface? = null
    private var mSelectedTextsColor = 0
    private var mUnselectedTextsColor = 0
    private var mHoveredTextColor = 0
    private var mTickTextsCustomArray: Array<String>? = null

    //indicator
    private var mIndicator : Indicator? = null
    private var mIndicatorColor = 0
    private var mIndicatorTextColor = 0
    private var mIndicatorStayAlways = false
    private var mIndicatorTextSize = 0
    private var mIndicatorContentView : View? = null
    private var mIndicatorTopContentView : View? = null
    private var mShowIndicatorType = 0
    private var mIndicatorTextFormat: String? = null

    //tick marks
    private var mTickMarksX : FloatArray? = floatArrayOf()
    private var mTicksCount = 0
    private var mUnSelectedTickMarksColor = 0
    private var mSelectedTickMarksColor = 0
    private var mTickRadius = 0f
    private var mUnselectTickMarksBitmap : Bitmap? = null
    private var mSelectTickMarksBitmap : Bitmap? = null
    private var mTickMarksDrawable: Drawable? = null
    private var mShowTickMarksType = 0
    private var mTickMarksEndsHide = false
    private var mTickMarksSweptHide = false
    private var mTickMarksSize = 0

    //track
    private var mTrackRoundedCorners = false
    private var mProgressTrack : RectF? = null
    private var mBackgroundTrack : RectF? = null
    private var mBackgroundTrackSize = 0
    private var mProgressTrackSize = 0
    private var mBackgroundTrackColor = 0
    private var mProgressTrackColor = 0
    private var mSectionTrackColorArray : IntArray = intArrayOf()
    private var mCustomTrackSectionColorResult = false
    private var mTrackBackgroundType = 0

    //thumb
    private var mThumbRadius = 0f
    private var mThumbTouchRadius = 0f
    private var mThumbBitmap : Bitmap? = null
    private var mThumbColor = 0
    private var mThumbSize = 0
    private var mThumbDrawable: Drawable? = null
    private var mPressedThumbBitmap : Bitmap? = null
    private var mPressedThumbColor = 0

    //thumb text
    private var mShowThumbText = false
    private var mThumbTextY = 0f
    private var mThumbTextColor = 0
    private var mHideThumb = false
    private var mAdjustAuto = false

    //ruler
    private var mRulerSize = 0
    private var mRulerWidth = 0
    private var mRulerOffset = 0
    private var mRulerColor = 0

    constructor(context: Context) : this(context, null) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        initAttrs(mContext, attrs)
        initParams()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mContext = context
        initAttrs(mContext, attrs)
        initParams()
    }

    internal constructor(builder: Builder) : super(builder.context) {
        mContext = builder.context
        val defaultPadding: Int = dpToPx(16f)
        setPadding(defaultPadding, paddingTop, defaultPadding, paddingBottom)
        this.apply(builder)
        initParams()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val builder = Builder(context)
        if (attrs == null) {
            this.apply(builder)
            return
        }
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.GradientSeekBar)
        // seekBar
        mMax = ta.getFloat(R.styleable.GradientSeekBar_gsb_max, builder.max)
        mMin = ta.getFloat(R.styleable.GradientSeekBar_gsb_min, builder.min)
        mProgress = ta.getFloat(R.styleable.GradientSeekBar_gsb_progress, builder.progress)
        mIsFloatProgress = ta.getBoolean(
            R.styleable.GradientSeekBar_gsb_progress_value_float,
            builder.progressValueFloat
        )
        mUserSeekable =
            ta.getBoolean(R.styleable.GradientSeekBar_gsb_user_seekable, builder.userSeekable)
        mClearPadding = ta.getBoolean(
            R.styleable.GradientSeekBar_gsb_clear_default_padding,
            builder.clearPadding
        )
        mOnlyThumbDraggable = ta.getBoolean(
            R.styleable.GradientSeekBar_gsb_only_thumb_draggable,
            builder.onlyThumbDraggable
        )
        mSeekSmoothly =
            ta.getBoolean(R.styleable.GradientSeekBar_gsb_seek_smoothly, builder.seekSmoothly)
        mR2L = ta.getBoolean(R.styleable.GradientSeekBar_gsb_r2l, builder.r2l)
        // track
        mBackgroundTrackSize = ta.getDimensionPixelSize(
            R.styleable.GradientSeekBar_gsb_track_background_size,
            builder.trackBackgroundSize
        )
        mProgressTrackSize = ta.getDimensionPixelSize(
            R.styleable.GradientSeekBar_gsb_track_progress_size,
            builder.trackProgressSize
        )
        mBackgroundTrackColor = ta.getColor(
            R.styleable.GradientSeekBar_gsb_track_background_color,
            builder.trackBackgroundColor
        )
        mProgressTrackColor = ta.getColor(
            R.styleable.GradientSeekBar_gsb_track_progress_color,
            builder.trackProgressColor
        )
        mTrackRoundedCorners = ta.getBoolean(
            R.styleable.GradientSeekBar_gsb_track_rounded_corners,
            builder.trackRoundedCorners
        )
        mTrackBackgroundType =
            ta.getInt(R.styleable.GradientSeekBar_gsb_track_background_type, builder.trackBackgroundType)
        // thumb
        mThumbSize =
            ta.getDimensionPixelSize(R.styleable.GradientSeekBar_gsb_thumb_size, builder.thumbSize)
        mThumbDrawable = ta.getDrawable(R.styleable.GradientSeekBar_gsb_thumb_drawable)
        mAdjustAuto = ta.getBoolean(R.styleable.GradientSeekBar_gsb_thumb_adjust_auto, true)
        initThumbColor(
            ta.getColorStateList(R.styleable.GradientSeekBar_gsb_thumb_color),
            builder.thumbColor
        )
        // thumb text
        mShowThumbText =
            ta.getBoolean(R.styleable.GradientSeekBar_gsb_show_thumb_text, builder.showThumbText)
        mThumbTextColor =
            ta.getColor(R.styleable.GradientSeekBar_gsb_thumb_text_color, builder.thumbTextColor)
        // tickMarks
        mTicksCount = ta.getInt(R.styleable.GradientSeekBar_gsb_ticks_count, builder.tickCount)
        mShowTickMarksType = ta.getInt(
            R.styleable.GradientSeekBar_gsb_show_tick_marks_type,
            builder.showTickMarksType
        )
        mTickMarksSize = ta.getDimensionPixelSize(
            R.styleable.GradientSeekBar_gsb_tick_marks_size,
            builder.tickMarksSize
        )
        initTickMarksColor(
            ta.getColorStateList(R.styleable.GradientSeekBar_gsb_tick_marks_color),
            builder.tickMarksColor
        )
        mTickMarksDrawable = ta.getDrawable(R.styleable.GradientSeekBar_gsb_tick_marks_drawable)
        mTickMarksSweptHide = ta.getBoolean(
            R.styleable.GradientSeekBar_gsb_tick_marks_swept_hide,
            builder.tickMarksSweptHide
        )
        mTickMarksEndsHide = ta.getBoolean(
            R.styleable.GradientSeekBar_gsb_tick_marks_ends_hide,
            builder.tickMarksEndsHide
        )
        // tickTexts
        mShowTickText =
            ta.getBoolean(R.styleable.GradientSeekBar_gsb_show_tick_texts, builder.showTickText)
        mTickTextsSize = ta.getDimensionPixelSize(
            R.styleable.GradientSeekBar_gsb_tick_texts_size,
            builder.tickTextsSize
        )
        initTickTextsColor(
            ta.getColorStateList(R.styleable.GradientSeekBar_gsb_tick_texts_color),
            builder.tickTextsColor
        )
        val tickTextCharSequences = ta.getTextArray(R.styleable.GradientSeekBar_gsb_tick_texts_array)?.joinToString(
            separator = "|~|"
        )
        var tickTextArray = arrayOf<String>()
        tickTextCharSequences?.split("|~|")?.forEach {
            tickTextArray += it
        }
        mTickTextsCustomArray = tickTextArray

        initTextsTypeface(
            ta.getInt(R.styleable.GradientSeekBar_gsb_tick_texts_typeface, -1),
            builder.tickTextsTypeFace
        )
        // indicator
        mShowIndicatorType =
            ta.getInt(R.styleable.GradientSeekBar_gsb_show_indicator, builder.showIndicatorType)
        mIndicatorColor =
            ta.getColor(R.styleable.GradientSeekBar_gsb_indicator_color, builder.indicatorColor)
        mIndicatorTextSize = ta.getDimensionPixelSize(
            R.styleable.GradientSeekBar_gsb_indicator_text_size,
            builder.indicatorTextSize
        )
        mIndicatorTextColor = ta.getColor(
            R.styleable.GradientSeekBar_gsb_indicator_text_color,
            builder.indicatorTextColor
        )
        val indicatorContentViewId =
            ta.getResourceId(R.styleable.GradientSeekBar_gsb_indicator_content_layout, 0)
        if (indicatorContentViewId > 0) {
            mIndicatorContentView = inflate(mContext, indicatorContentViewId, null)
        }
        val indicatorTopContentLayoutId =
            ta.getResourceId(R.styleable.GradientSeekBar_gsb_indicator_top_content_layout, 0)
        if (indicatorTopContentLayoutId > 0) {
            mIndicatorTopContentView = inflate(mContext, indicatorTopContentLayoutId, null)
        }

        mRulerSize = ta.getDimensionPixelSize(R.styleable.GradientSeekBar_gsb_ruler_size, builder.mRulerSize)
        mRulerWidth = ta.getDimensionPixelSize(R.styleable.GradientSeekBar_gsb_ruler_width, builder.mRulerWidth)
        mRulerOffset = ta.getDimensionPixelSize(R.styleable.GradientSeekBar_gsb_ruler_offset, builder.mRulerOffset)
        mRulerColor = ta.getColor(R.styleable.GradientSeekBar_gsb_ruler_color, builder.mRulerColor)
        ta.recycle()
    }

    private fun initParams() {
        initProgressRangeValue()
        if (mBackgroundTrackSize > mProgressTrackSize) {
            mBackgroundTrackSize = mProgressTrackSize
        }
        if (mThumbDrawable == null) {
            mThumbRadius = mThumbSize / 2.0f
            mThumbTouchRadius = mThumbRadius * 1.2f
        } else {
            mThumbRadius = dpToPx(THUMB_MAX_WIDTH.toFloat()).coerceAtMost(mThumbSize) / 2.0f
            mThumbTouchRadius = mThumbRadius
        }
        mTickRadius = if (mTickMarksDrawable == null) {
            mTickMarksSize / 2.0f
        } else {
            dpToPx(THUMB_MAX_WIDTH.toFloat()).coerceAtMost(mTickMarksSize) / 2.0f
        }
        mCustomDrawableMaxHeight = mTickMarksSize.toFloat()
        initStrokePaint()
        measureTickTextsBonds()
        lastProgress = mProgress
        collectTicksInfo()
        mProgressTrack = RectF()
        mBackgroundTrack = RectF()
        initDefaultPadding()
        initIndicatorContentView()
    }

    private fun collectTicksInfo() {
        require(!(mTicksCount < 0 || mTicksCount > 50)) { "the Argument: TICK COUNT must be limited between (0-50), Now is $mTicksCount" }
        if (mTicksCount != 0) {
            mTickMarksX = FloatArray(mTicksCount)
            if (mShowTickText) {
                mTextCenterX = FloatArray(mTicksCount)
                mTickTextsWidth = FloatArray(mTicksCount)
            }
            mProgressArr = FloatArray(mTicksCount)
            for (i in mProgressArr.indices) {
                mProgressArr[i] =
                    mMin + i * (mMax - mMin) / if (mTicksCount - 1 > 0) mTicksCount - 1 else 1
            }
        }
    }

    private fun initDefaultPadding() {
        if (!mClearPadding) {
            val normalPadding: Int = dpToPx(16f)
            if (paddingLeft == 0) {
                setPadding(normalPadding, paddingTop, paddingRight, paddingBottom)
            }
            if (paddingRight == 0) {
                setPadding(
                    paddingLeft,
                    paddingTop,
                    normalPadding,
                    paddingBottom
                )
            }
        }
    }

    private fun initProgressRangeValue() {
        require(mMax >= mMin) { "the Argument: MAX's value must be larger than MIN's." }
        if (mProgress < mMin) {
            mProgress = mMin
        }
        if (mProgress > mMax) {
            mProgress = mMax
        }
    }

    private fun initStrokePaint() {
        if (mStockPaint == null) {
            mStockPaint = Paint()
        }
        if (mTrackRoundedCorners) {
            mStockPaint?.strokeCap = Paint.Cap.ROUND
        }
        mStockPaint?.isAntiAlias = true
        if (mBackgroundTrackSize > mProgressTrackSize) {
            mProgressTrackSize = mBackgroundTrackSize
        }
    }

    private fun measureTickTextsBonds() {
        if (needDrawText()) {
            initTextPaint()
            mTextPaint?.typeface = mTextsTypeface
            mTextPaint?.getTextBounds("j", 0, 1, mRect)
            mTickTextsHeight = (mRect?.height() ?: 0) + dpToPx(3f) //with the gap(3dp) between tickTexts and track.
        }
    }

    private fun needDrawText(): Boolean {
        return mShowThumbText || mTicksCount != 0 && mShowTickText
    }

    private fun initTextPaint() {
        if (mTextPaint == null) {
            mTextPaint = TextPaint()
            mTextPaint?.isAntiAlias = true
            mTextPaint?.textAlign = Paint.Align.CENTER
            mTextPaint?.textSize = mTickTextsSize.toFloat()
        }
        if (mRect == null) {
            mRect = Rect()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height =
            (mCustomDrawableMaxHeight + paddingTop + paddingBottom).roundToInt()
        setMeasuredDimension(
            resolveSize(dpToPx(170f), widthMeasureSpec),
            height + mTickTextsHeight
        )
        initSeekBarInfo()
        refreshSeekBarLocation()
    }

    private fun initSeekBarInfo() {
        mMeasuredWidth = measuredWidth
        mPaddingLeft = paddingStart
        mPaddingRight = paddingEnd
        mPaddingTop = paddingTop
        mSeekLength = (mMeasuredWidth - mPaddingLeft - mPaddingRight).toFloat()
        mSeekBlockLength = mSeekLength / if (mTicksCount - 1 > 0) mTicksCount - 1 else 1
    }

    private fun refreshSeekBarLocation() {
        initTrackLocation()
        //init TickTexts Y Location
        if (needDrawText()) {
            mTextPaint?.run {
                getTextBounds("j", 0, 1, mRect)
                mTickTextY =
                    mPaddingTop + mCustomDrawableMaxHeight + ((mRect?.height()
                        ?: 0) - descent()).roundToInt() + dpToPx(3f)
                mThumbTextY = mTickTextY
            }
        }
        //init tick's X and text's X location;
        if (mTickMarksX == null) {
            return
        }
        initTextsArray()
        //adjust thumb auto,so find out the closest progress in the mProgressArr array and replace it.
        //it is not necessary to adjust thumb while count is less than 2.
        if (mTicksCount > 2) {
            mProgress = mProgressArr[getClosestIndex()]
            lastProgress = mProgress
        }
        refreshThumbCenterXByProgress(mProgress)
    }

    private fun initTextsArray() {
        if (mTicksCount == 0) {
            return
        }
        if (mShowTickText) {
            mTickTextsArr = arrayOf(mTicksCount.toString())
        }
        mTickMarksX?.run {
            for (i in indices) {
                if (mShowTickText) {
                    mTickTextsArr?.let {
                        it[i] = getTickTextByPosition(i)
                        mTextPaint?.getTextBounds(it[i], 0, it[i].length, mRect)
                    }
                    mTickTextsWidth[i] = mRect?.width()?.toFloat() ?: 0f
                    mTextCenterX[i] = mPaddingLeft + mSeekBlockLength * i
                }
                this[i] = mPaddingLeft + mSeekBlockLength * i
            }
        }
    }

    private fun initTrackLocation() {
        if (mR2L) {
            mBackgroundTrack?.left = mPaddingLeft.toFloat()
            mBackgroundTrack?.top = mPaddingTop + mThumbTouchRadius
            //ThumbCenterX
            mBackgroundTrack?.right =
                mPaddingLeft + mSeekLength * (1.0f - (mProgress - mMin) / getAmplitude())
            mBackgroundTrack?.bottom = mBackgroundTrack?.top
            //ThumbCenterX
            mProgressTrack?.left = mBackgroundTrack?.right
            mProgressTrack?.top = mBackgroundTrack?.top
            mProgressTrack?.right = (mMeasuredWidth - mPaddingRight).toFloat()
            mProgressTrack?.bottom = mBackgroundTrack?.bottom
        } else {
            mProgressTrack?.left = mPaddingLeft.toFloat()
            mProgressTrack?.top = mPaddingTop + mThumbTouchRadius
            //ThumbCenterX
            mProgressTrack?.right =
                (mProgress - mMin) * mSeekLength / getAmplitude() + mPaddingLeft
            mProgressTrack?.bottom = mProgressTrack?.top
            //ThumbCenterX
            mBackgroundTrack?.left = mProgressTrack?.right
            mBackgroundTrack?.top = mProgressTrack?.bottom
            mBackgroundTrack?.right = (mMeasuredWidth - mPaddingRight).toFloat()
            mBackgroundTrack?.bottom = mProgressTrack?.bottom
        }
    }

    private fun getTickTextByPosition(index: Int): String {
        return mTickTextsCustomArray?.let {
            if (index < it.size) {
                it[index]
            } else ""
        } ?: run {
            return getProgressString(mProgressArr[index])
        }
    }

    private fun refreshThumbCenterXByProgress(progress: Float) {
        //ThumbCenterX
        if (mR2L) {
            mBackgroundTrack?.right =
                mPaddingLeft + mSeekLength * (1.0f - (progress - mMin) / getAmplitude()) //ThumbCenterX
            mProgressTrack?.left = mBackgroundTrack?.right
        } else {
            mProgressTrack?.right = (progress - mMin) * mSeekLength / getAmplitude() + mPaddingLeft
            mBackgroundTrack?.left = mProgressTrack?.right
        }
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        drawTrack(canvas)
        drawTickMarks(canvas)
        drawTickTexts(canvas)
        drawThumb(canvas)
        drawThumbText(canvas)
    }

    private fun drawTrack(canvas: Canvas) {
        mStockPaint?.run {
            if (mCustomTrackSectionColorResult) { //the track has custom the section track color
                val sectionSize = if (mTicksCount - 1 > 0) mTicksCount - 1 else 1
                for (i in 0 until sectionSize) {
                    when(mR2L){
                        true -> {
                            mStockPaint?.color = mSectionTrackColorArray[sectionSize - i - 1]
                        }
                        false -> {
                            mStockPaint?.color = mSectionTrackColorArray[i]
                        }
                    }
                    val thumbPosFloat = getThumbPosOnTickFloat()
                    mTickMarksX?.let { tickMarksX ->
                        mProgressTrack?.let { progressTrack ->
                            if (i < thumbPosFloat && thumbPosFloat < i + 1) {
                                // the section track include the thumb,
                                // set the ProgressTrackSize for thumb's left side track ,
                                // BGTrackSize for the right's.
                                val thumbCenterX = getThumbCenterX()
                                mStockPaint?.strokeWidth = getLeftSideTrackSize()
                                canvas.drawLine(
                                    tickMarksX[i],
                                    progressTrack.top,
                                    thumbCenterX,
                                    progressTrack.bottom,
                                    this
                                )
                                mStockPaint?.strokeWidth = getRightSideTrackSize()
                                canvas.drawLine(
                                    thumbCenterX, progressTrack.top,
                                    tickMarksX[i + 1], progressTrack.bottom, this
                                )
                            } else {
                                strokeWidth = if (i < thumbPosFloat) {
                                    getLeftSideTrackSize()
                                } else {
                                    getRightSideTrackSize()
                                }
                                canvas.drawLine(
                                    tickMarksX[i], progressTrack.top,
                                    tickMarksX[i + 1], progressTrack.bottom, this
                                )
                            }
                        }
                    }
                }
            }
            else {
                //draw progress track
                if(mTrackBackgroundType == BackgroundType.GRADIENT_COLOR){
                    val colors = intArrayOf(
                        Color.RED,
                        Color.YELLOW,
                        Color.GREEN
                    )
                    val positions: FloatArray? = null
                    val gradientShader: Shader =
                        LinearGradient(
                            0f,
                            0f,
                            width.toFloat(),
                            0f,
                            colors,
                            positions,
                            Shader.TileMode.MIRROR
                        )
                    mStockPaint?.apply {
                        if (mTrackRoundedCorners) {
                            strokeCap = Paint.Cap.ROUND
                        }
                        shader = gradientShader
                        color = mProgressTrackColor
                        strokeWidth = mProgressTrackSize.toFloat()
                        mProgressTrack?.apply {
                            canvas.drawLine(
                                left,
                                top,
                                right,
                                bottom,
                                this@run
                            )
                        }
                        strokeWidth = mBackgroundTrackSize.toFloat()
                        color = mBackgroundTrackColor
                        mBackgroundTrack?.apply {
                            canvas.drawLine(
                                left,
                                top,
                                right,
                                bottom,
                                this@run
                            )
                        }
                    }
                } else {
                    mStockPaint?.apply {
                        strokeWidth = mProgressTrackSize.toFloat()
                        color = mProgressTrackColor
                        if (mTrackRoundedCorners) {
                            strokeCap = Paint.Cap.ROUND
                        }
                        mProgressTrack?.let { rectF ->
                            canvas.drawLine(
                                rectF.left,
                                rectF.top,
                                rectF.right,
                                rectF.bottom,
                                this@run
                            )
                        }
                        //draw BG track
                        color = mBackgroundTrackColor
                        strokeWidth = mBackgroundTrackSize.toFloat()
                        mBackgroundTrack?.apply {
                            canvas.drawLine(
                                left,
                                top,
                                right,
                                bottom,
                                this@run
                            )
                        }
                    }
                }
            }
        }
    }

    private fun drawTickMarks(canvas: Canvas) {
        if (mTicksCount == 0 || mShowTickMarksType == TickMarkType.NONE && mTickMarksDrawable == null) {
            return
        }
        val thumbCenterX = getThumbCenterX()
        mTickMarksX?.let { tickMarksX ->
            for (i in tickMarksX.indices) {
                val thumbPosFloat = getThumbPosOnTickFloat()
                if (mTickMarksSweptHide) {
                    if (thumbCenterX >= tickMarksX[i]) {
                        continue
                    }
                }
                if (mTickMarksEndsHide) {
                    if (i == 0 || i == tickMarksX.size - 1) {
                        continue
                    }
                }
                if (i == getThumbPosOnTick() && mTicksCount > 2 && !mSeekSmoothly) {
                    continue
                }
                if (i <= thumbPosFloat) {
                    mStockPaint?.color = getLeftSideTickColor()
                } else {
                    mStockPaint?.color = getRightSideTickColor()
                }
                if (mTickMarksDrawable != null) {
                    if (mSelectTickMarksBitmap == null || mUnselectTickMarksBitmap == null) {
                        initTickMarksBitmap()
                    }
                    require(!(mSelectTickMarksBitmap == null || mUnselectTickMarksBitmap == null)) {
                        //please check your selector drawable's format and correct.
                        "the format of the selector TickMarks drawable is wrong!"
                    }
                    mSelectTickMarksBitmap?.let { selectTickMarkBitmap ->
                        mUnselectTickMarksBitmap?.let { unselectTickMarksBitmap ->
                            mProgressTrack?.let { progressTrack ->
                                if (i <= thumbPosFloat) {
                                    canvas.drawBitmap(
                                        selectTickMarkBitmap,
                                        tickMarksX[i] - unselectTickMarksBitmap.width / 2.0f,
                                        progressTrack.top - unselectTickMarksBitmap.height / 2.0f,
                                        mStockPaint
                                    )
                                } else {
                                    canvas.drawBitmap(
                                        selectTickMarkBitmap,
                                        tickMarksX[i] - unselectTickMarksBitmap.width / 2.0f,
                                        progressTrack.top - unselectTickMarksBitmap.height / 2.0f,
                                        mStockPaint
                                    )
                                }
                            }
                        }
                    }
                    continue
                }
                mStockPaint?.apply {
                    mProgressTrack?.let { progressTrack ->
                        when (mShowTickMarksType) {
                            TickMarkType.OVAL -> {
                                val acc = Paint()
                                acc.color = Color.GRAY

                                canvas.drawCircle(tickMarksX[i], progressTrack.top, mTickRadius, acc)
                            }
                            TickMarkType.DIVIDER -> {
                                val rectWidth: Int = dpToPx(1.1f)
                                val dividerTickHeight: Float = if (thumbCenterX >= tickMarksX[i]) {
                                    getLeftSideTrackSize()
                                } else {
                                    getRightSideTrackSize()
                                }
                                val acc = Paint()
                                acc.color = Color.LTGRAY
                                canvas.drawRect(
                                    tickMarksX[i] - rectWidth,
                                    progressTrack.top - dividerTickHeight / 2.0f,
                                    tickMarksX[i] + rectWidth,
                                    progressTrack.top + dividerTickHeight / 2.0f,
                                    acc
                                )
                            }
                            TickMarkType.RULER -> {
                                val dividerTickHeight: Float = if (thumbCenterX >= tickMarksX[i]) {
                                    getLeftSideTrackSize()
                                } else {
                                    getRightSideTrackSize()
                                }
                                val acc = Paint()
                                acc.color = mRulerColor
                                acc.strokeWidth = mRulerWidth.toFloat()

                                canvas.drawLine(
                                    tickMarksX[i] - mRulerWidth.toFloat(),
                                    (progressTrack.top - dividerTickHeight / 2.0f) + (mRulerOffset.toFloat() - mRulerSize.toFloat() * 2.0f),
                                    tickMarksX[i] - mRulerWidth.toFloat(),
                                    mRulerOffset.toFloat() + mRulerSize.toFloat(),
                                    acc)
                            }
                            TickMarkType.SQUARE -> {
                                canvas.drawRect(
                                    tickMarksX[i] - mTickMarksSize / 2.0f,
                                    progressTrack.top - mTickMarksSize / 2.0f,
                                    tickMarksX[i] + mTickMarksSize / 2.0f,
                                    progressTrack.top + mTickMarksSize / 2.0f,
                                    this
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun drawTickTexts(canvas: Canvas) {
        mTickTextsArr?.let { tickTextArr ->
            val thumbPosFloat = getThumbPosOnTickFloat()
            for (i in tickTextArr.indices) {
                if (mShowBothTickTextsOnly) {
                    if (i != 0 && i != tickTextArr.size - 1) {
                        continue
                    }
                }
                mTextPaint?.apply {
                    color = if (i == getThumbPosOnTick() && i.toFloat() == thumbPosFloat) {
                        mHoveredTextColor
                    } else if (i < thumbPosFloat) {
                        getLeftSideTickTextsColor()
                    } else {
                        getRightSideTickTextsColor()
                    }
                    var index = i
                    if (mR2L) {
                        mTickTextsArr?.size?.let { arrSize ->
                            index = arrSize - i - 1
                        }
                    }
                    when (i) {
                        0 -> {
                            canvas.drawText(
                                tickTextArr[index],
                                mTextCenterX[i] + mTickTextsWidth[index] / 2.0f,
                                mTickTextY,
                                this
                            )
                        }
                        tickTextArr.size - 1 -> {
                            canvas.drawText(
                                tickTextArr[index],
                                mTextCenterX[i] - mTickTextsWidth[index] / 2.0f,
                                mTickTextY,
                                this
                            )
                        }
                        else -> {
                            canvas.drawText(tickTextArr[index], mTextCenterX[i], mTickTextY, this)
                        }
                    }
                }
            }
        }
    }

    private fun drawThumb(canvas: Canvas) {
        if (mHideThumb) {
            return
        }
        val thumbCenterX = getThumbCenterX()
        if (mThumbDrawable != null) { //check user has set thumb drawable or not.ThumbDrawable first, thumb color for later.
            if (mThumbBitmap == null || mPressedThumbBitmap == null) {
                initThumbBitmap()
            }
            require(!(mThumbBitmap == null || mPressedThumbBitmap == null)) {
                //please check your selector drawable's format and correct.
                "the format of the selector thumb drawable is wrong!"
            }
            val paint = Paint()
            paint.alpha = 255
            if (mIsTouching) {
                mPressedThumbBitmap?.let { pressedThumbBitmap ->
                    mProgressTrack?.let { progressTrack ->
                        canvas.drawBitmap(
                            pressedThumbBitmap,
                            thumbCenterX - pressedThumbBitmap.width / 2.0f,
                            progressTrack.top - pressedThumbBitmap.height / 2.0f,
                            paint
                        )
                    }
                }
            } else {
                mThumbBitmap?.let { thumbBitmap ->
                    mProgressTrack?.let { progressTrack ->
                        canvas.drawBitmap(
                            thumbBitmap,
                            thumbCenterX - thumbBitmap.width / 2.0f,
                            progressTrack.top - thumbBitmap.height / 2.0f,
                            paint
                        )
                    }
                }
            }
        } else {
            mStockPaint?.color = if(mIsTouching) mPressedThumbColor else mThumbColor
            mStockPaint?.let { stockPaint ->
                mProgressTrack?.let { progressTrack ->
                    canvas.drawCircle(
                        thumbCenterX,
                        progressTrack.top,
                        if (mIsTouching) mThumbTouchRadius else mThumbRadius,
                        stockPaint
                    )
                }
            }
        }
    }

    private fun drawThumbText(canvas: Canvas) {
        if (!mShowThumbText || mShowTickText && mTicksCount > 2) {
            return
        }
        mTextPaint?.run {
            color = mThumbTextColor
            canvas.drawText(getProgressString(mProgress), getThumbCenterX(), mThumbTextY, this)
        }
    }

    private fun getThumbCenterX(): Float {
        return if (mR2L) {
            mBackgroundTrack?.right ?: 0f
        } else mProgressTrack?.right ?: 0f
    }

    private fun getLeftSideTickColor(): Int {
        return if (mR2L) {
            mUnSelectedTickMarksColor
        } else mSelectedTickMarksColor
    }

    private fun getRightSideTickColor(): Int {
        return if (mR2L) {
            mSelectedTickMarksColor
        } else mUnSelectedTickMarksColor
    }

    private fun getLeftSideTickTextsColor(): Int {
        return if (mR2L) {
            mUnselectedTextsColor
        } else mSelectedTextsColor
    }

    private fun getRightSideTickTextsColor(): Int {
        return if (mR2L) {
            mSelectedTextsColor
        } else mUnselectedTextsColor
    }

    private fun getLeftSideTrackSize(): Float {
        return if (mR2L) {
            mBackgroundTrackSize.toFloat()
        } else mProgressTrackSize.toFloat()
    }

    private fun getRightSideTrackSize(): Float {
        return if (mR2L) {
            mProgressTrackSize.toFloat()
        } else mBackgroundTrackSize.toFloat()
    }

    private fun getThumbPosOnTick(): Int {
        return if (mTicksCount != 0) {
            ((getThumbCenterX() - mPaddingLeft) / mSeekBlockLength).roundToInt()
        } else 0
        // when tick count = 0 ; seek bar has not tick(continuous series), return 0;
    }

    private fun getThumbPosOnTickFloat(): Float {
        return if (mTicksCount != 0) {
            (getThumbCenterX() - mPaddingLeft) / mSeekBlockLength
        } else 0f
    }

    private fun getHeightByRatio(drawable: Drawable, width: Int): Int {
        val intrinsicWidth = drawable.intrinsicWidth
        val intrinsicHeight = drawable.intrinsicHeight
        return (1.0f * width * intrinsicHeight / intrinsicWidth).roundToInt()
    }

    private fun getDrawBitmap(drawable: Drawable?, isThumb: Boolean): Bitmap? {
        if (drawable == null) {
            return null
        }
        val width: Int = if (isThumb) {
                mThumbSize
            } else {
                mTickMarksSize
            }
        val height: Int = if (isThumb) {
                mThumbSize
            } else {
                mTickMarksSize
            }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun initThumbColor(colorStateList: ColorStateList?, defaultColor: Int) {
        //if you didn't set the thumb color, set a default color.
        if (colorStateList == null) {
            mThumbColor = defaultColor
            mPressedThumbColor = mThumbColor
            return
        }
        var states: Array<IntArray>? = null
        var colors: IntArray? = null
        val aClass: Class<out ColorStateList> = colorStateList.javaClass
        try {
            val f: Array<Field> = aClass.declaredFields
            for (field in f) {
                field.isAccessible = true
                if ("mStateSpecs" == field.name) {
                    states = field.get(colorStateList) as Array<IntArray>?
                }
                if ("mColors" == field.name) {
                    colors = field.get(colorStateList) as IntArray?
                }
            }
            if (states == null || colors == null) {
                return
            }
        } catch (e: Exception) {
            throw RuntimeException("Something wrong happened when parseing thumb selector color.")
        }
        if (states.size == 1) {
            mThumbColor = colors[0]
            mPressedThumbColor = mThumbColor
        } else if (states.size == 2) {
            for (i in states.indices) {
                val attr = states[i]
                if (attr.isEmpty()) { // didn't have state,so just get color.
                    mPressedThumbColor = colors[i]
                    continue
                }
                mThumbColor = when (attr[0]) {
                    android.R.attr.state_pressed -> colors[i]
                    else -> throw IllegalArgumentException("the selector color file you set for the argument: gsb_thumb_color is in wrong format.")
                }
            }
        } else {
            //the color selector file was set by a wrong format , please see above to correct.
            throw IllegalArgumentException("the selector color file you set for the argument: gsb_thumb_color is in wrong format.")
        }
    }

    private fun initTickMarksColor(colorStateList: ColorStateList?, defaultColor: Int) {
        //if you didn't set the tick's text color, set a default selector color file.
        if (colorStateList == null) {
            mSelectedTickMarksColor = defaultColor
            mUnSelectedTickMarksColor = mSelectedTickMarksColor
            return
        }
        var states: Array<IntArray>? = null
        var colors: IntArray? = null
        val aClass: Class<out ColorStateList> = colorStateList.javaClass
        try {
            val f: Array<Field> = aClass.declaredFields
            for (field in f) {
                field.isAccessible = true
                if ("mStateSpecs" == field.name) {
                    states = field.get(colorStateList) as Array<IntArray>?
                }
                if ("mColors" == field.name) {
                    colors = field.get(colorStateList) as IntArray?
                }
            }
            if (states == null || colors == null) {
                return
            }
        } catch (e: Exception) {
            throw RuntimeException("Something wrong happened when parsing thumb selector color." + e.message)
        }
        if (states.size == 1) {
            mSelectedTickMarksColor = colors[0]
            mUnSelectedTickMarksColor = mSelectedTickMarksColor
        } else if (states.size == 2) {
            for (i in states.indices) {
                val attr = states[i]
                if (attr.isEmpty()) { //didn't have state,so just get color.
                    mUnSelectedTickMarksColor = colors[i]
                    continue
                }
                mSelectedTickMarksColor = when (attr[0]) {
                    android.R.attr.state_selected -> colors[i]
                    else -> throw IllegalArgumentException("the selector color file you set for the argument: gsb_tick_marks_color is in wrong format.")
                }
            }
        } else {
            //the color selector file was set by a wrong format , please see above to correct.
            throw IllegalArgumentException("the selector color file you set for the argument: gsb_tick_marks_color is in wrong format.")
        }
    }

    private fun initTickTextsColor(colorStateList: ColorStateList?, defaultColor: Int) {
        //if you didn't set the tick's texts color, will be set a selector color file default.
        if (colorStateList == null) {
            mUnselectedTextsColor = defaultColor
            mSelectedTextsColor = mUnselectedTextsColor
            mHoveredTextColor = mUnselectedTextsColor
            return
        }
        var states: Array<IntArray>? = null
        var colors: IntArray? = null
        val aClass: Class<out ColorStateList> = colorStateList.javaClass
        try {
            val f: Array<Field> = aClass.declaredFields
            for (field in f) {
                field.isAccessible = true
                if ("mStateSpecs" == field.name) {
                    states = field.get(colorStateList) as Array<IntArray>?
                }
                if ("mColors" == field.name) {
                    colors = field.get(colorStateList) as IntArray?
                }
            }
            if (states == null || colors == null) {
                return
            }
        } catch (e: Exception) {
            throw RuntimeException("Something wrong happened when parseing thumb selector color.")
        }
        if (states.size == 1) {
            mUnselectedTextsColor = colors[0]
            mSelectedTextsColor = mUnselectedTextsColor
            mHoveredTextColor = mUnselectedTextsColor
        } else if (states.size == 3) {
            for (i in states.indices) {
                val attr = states[i]
                if (attr.isEmpty()) { //didn't have state,so just get color.
                    mUnselectedTextsColor = colors[i]
                    continue
                }
                when (attr[0]) {
                    android.R.attr.state_selected -> mSelectedTextsColor = colors[i]
                    android.R.attr.state_hovered -> mHoveredTextColor = colors[i]
                    else -> throw IllegalArgumentException("the selector color file you set for the argument: gsb_tick_texts_color is in wrong format.")
                }
            }
        } else {
            //the color selector file was set by a wrong format , please see above to correct.
            throw IllegalArgumentException("the selector color file you set for the argument: gsb_tick_texts_color is in wrong format.")
        }
    }

    private fun initTextsTypeface(typeface: Int, defaultTypeface: Typeface?) {
        when (typeface) {
            0 -> mTextsTypeface = Typeface.DEFAULT
            1 -> mTextsTypeface = Typeface.MONOSPACE
            2 -> mTextsTypeface = Typeface.SANS_SERIF
            3 -> mTextsTypeface = Typeface.SERIF
            else -> {
                if (defaultTypeface == null) {
                    mTextsTypeface = Typeface.DEFAULT
                }
                mTextsTypeface = defaultTypeface
            }
        }
    }

    private fun initThumbBitmap() {
        if (mThumbDrawable == null) {
            return
        }
        if (mThumbDrawable is StateListDrawable) {
            try {
                val listDrawable = mThumbDrawable as StateListDrawable
                val aClass: Class<out StateListDrawable> = listDrawable.javaClass
                val stateCount = aClass.getMethod("getStateCount").invoke(listDrawable) as Int
                if (stateCount == 2) {
                    val getStateSet: Method =
                        aClass.getMethod("getStateSet", Int::class.javaPrimitiveType)
                    val getStateDrawable: Method =
                        aClass.getMethod("getStateDrawable", Int::class.javaPrimitiveType)
                    for (i in 0 until stateCount) {
                        val stateSet = getStateSet.invoke(listDrawable, i) as IntArray
                        if (stateSet.isNotEmpty()) {
                            mPressedThumbBitmap = if (stateSet[0] == android.R.attr.state_pressed) {
                                val stateDrawable =
                                    getStateDrawable.invoke(listDrawable, i) as Drawable
                                getDrawBitmap(stateDrawable, true)
                            } else {
                                //please check your selector drawable's format, please see above to correct.
                                throw IllegalArgumentException("the state of the selector thumb drawable is wrong!")
                            }
                        } else {
                            val stateDrawable = getStateDrawable.invoke(listDrawable, i) as Drawable
                            mThumbBitmap = getDrawBitmap(stateDrawable, true)
                        }
                    }
                } else {
                    //please check your selector drawable's format, please see above to correct.
                    throw IllegalArgumentException("the format of the selector thumb drawable is wrong!")
                }
            } catch (e: Exception) {
                mThumbBitmap = getDrawBitmap(mThumbDrawable, true)
                mPressedThumbBitmap = mThumbBitmap
            }
        } else {
            mThumbBitmap = getDrawBitmap(mThumbDrawable, true)
            mPressedThumbBitmap = mThumbBitmap
        }
    }

    private fun initTickMarksBitmap() {
        if (mTickMarksDrawable is StateListDrawable) {
            val listDrawable = mTickMarksDrawable as StateListDrawable
            try {
                val aClass: Class<out StateListDrawable> = listDrawable.javaClass
                val getStateCount: Method = aClass.getMethod("getStateCount")
                val stateCount = getStateCount.invoke(listDrawable) as Int
                if (stateCount == 2) {
                    val getStateSet: Method =
                        aClass.getMethod("getStateSet", Int::class.javaPrimitiveType)
                    val getStateDrawable: Method =
                        aClass.getMethod("getStateDrawable", Int::class.javaPrimitiveType)
                    for (i in 0 until stateCount) {
                        val stateSet = getStateSet.invoke(listDrawable, i) as IntArray
                        if (stateSet.isNotEmpty()) {
                            mSelectTickMarksBitmap = if (stateSet[0] == android.R.attr.state_selected) {
                                val stateDrawable =
                                    getStateDrawable.invoke(listDrawable, i) as Drawable
                                getDrawBitmap(stateDrawable, false)
                            } else {
                                //please check your selector drawable's format, please see above to correct.
                                throw IllegalArgumentException("the state of the selector TickMarks drawable is wrong!")
                            }
                        } else {
                            val stateDrawable = getStateDrawable.invoke(listDrawable, i) as Drawable
                            mUnselectTickMarksBitmap = getDrawBitmap(stateDrawable, false)
                        }
                    }
                } else {
                    //please check your selector drawable's format, please see above to correct.
                    throw IllegalArgumentException("the format of the selector TickMarks drawable is wrong!")
                }
            } catch (e: Exception) {
                mUnselectTickMarksBitmap = getDrawBitmap(mTickMarksDrawable, false)
                mSelectTickMarksBitmap = mUnselectTickMarksBitmap
            }
        } else {
            mUnselectTickMarksBitmap = getDrawBitmap(mTickMarksDrawable, false)
            mSelectTickMarksBitmap = mUnselectTickMarksBitmap
        }
    }

    override fun setEnabled(enabled: Boolean) {
        if (enabled == isEnabled) {
            return
        }
        super.setEnabled(enabled)
        if (isEnabled) {
            alpha = 1.0f
            if (mIndicatorStayAlways) {
                mIndicatorContentView?.alpha = 1.0f
            }
        } else {
            alpha = 0.3f
            if (mIndicatorStayAlways) {
                mIndicatorContentView?.alpha = 0.3f
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        post(Runnable { requestLayout() })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val parent: ViewParent = parent ?: return super.dispatchTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> parent.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(
                false
            )
        }
        return super.dispatchTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("gsb_instance_state", super.onSaveInstanceState())
        bundle.putFloat("gsb_progress", mProgress)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            setProgress(state.getFloat("gsb_progress"))
            super.onRestoreInstanceState(state.getParcelable("gsb_instance_state"))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!mUserSeekable || !isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                val mX = event.x
                if (isTouchSeekBar(mX, event.y)) {
                    if (mOnlyThumbDraggable && !isTouchThumb(mX)) {
                        return false
                    }
                    mIsTouching = true
                    mSeekChangeListener?.onStartTrackingTouch(this)
                    refreshSeekBar(event)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> refreshSeekBar(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mIsTouching = false
                mSeekChangeListener?.onStopTrackingTouch(this)
                if (!autoAdjustThumb()) {
                    invalidate()
                }
                mIndicator?.hide()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun refreshSeekBar(event: MotionEvent) {
        refreshThumbCenterXByProgress(calculateProgress(calculateTouchX(adjustTouchX(event))))
        setSeekListener(true)
        invalidate()
        updateIndicator()
    }

    private fun progressChange(): Boolean {
        return if (mIsFloatProgress) {
            lastProgress != mProgress
        } else {
            lastProgress.roundToInt() != mProgress.roundToInt()
        }
    }

    private fun adjustTouchX(event: MotionEvent): Float {
        val mTouchXCache: Float = if (event.x < mPaddingLeft) {
            mPaddingLeft.toFloat()
        } else if (event.x > mMeasuredWidth - mPaddingRight) {
            (mMeasuredWidth - mPaddingRight).toFloat()
        } else {
            event.x
        }
        return mTouchXCache
    }

    private fun calculateProgress(touchX: Float): Float {
        lastProgress = mProgress
        mProgress = mMin + getAmplitude() * (touchX - mPaddingLeft) / mSeekLength
        return mProgress
    }

    private fun calculateTouchX(touchX: Float): Float {
        var touchXTemp = touchX
        //make sure the seek bar to seek smoothly always
        // while the tick's count is less than 3(tick's count is 1 or 2.).
        if (mTicksCount > 2 && !mSeekSmoothly) {
            val touchBlockSize = ((touchX - mPaddingLeft) / mSeekBlockLength).roundToInt()
            touchXTemp = mSeekBlockLength * touchBlockSize + mPaddingLeft
        }
        return if (mR2L) {
            mSeekLength - touchXTemp + 2 * mPaddingLeft
        } else touchXTemp
    }

    private fun isTouchSeekBar(mX: Float, mY: Float): Boolean {
        if (mFaultTolerance == -1f) {
            mFaultTolerance = dpToPx(5f).toFloat()
        }
        val inWidthRange =
            mX >= mPaddingLeft - 2 * mFaultTolerance && mX <= mMeasuredWidth - mPaddingRight + 2 * mFaultTolerance
        val inHeightRange =
            mY >= mProgressTrack!!.top - mThumbTouchRadius - mFaultTolerance && mY <= mProgressTrack!!.top + mThumbTouchRadius + mFaultTolerance
        return inWidthRange && inHeightRange
    }

    private fun isTouchThumb(mX: Float): Boolean {
        refreshThumbCenterXByProgress(mProgress)
        val rawTouchX: Float = if (mR2L) {
            mBackgroundTrack!!.right
        } else {
            mProgressTrack!!.right
        }
        return rawTouchX - mThumbSize / 2f <= mX && mX <= rawTouchX + mThumbSize / 2f
    }

    private fun updateIndicator() {
        if (mIndicatorStayAlways) {
            updateStayIndicator()
        } else {
            if (mIndicator == null) {
                return
            }
            mIndicator?.initPop()
            if (mIndicator?.isShowing == true) {
                mIndicator?.update(getThumbCenterX())
            } else {
                mIndicator?.show(getThumbCenterX())
            }
        }
    }

    private fun initIndicatorContentView() {
        if (mShowIndicatorType == IndicatorType.NONE) {
            return
        }
        if (mIndicator == null) {
            mIndicator = Indicator(
                mContext,
                this,
                mIndicatorColor,
                mShowIndicatorType,
                mIndicatorTextSize,
                mIndicatorTextColor,
                mIndicatorContentView,
                mIndicatorTopContentView
            )
            mIndicatorContentView = mIndicator?.insideContentView
        }
    }

    private fun updateStayIndicator() {
        if (!mIndicatorStayAlways || mIndicator == null) {
            return
        }
        mIndicator?.setProgressTextView(getIndicatorTextString())
        mIndicatorContentView?.measure(0, 0)
        val measuredWidth: Int? = mIndicatorContentView?.measuredWidth
        val thumbCenterX = getThumbCenterX()
        if (mScreenWidth == -1f) {
            val metric = DisplayMetrics()
            val systemService = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            systemService.defaultDisplay.getMetrics(metric)
            mScreenWidth = metric.widthPixels.toFloat()
        }
        var indicatorOffset = 0
        var arrowOffset = 0
        measuredWidth?.let { mWidth ->
            if (mWidth / 2 + thumbCenterX > mMeasuredWidth) {
                indicatorOffset = mMeasuredWidth - mWidth
                arrowOffset = (thumbCenterX - indicatorOffset - mWidth / 2).toInt()
            } else if (thumbCenterX - mWidth / 2 < 0) {
                indicatorOffset = 0
                arrowOffset = -(mWidth / 2 - thumbCenterX).toInt()
            } else {
                indicatorOffset = (getThumbCenterX() - mWidth / 2).toInt()
                arrowOffset = 0
            }
        }
        mIndicator?.updateIndicatorLocation(indicatorOffset)
        mIndicator?.updateArrowViewLocation(arrowOffset)
    }

    private fun autoAdjustThumb(): Boolean {
        if (mTicksCount < 3 || !mSeekSmoothly) { //it is not necessary to adjust while count less than 2.
            return false
        }
        if (!mAdjustAuto) {
            return false
        }
        val closestIndex = getClosestIndex()
        val touchUpProgress = mProgress
        val animator =
            ValueAnimator.ofFloat(0f, Math.abs(touchUpProgress - mProgressArr[closestIndex]))
        animator.start()
        animator.addUpdateListener { animation ->
            lastProgress = mProgress
            mProgress = if (touchUpProgress - mProgressArr[closestIndex] > 0) {
                touchUpProgress - animation.animatedValue as Float
            } else {
                touchUpProgress + animation.animatedValue as Float
            }
            refreshThumbCenterXByProgress(mProgress)
            //the auto adjust was happened after user touched up, so from user is false.
            setSeekListener(false)
            if (mIndicator != null && mIndicatorStayAlways) {
                mIndicator?.refreshProgressText()
                updateStayIndicator()
            }
            invalidate()
        }
        return true
    }

    private fun getProgressString(progress: Float): String {
        return if (mIsFloatProgress) {
            fastFormat(progress.toDouble(), mScale)
        } else {
            progress.roundToInt().toString()
        }
    }

    private fun getClosestIndex(): Int {
        var closestIndex = 0
        var amplitude = abs(mMax - mMin)
        for (i in mProgressArr.indices) {
            val amplitudeTemp = abs(mProgressArr[i] - mProgress)
            if (amplitudeTemp <= amplitude) {
                amplitude = amplitudeTemp
                closestIndex = i
            }
        }
        return closestIndex
    }

    private fun getAmplitude(): Float {
        return if (mMax - mMin > 0) mMax - mMin else 1f
    }

    private fun setSeekListener(formUser: Boolean) {
        if (mSeekChangeListener == null) {
            return
        }
        if (progressChange()) {
            mSeekChangeListener?.onSeeking(collectParams(formUser))
        }
    }

    private fun collectParams(formUser: Boolean): SeekParams? {
        if (mSeekParams == null) {
            mSeekParams = SeekParams(this)
        }
        mSeekParams?.progress = getProgress()
        mSeekParams?.progressFloat = getProgressFloat()
        mSeekParams?.fromUser = formUser
        //for discrete series seek bar
        if (mTicksCount > 2) {
            val rawThumbPos = getThumbPosOnTick()
            if (mShowTickText && mTickTextsArr != null) {
                mSeekParams?.tickText = mTickTextsArr!![rawThumbPos]
            }
            if (mR2L) {
                mSeekParams?.thumbPosition = mTicksCount - rawThumbPos - 1
            } else {
                mSeekParams?.thumbPosition = rawThumbPos
            }
        }
        return mSeekParams
    }

    private fun apply(builder: Builder) {
        //seek bar
        mMax = builder.max
        mMin = builder.min
        mProgress = builder.progress
        mIsFloatProgress = builder.progressValueFloat
        mTicksCount = builder.tickCount
        mSeekSmoothly = builder.seekSmoothly
        mR2L = builder.r2l
        mUserSeekable = builder.userSeekable
        mClearPadding = builder.clearPadding
        mOnlyThumbDraggable = builder.onlyThumbDraggable
        //indicator
        mShowIndicatorType = builder.showIndicatorType
        mIndicatorColor = builder.indicatorColor
        mIndicatorTextColor = builder.indicatorTextColor
        mIndicatorTextSize = builder.indicatorTextSize
        mIndicatorContentView = builder.indicatorContentView
        mIndicatorTopContentView = builder.indicatorTopContentView
        //track
        mBackgroundTrackSize = builder.trackBackgroundSize
        mBackgroundTrackColor = builder.trackBackgroundColor
        mProgressTrackSize = builder.trackProgressSize
        mProgressTrackColor = builder.trackProgressColor
        mTrackRoundedCorners = builder.trackRoundedCorners
        //thumb
        mThumbSize = builder.thumbSize
        mThumbDrawable = builder.thumbDrawable
        mThumbTextColor = builder.thumbTextColor
        initThumbColor(builder.thumbColorStateList, builder.thumbColor)
        mShowThumbText = builder.showThumbText
        //tickMarks
        mShowTickMarksType = builder.showTickMarksType
        mTickMarksSize = builder.tickMarksSize
        mTickMarksDrawable = builder.tickMarksDrawable
        mTickMarksEndsHide = builder.tickMarksEndsHide
        mTickMarksSweptHide = builder.tickMarksSweptHide
        initTickMarksColor(builder.tickMarksColorStateList, builder.tickMarksColor)
        //tickTexts
        mShowTickText = builder.showTickText
        mTickTextsSize = builder.tickTextsSize
        mTickTextsCustomArray = builder.tickTextsCustomArray
        mTextsTypeface = builder.tickTextsTypeFace
        initTickTextsColor(builder.tickTextsColorStateList, builder.tickTextsColor)
    }

    fun showStayIndicator() {
        mIndicatorContentView?.visibility = INVISIBLE
        postDelayed(Runnable {
            val animation: Animation = AlphaAnimation(0.1f, 1.0f)
            animation.duration = 180
            mIndicatorContentView?.animation = animation
            updateStayIndicator()
            mIndicatorContentView?.visibility = VISIBLE
        }, 300)
    }

    fun setIndicatorStayAlways(indicatorStayAlways: Boolean) {
        mIndicatorStayAlways = indicatorStayAlways
    }

    fun getIndicatorContentView(): View? {
        return mIndicatorContentView
    }

    fun getIndicatorTextString(): String {
        if (mIndicatorTextFormat != null && mIndicatorTextFormat?.contains(FORMAT_TICK_TEXT) == true) {
            mTickTextsArr?.let {
                if (mTicksCount > 2) {
                    return mIndicatorTextFormat?.replace(
                        FORMAT_TICK_TEXT,
                        it[getThumbPosOnTick()]
                    ).orEmpty()
                }
            }
        } else if (mIndicatorTextFormat != null && mIndicatorTextFormat?.contains(FORMAT_PROGRESS) == true) {
            return mIndicatorTextFormat?.replace(FORMAT_PROGRESS, getProgressString(mProgress)).orEmpty()
        } else if(mIndicatorTextFormat != null && mIndicatorTextFormat?.contains(FORMAT_CUSTOM_TEXT) == true) {
            return mIndicatorTextFormat?.replace(FORMAT_CUSTOM_TEXT, "").orEmpty()
        }
        return getProgressString(mProgress)
    }

    fun getIndicator(): Indicator? {
        return mIndicator
    }

    fun getTickCount(): Int {
        return mTicksCount
    }

    @Synchronized fun getProgressFloat(): Float {
        val bigDecimal: BigDecimal = BigDecimal.valueOf(mProgress.toLong())
        return bigDecimal.setScale(mScale, BigDecimal.ROUND_HALF_UP).toFloat()
    }

    fun getProgress(): Int {
        return mProgress.roundToInt()
    }

    fun getMax(): Float {
        return mMax
    }

    fun getMin(): Float {
        return mMin
    }

    fun getOnSeekChangeListener(): OnSeekChangeListener? {
        return mSeekChangeListener
    }

    @Synchronized fun setProgress(progress: Float) {
        lastProgress = mProgress
        mProgress = if (progress < mMin) mMin else if (progress > mMax) mMax else progress
        //adjust to the closest tick's progress
        if (!mSeekSmoothly && mTicksCount > 2) {
            mProgress = mProgressArr[getClosestIndex()]
        }
        setSeekListener(false)
        refreshThumbCenterXByProgress(mProgress)
        postInvalidate()
        updateStayIndicator()
    }

    @Synchronized fun setMax(max: Float) {
        mMax = mMin.coerceAtLeast(max)
        initProgressRangeValue()
        collectTicksInfo()
        refreshSeekBarLocation()
        invalidate()
        updateStayIndicator()
    }

    @Synchronized fun setMin(min: Float) {
        mMin = mMax.coerceAtMost(min)
        initProgressRangeValue()
        collectTicksInfo()
        refreshSeekBarLocation()
        invalidate()
        updateStayIndicator()
    }

    fun setR2L(isR2L: Boolean) {
        mR2L = isR2L
        requestLayout()
        invalidate()
        updateStayIndicator()
    }

    fun setThumbDrawable(drawable: Drawable?) {
        if (drawable == null) {
            mThumbDrawable = null
            mThumbBitmap = null
            mPressedThumbBitmap = null
        } else {
            mThumbDrawable = drawable
//            mThumbRadius = dpToPx(THUMB_MAX_WIDTH.toFloat()).coerceAtMost(mThumbSize) / 2.0f
//            mThumbTouchRadius = mThumbRadius
//            mCustomDrawableMaxHeight = mTickMarksSize.toFloat()
            initThumbBitmap()
        }
        requestLayout()
        invalidate()
    }

    fun setThumbSize(thumbSize: Float) {
        mThumbSize = thumbSize.roundToInt()
        mCustomDrawableMaxHeight = dpToPx(thumbSize).toFloat()
        initThumbBitmap()
        requestLayout()
        invalidate()
    }

    fun hideThumb(hide: Boolean) {
        mHideThumb = hide
        invalidate()
    }

    fun hideThumbText(hide: Boolean) {
        mShowThumbText = !hide
        invalidate()
    }

    fun thumbColor(@ColorInt thumbColor: Int) {
        mThumbColor = thumbColor
        mPressedThumbColor = thumbColor
        invalidate()
    }

    fun thumbColorStateList(thumbColorStateList: ColorStateList) {
        initThumbColor(thumbColorStateList, mThumbColor)
        invalidate()
    }

    fun setTickMarksDrawable(drawable: Drawable?) {
        if (drawable == null) {
            mTickMarksDrawable = null
            mUnselectTickMarksBitmap = null
            mSelectTickMarksBitmap = null
        } else {
            mTickMarksDrawable = drawable
            mTickRadius =
                dpToPx(THUMB_MAX_WIDTH.toFloat()).coerceAtMost(mTickMarksSize) / 2.0f
            mCustomDrawableMaxHeight = mThumbTouchRadius.coerceAtLeast(mTickRadius) * 2.0f
            initTickMarksBitmap()
        }
        invalidate()
    }

    fun tickMarksColor(@ColorInt tickMarksColor: Int) {
        mSelectedTickMarksColor = tickMarksColor
        mUnSelectedTickMarksColor = tickMarksColor
        invalidate()
    }

    fun tickMarksColor(tickMarksColorStateList: ColorStateList) {
        initTickMarksColor(tickMarksColorStateList, mSelectedTickMarksColor)
        invalidate()
    }

    fun tickTextsColor(@ColorInt tickTextsColor: Int) {
        mUnselectedTextsColor = tickTextsColor
        mSelectedTextsColor = tickTextsColor
        mHoveredTextColor = tickTextsColor
        invalidate()
    }

    fun tickTextsColorStateList(tickTextsColorStateList: ColorStateList) {
        initTickTextsColor(tickTextsColorStateList, mSelectedTextsColor)
        invalidate()
    }

    fun setDecimalScale(scale: Int) {
        mScale = scale
    }

    fun setIndicatorTextFormat(format: String?) {
        mIndicatorTextFormat = format
        initTextsArray()
        updateStayIndicator()
    }

    fun customSectionTrackColor(collector: ColorCollector) {
        val colorArray = IntArray(if (mTicksCount - 1 > 0) mTicksCount - 1 else 1)
        for (i in colorArray.indices) {
            //set the default section color
            colorArray[i] = mBackgroundTrackColor
        }
        mCustomTrackSectionColorResult = collector.collectSectionTrackColor(colorArray)
        mSectionTrackColorArray = colorArray
        invalidate()
    }

    fun customTickTexts(tickTextsArr: Array<String>) {
        mTickTextsCustomArray = tickTextsArr
        mTickTextsArr?.let {
            for (i in it.indices) {
                val tickText: String = if (i < tickTextsArr.size) {
                    tickTextsArr[i]
                } else {
                    ""
                }
                var index = i
                if (mR2L) {
                    index = mTicksCount - 1 - i
                }
                it[index] = tickText
                mRect?.let { rect ->
                    mTextPaint?.getTextBounds(tickText, 0, tickText.length, rect)
                    mTickTextsWidth[index] = rect.width().toFloat()
                }
            }
            invalidate()
        }
    }

    fun customTickTextsTypeface(typeface: Typeface) {
        mTextsTypeface = typeface
        measureTickTextsBonds()
        requestLayout()
        invalidate()
    }

    fun setOnSeekChangeListener(listener: OnSeekChangeListener) {
        mSeekChangeListener = listener
    }

    fun showBothEndsTickTextsOnly(onlyShow: Boolean) {
        mShowBothTickTextsOnly = onlyShow
    }

    fun setUserSeekAble(seekAble: Boolean) {
        mUserSeekable = seekAble
    }

    @Synchronized fun setTickCount(tickCount: Int) {
        require(!(mTicksCount < 0 || mTicksCount > 50)) { "the Argument: TICK COUNT must be limited between (0-50), Now is $mTicksCount" }
        mTicksCount = tickCount
        collectTicksInfo()
        initTextsArray()
        initSeekBarInfo()
        refreshSeekBarLocation()
        invalidate()
        updateStayIndicator()
    }

    fun setThumbAdjustAuto(adjustAuto: Boolean) {
        mAdjustAuto = adjustAuto
    }

    fun setBackgroundIndicator(@DrawableRes customBackground: Int, customArrowColor: Int){
        mIndicator?.setBackgroundIndicator(customBackground, customArrowColor)
    }

    fun setIndicatorTextColor(@ColorRes textColor: Int){
        mIndicator?.setIndicatorTextColor(textColor)
    }
    /*------------------API END-------------------*/

    companion object {

        private const val THUMB_MAX_WIDTH = 30
        private const val FORMAT_PROGRESS = "\${PROGRESS}"
        private const val FORMAT_TICK_TEXT = "\${TICK_TEXT}"
        private const val FORMAT_CUSTOM_TEXT = "\${CUSTOM_TEXT}"
        /*------------------API START-------------------*/

        fun with(context: Context): Builder {
            return Builder(context)
        }
    }
}