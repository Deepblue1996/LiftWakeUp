package com.prohua.liftwakeup.weight

import android.content.Context
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.prohua.liftwakeup.R
import com.prohua.liftwakeup.util.StatusBarUtil

/**
 * 自定义标题栏
 *
 * @author Deep
 * @date 2017/11/10 0010
 */

class TitleBar : LinearLayout {

    private var leftLinear: LinearLayout? = null
    private var rightLinear: LinearLayout? = null
    private var centerLinear: LinearLayout? = null
    private var viewLoading: ImageView? = null
    private var leftImageView: ImageView? = null
    private var rightImageView: ImageView? = null
    private var rightImageView2: ImageView? = null
    private var leftTextView: TextView? = null
    private var rightTextView: TextView? = null
    private var titleTextView: TextView? = null
    private var statusBottomLineShow: View? = null

    private var mRefreshAnim: Animation? = null

    private var mContext: Context? = null

    private var openLoadingNum: Int = 0

    constructor(context: Context) : super(context) {
        mContext = context
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        init()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        mContext = context
        init()
    }

    private fun init() {
        val mInflater = LayoutInflater.from(mContext)
        val childView = mInflater.inflate(R.layout.title_bar_layout, null)

        val linearParams = childView
                .findViewById<View>(R.id.status)
                .layoutParams as LinearLayout.LayoutParams
        //取控件textView当前的布局参数
        linearParams.height = StatusBarUtil.getStatusBarHeight(context)
        childView.findViewById<View>(R.id.status).layoutParams = linearParams

        leftLinear = childView.findViewById(R.id.left_show)
        rightLinear = childView.findViewById(R.id.right_show)
        centerLinear = childView.findViewById(R.id.centerLinear)
        viewLoading = childView.findViewById(R.id.viewLoading)
        leftImageView = childView.findViewById(R.id.left_icon)
        leftTextView = childView.findViewById(R.id.left_text)
        rightImageView = childView.findViewById(R.id.right_icon)
        rightImageView2 = childView.findViewById(R.id.right_icon_2)
        rightTextView = childView.findViewById(R.id.right_text)
        statusBottomLineShow = childView.findViewById(R.id.status_bottom_line_show)

        titleTextView = childView.findViewById(R.id.title_text)

        mRefreshAnim = AnimationUtils.loadAnimation(context, R.anim.diy_probar)

        addView(childView)

        openLoadingNum = 0
    }

    @Synchronized
    fun stopLoading() {

        openLoadingNum--

        if (openLoadingNum == 0) {
            viewLoading!!.visibility = View.GONE
            mRefreshAnim!!.reset()
            viewLoading!!.clearAnimation()
        }

    }

    @Synchronized
    fun startLoading() {

        centerLinear!!.visibility = View.VISIBLE
        viewLoading!!.visibility = View.VISIBLE

        openLoadingNum++

        if (openLoadingNum == 1) {
            mRefreshAnim!!.reset()
            viewLoading!!.clearAnimation()
            viewLoading!!.startAnimation(mRefreshAnim)
        }
    }

    fun visibleLoading() {
        viewLoading!!.visibility = View.VISIBLE
    }

    fun setTitle(title: String) {
        centerLinear!!.visibility = View.VISIBLE
        titleTextView!!.visibility = View.VISIBLE
        titleTextView!!.text = title
    }

    fun setBack(@DrawableRes id: Int, backListener: BackListener) {
        leftLinear!!.visibility = View.VISIBLE
        leftImageView!!.visibility = View.VISIBLE
        leftImageView!!.setImageResource(id)
        leftLinear!!.setOnClickListener { backListener.back() }
    }

    fun setBack(text: String, backListener: BackListener) {
        leftLinear!!.visibility = View.VISIBLE
        leftTextView!!.visibility = View.VISIBLE
        leftTextView!!.text = text
        leftLinear!!.setOnClickListener { backListener.back() }
    }

    fun setBack(@DrawableRes id: Int, text: String, backListener: BackListener) {
        leftLinear!!.visibility = View.VISIBLE
        leftImageView!!.visibility = View.VISIBLE
        leftTextView!!.visibility = View.VISIBLE
        leftImageView!!.setImageResource(id)
        leftTextView!!.text = text
        leftLinear!!.setOnClickListener { backListener.back() }
    }

    fun setMore(@DrawableRes id: Int, moreListener: MoreListener) {
        rightLinear!!.visibility = View.VISIBLE
        rightImageView!!.visibility = View.VISIBLE
        rightImageView!!.setImageResource(id)
        rightLinear!!.setOnClickListener { moreListener.more() }
    }

    fun setMore(text: String, moreListener: MoreListener) {
        rightLinear!!.visibility = View.VISIBLE
        rightTextView!!.visibility = View.VISIBLE
        rightTextView!!.text = text
        rightLinear!!.setOnClickListener { moreListener.more() }
    }

    fun setMore(@DrawableRes id: Int, text: String, moreListener: MoreListener) {
        rightLinear!!.visibility = View.VISIBLE
        rightImageView!!.visibility = View.VISIBLE
        rightTextView!!.visibility = View.VISIBLE
        rightImageView!!.setImageResource(id)
        rightTextView!!.text = text
        rightLinear!!.setOnClickListener { moreListener.more() }
    }

    fun setMore(@DrawableRes id: Int, @DrawableRes id2: Int, moreListener: MoreListener,
                moreListener2: MoreListener) {
        rightLinear!!.visibility = View.VISIBLE
        rightImageView!!.visibility = View.VISIBLE
        rightImageView2!!.visibility = View.VISIBLE
        rightTextView!!.visibility = View.VISIBLE
        rightTextView!!.visibility = View.VISIBLE
        rightImageView!!.setImageResource(id)
        rightImageView2!!.setImageResource(id2)

        rightImageView!!.setOnClickListener { moreListener.more() }
        rightImageView2!!.setOnClickListener { moreListener2.more() }
    }

    fun hideLine() {
        statusBottomLineShow!!.visibility = View.GONE
    }

    interface BackListener {
        /**
         * 返回
         */
        fun back()
    }

    interface MoreListener {
        /**
         * 更多
         */
        fun more()
    }
}
