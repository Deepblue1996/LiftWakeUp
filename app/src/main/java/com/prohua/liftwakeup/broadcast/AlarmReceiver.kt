package com.prohua.liftwakeup.broadcast

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import com.prohua.liftwakeup.service.HardwareMonitoringJobService

/**
 * 定时
 * Created by Deep on 2017/11/30 0030.
 */
class AlarmReceiver : BroadcastReceiver() {

    @SuppressLint("LongLogTag")
    override fun onReceive(context: Context, intent: Intent) {
        // TODO Auto-generated method stub
        if (intent.action == "com.prohua.liftwakeup.destroy_timer") {
            // 判断是否存活
            if (!isServiceWork(context, "com.prohua.liftwakeup.service.HardwareMonitoringJobService")) {
                Log.i("Lift_AlarmReceiver_BootReceiver", "启动保活服务")
                context.startService(Intent(context, HardwareMonitoringJobService::class.java))
            }
            Log.i("Lift_AlarmReceiver_BootReceiver", "执行了")
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