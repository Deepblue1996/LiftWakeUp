package com.prohua.liftwakeup.util

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

/**
 * 状态栏
 * @author Deep
 * @date 2017/11/10 0010
 */

object StatusBarUtil {

    /** 获取状态栏高度  */
    fun getStatusBarHeight(context: Context): Int {
        var result = 24
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resId > 0) {
            result = context.resources.getDimensionPixelSize(resId)
        } else {
            result = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    result.toFloat(), Resources.getSystem().displayMetrics).toInt()
        }
        return result
    }
}
