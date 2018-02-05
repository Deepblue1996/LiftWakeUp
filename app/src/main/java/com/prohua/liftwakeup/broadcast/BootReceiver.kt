package com.prohua.liftwakeup.broadcast

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.prohua.liftwakeup.service.HardwareMonitoringJobService

/**
 * 接收指定广播启动保活服务
 * Created by Deep on 2017/11/30 0030.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Lift_BootReceiver", "接收到复活广播指令")
        if (intent.action == "com.prohua.liftwakeup.destroy") {
            // 判断是否存活
            if (!isServiceWork(context, "com.prohua.liftwakeup.service.HardwareMonitoringJobService")) {
                Log.i("Lift_BootReceiver", "启动保活服务")
                context.startService(Intent(context, HardwareMonitoringJobService::class.java))
            }
        }
    }

    // 判断服务是否正在运行
    private fun isServiceWork(mContext: Context, serviceName: String): Boolean {
        val myAM = mContext
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val myList = myAM.getRunningServices(100)
        if (myList.size <= 0) {
            return false
        }
        return myList.indices
                .map { myList[it].service.className.toString() }
                .contains(serviceName)
    }
}