package com.prohua.liftwakeup.service

import android.annotation.TargetApi
import android.app.Notification
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.prohua.liftwakeup.ProcessService
import com.prohua.liftwakeup.R

/**
 * 辅助唤醒服务
 * Created by Deep on 2017/11/29 0029.
 */
class HardwareMonitoringRemoteService : Service() {

    private var binder: MyBinder? = null
    private var conn: MyConn? = null

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        return binder
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate() {
        super.onCreate()
        binder = MyBinder()
        if (conn == null)
            conn = MyConn()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val builder = Notification.Builder(this)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        startForeground(250, builder.build())
        this@HardwareMonitoringRemoteService.bindService(Intent(this, HardwareMonitoringLocalService::class.java), conn, Context.BIND_IMPORTANT)

        return START_REDELIVER_INTENT
    }

    internal inner class MyBinder : ProcessService.Stub() {

        @Throws(RemoteException::class)
        override fun getServiceName(): String {
            return "I am HardwareMonitoringRemoteService"
        }
    }

    internal inner class MyConn : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.i("Lift_Info", "与HardwareMonitoringLocalService连接成功")
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            // 启动HardwareMonitoringLocalService
            this@HardwareMonitoringRemoteService.startService(Intent(this@HardwareMonitoringRemoteService, HardwareMonitoringLocalService::class.java))
            //绑定HardwareMonitoringLocalService
            this@HardwareMonitoringRemoteService.bindService(Intent(this@HardwareMonitoringRemoteService, HardwareMonitoringLocalService::class.java), conn, Context.BIND_IMPORTANT)
        }
    }
}