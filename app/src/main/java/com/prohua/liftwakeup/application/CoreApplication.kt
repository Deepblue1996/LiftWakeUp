package com.prohua.liftwakeup.application

import android.app.Application

/**
 * 主类(入口)
 * Created by Deep on 2017/11/29 0029.
 */

class CoreApplication : Application() {

    /**
     * 伴生对象, 延迟初始化, 外部不可赋值
     */
    companion object {
        lateinit var instance: CoreApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}