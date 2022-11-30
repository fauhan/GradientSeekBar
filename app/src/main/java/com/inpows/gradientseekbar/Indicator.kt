package com.inpows.gradientseekbar

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

class Indicator(
    context: Context,
    seekBar: GradientSeekBar,
    indicatorColor: Int,
    indicatorType: Int,
    indicatorTextSize: Int,
    indicatorTextColor: Int,
    indicatorCustomView: View?,
    indicatorCustomTopContentView: View?
) {

    private val mWindowWidth: Int
    private val mLocation = IntArray(2)
    private var mArrowView: ArrowView? = null
    private var mProgressTextView: TextView? = null
    private var mIndicatorPopW: PopupWindow? = null
    private var mTopContentView: LinearLayout? = null
    private val mGap: Int
    private val mIndicatorColor: Int
    private val mContext: Context
    private var mIndicatorType: Int
    private val mSeekBar: GradientSeekBar
    private var mIndicatorView: View? = null
    private var mIndicatorCustomView: View?
    private val mIndicatorCustomTopContentView: View?
    private val mIndicatorTextSize: Float
    private var mIndicatorTextColor: Int

    init {
        mContext = context
        mSeekBar = seekBar
        mIndicatorColor = indicatorColor
        mIndicatorType = indicatorType
        mIndicatorCustomView = indicatorCustomView
        mIndicatorCustomTopContentView = indicatorCustomTopContentView
        mIndicatorTextSize = indicatorTextSize.toFloat()
        mIndicatorTextColor = indicatorTextColor
        mWindowWidth = windowWidth
        mGap = dpToPx(2f)
        initIndicator()
    }

    private fun initIndicator() {
        if (mIndicatorType == IndicatorType.CUSTOM) {
            if (mIndicatorCustomView != null) {
                mIndicatorView = mIndicatorCustomView
                // for the custom indicator view, if progress need to show when seeking ,
                // need a TextView to show progress and this textView 's identify must be progress;
                val progressTextViewId: Int = mContext.resources.getIdentifier(
                    "gsb_progress",
                    "id",
                    mContext.applicationContext.packageName
                )
                if (progressTextViewId > 0) {
                    val view = mIndicatorView?.findViewById<View>(progressTextViewId)
                    view?.run {
                        if (this is TextView) {
                            //progressText
                            mProgressTextView = this
                            mProgressTextView?.text = mSeekBar.getIndicatorTextString()
                            mProgressTextView?.textSize = pxTosp(mIndicatorTextSize)
                            mProgressTextView?.setTextColor(mIndicatorTextColor)
                        } else {
                            throw ClassCastException("the view identified by gsb_progress in indicator custom layout can not be cast to TextView")
                        }
                    }
                }
            } else {
                throw IllegalArgumentException("the attr：indicator_custom_layout must be set while you set the indicator type to CUSTOM.")
            }
        } else {
            if (mIndicatorType == IndicatorType.CIRCULAR_BUBBLE) {
                mIndicatorView = CircleBubbleView(
                    mContext,
                    mIndicatorTextSize,
                    mIndicatorTextColor,
                    mIndicatorColor,
                    "1000"
                )
                (mIndicatorView as CircleBubbleView).setProgress(mSeekBar.getIndicatorTextString())
            } else {
                mIndicatorView = View.inflate(mContext, R.layout.gsb_indicator, null)
                //container
                mTopContentView = mIndicatorView?.findViewById(R.id.indicator_container)
                //arrow
                mArrowView = mIndicatorView?.findViewById(R.id.indicator_arrow)
                mArrowView?.setColor(mIndicatorColor)
                //progressText
                mProgressTextView = mIndicatorView?.findViewById(R.id.gsb_progress)
                mProgressTextView?.text = mSeekBar.getIndicatorTextString()
                mProgressTextView?.textSize = pxTosp(mIndicatorTextSize)
                mProgressTextView?.setTextColor(mIndicatorTextColor)
                mTopContentView?.background = gradientDrawable
                //custom top content view
                if (mIndicatorCustomTopContentView != null) {
                    //for the custom indicator top content view, if progress need to show when seeking ,
                    //need a TextView to show progress and this textView 's identify must be progress;
                    val progressTextViewId: Int = mContext.resources.getIdentifier(
                        "gsb_progress",
                        "id",
                        mContext.applicationContext.packageName
                    )
                    var topContentView: View = mIndicatorCustomTopContentView
                    if (progressTextViewId > 0) {
                        val tv: View = topContentView.findViewById(progressTextViewId)
                        if (tv is TextView) {
                            setTopContentView(topContentView, tv)
                        } else {
                            topContentView = topContentView
                        }
                    } else {
                        topContentView = topContentView
                    }
                }
            }
        }
    }

    private val gradientDrawable: Drawable?
        get() {
            val tvDrawable: Drawable? = when (mIndicatorType) {
                IndicatorType.ROUNDED_RECTANGLE -> {
                    ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_rectangle_seekbar_indicator)
                }
                IndicatorType.RECTANGLE -> {
                    ContextCompat.getDrawable(mContext, R.drawable.square_skeleton)
                }
                else -> {
                    ContextCompat.getDrawable(mContext, R.drawable.square_skeleton)
                }
            }
            return tvDrawable
        }

    private val windowWidth: Int
        get() {
            val wm = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            return wm.defaultDisplay?.width ?: 0
        }

    private val indicatorScreenX: Int
        get() {
            mSeekBar.getLocationOnScreen(mLocation)
            return mLocation[0]
        }

    private fun adjustArrow(touchX: Float) {
        if (mIndicatorType == IndicatorType.CUSTOM || mIndicatorType == IndicatorType.CIRCULAR_BUBBLE) {
            return
        }
        val indicatorScreenX = indicatorScreenX
        mIndicatorPopW?.run {
            if (indicatorScreenX + touchX < contentView.measuredWidth / 2
            ) {
                setMargin(
                    mArrowView, -(contentView.measuredWidth / 2 - indicatorScreenX - touchX).roundToInt(),
                    -1,
                    -1,
                    -1
                )
            } else if (mWindowWidth - indicatorScreenX - touchX < contentView.measuredWidth / 2) {
                setMargin(
                    mArrowView,
                    (contentView.measuredWidth / 2 - (mWindowWidth - indicatorScreenX - touchX)).roundToInt(),
                    -1,
                    -1,
                    -1
                )
            } else {
                setMargin(mArrowView, 0, 0, 0, 0)
            }
        }
    }

    private fun setMargin(view: View?, left: Int, top: Int, right: Int, bottom: Int) {
        if (view == null) {
            return
        }
        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(
                if (left == -1) layoutParams.leftMargin else left,
                if (top == -1) layoutParams.topMargin else top,
                if (right == -1) layoutParams.rightMargin else right,
                if (bottom == -1) layoutParams.bottomMargin else bottom
            )
            view.requestLayout()
        }
    }

    fun initPop() {
        if (mIndicatorPopW != null) {
            return
        }
        if (mIndicatorType != IndicatorType.NONE && mIndicatorView != null) {
            mIndicatorView?.measure(0, 0)
            mIndicatorPopW = PopupWindow(
                mIndicatorView,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                false
            )
        }
    }

    val insideContentView: View?
        get() = mIndicatorView

    fun setProgressTextView(text: String?) {
        if (mIndicatorView is CircleBubbleView) {
            (mIndicatorView as CircleBubbleView?)!!.setProgress(text)
        } else if (mProgressTextView != null) {
            mProgressTextView?.text = text
        }
    }

    fun updateIndicatorLocation(offset: Int) {
        setMargin(mIndicatorView, offset, -1, -1, -1)
    }

    fun updateArrowViewLocation(offset: Int) {
        setMargin(mArrowView, offset, -1, -1, -1)
    }

    fun update(touchX: Float) {
        if (!mSeekBar.isEnabled || mSeekBar.visibility != View.VISIBLE) {
            return
        }
        refreshProgressText()
        mIndicatorPopW?.run {
            contentView.measure(0, 0)
            update(
                mSeekBar,
                (touchX - contentView.measuredWidth / 2).roundToInt(),
                - (mSeekBar.measuredHeight + contentView
                    .measuredHeight - mSeekBar.paddingTop /*- mSeekBar.getTextHeight() */ + mGap),
                -1,
                -1
            )
            adjustArrow(touchX)

        }
    }

    fun show(touchX: Float) {
        if (!mSeekBar.isEnabled || mSeekBar.visibility != View.VISIBLE) {
            return
        }
        refreshProgressText()
        mIndicatorPopW?.run {
            contentView.measure(0, 0)
            showAsDropDown(
                mSeekBar,
                (touchX - contentView.measuredWidth / 2f).roundToInt(),
                - (mSeekBar.measuredHeight + contentView
                    .measuredHeight - mSeekBar.paddingTop /*- mSeekBar.getTextHeight()*/ + mGap)
            )
            adjustArrow(touchX)
        }
    }

    fun refreshProgressText() {
        val tickTextString: String = mSeekBar.getIndicatorTextString()
        if (mIndicatorView is CircleBubbleView) {
            (mIndicatorView as CircleBubbleView?)?.setProgress(tickTextString)
        } else if (mProgressTextView != null) {
            mProgressTextView?.text = tickTextString
        }
    }

    fun hide() {
        mIndicatorPopW?.dismiss()
    }

    val isShowing: Boolean
        get() = mIndicatorPopW != null && mIndicatorPopW?.isShowing == true

    /*----------------------API START-------------------*/
    var contentView: View?
        get() = mIndicatorView
        set(customIndicatorView) {
            mIndicatorType = IndicatorType.CUSTOM
            mIndicatorCustomView = customIndicatorView
            initIndicator()
        }

    fun setContentView(customIndicatorView: View?, progressTextView: TextView?) {
        mProgressTextView = progressTextView
        mIndicatorType = IndicatorType.CUSTOM
        mIndicatorCustomView = customIndicatorView
        initIndicator()
    }

    var topContentView: View?
        get() = mTopContentView
        set(topContentView) {
            if (topContentView != null) {
                setTopContentView(topContentView, null)
            }
        }

    fun setTopContentView(
        topContentView: View,
        progressTextView: TextView?
    ) {
        mProgressTextView = progressTextView
        mTopContentView?.removeAllViews()
        topContentView.background = gradientDrawable
        mTopContentView?.addView(topContentView)
    }

    fun setBackgroundIndicator(@DrawableRes customBackground: Int, customArrowColor: Int){
        mTopContentView?.background = ContextCompat.getDrawable(mContext, customBackground)
        mArrowView?.setColor(ContextCompat.getColor(mContext, customArrowColor))
    }

    fun setIndicatorTextColor(@ColorRes textColor: Int){
        mIndicatorTextColor = textColor
        mProgressTextView?.setTextColor(ContextCompat.getColor(mContext, textColor))
    }
    /*----------------------API END-------------------*/
}