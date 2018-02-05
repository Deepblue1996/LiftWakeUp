package com.prohua.liftwakeup.service

import android.annotation.SuppressLint
import android.app.*
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.prohua.liftwakeup.CoreActivity
import com.prohua.liftwakeup.R


/**
 * 保活服务
 * Created by Deep on 2017/11/29 0029.
 */
class HardwareMonitoringJobService : JobService() {
    
    private val pid = android.os.Process.myPid()
    private var kJobId = 0

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("LongLogTag")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("Lift_HardwareMonitoringJobService", "jobService启动")

        scheduleJob(getJobInfo())

        val notification = NotificationCompat.Builder(this)
                /**设置通知左边的大图标 */
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
                /**设置通知右边的小图标 */
                .setSmallIcon(R.mipmap.ic_launcher)
                /**通知首次出现在通知栏，带上升动画效果的 */
                .setTicker("hello ヾ(•ω•`)o")
                /**设置通知的标题 */
                .setContentTitle(getString(R.string.app_name))
                /**设置通知的内容 */
                .setContentText(getString(R.string.app_name) + "正在后台运行")
                /**通知产生的时间，会在通知信息里显示 */
                .setWhen(System.currentTimeMillis())
                /**设置该通知优先级 */
                .setPriority(Notification.FLAG_ONGOING_EVENT)
                /**设置这个标志当用户单击面板就可以让通知将自动取消 */
                .setAutoCancel(true)
                /**设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接) */
                .setOngoing(true)
                /**向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合： */
                //.setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(PendingIntent.getActivity(this, 1, Intent(this, CoreActivity::class.java), PendingIntent.FLAG_CANCEL_CURRENT))
                .build()

        notification.flags = Notification.FLAG_ONGOING_EVENT

        //val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ///**发起通知**/
        //notificationManager.notify(pid, notification)

        startForeground(pid, notification)

        stopForeground(true)

        return Service.START_REDELIVER_INTENT
    }

    @SuppressLint("LongLogTag")
    override fun onStartJob(params: JobParameters): Boolean {
        Log.i("Lift_HardwareMonitoringJobService", "执行了onStartJob方法")
        val isLocalServiceWork = isServiceWork(this, "com.prohua.liftwakeup.service.HardwareMonitoringLocalService")
        val isRemoteServiceWork = isServiceWork(this, "com.prohua.liftwakeup.service.HardwareMonitoringRemoteService")
        if (!isLocalServiceWork || !isRemoteServiceWork) {
            this.startService(Intent(this, HardwareMonitoringLocalService::class.java))
            this.startService(Intent(this, HardwareMonitoringRemoteService::class.java))

            Log.i("Lift_onStartJob", "保活服务正在启动 HardwareMonitoringLocalService HardwareMonitoringRemoteService")
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("LongLogTag")
    override fun onStopJob(params: JobParameters): Boolean {
        Log.i("Lift_HardwareMonitoringJobService", "执行了onStopJob方法")
        scheduleJob(getJobInfo())
        return true
    }

    @SuppressLint("LongLogTag")
    override fun onDestroy() {
        Log.d("Lift_HardwareMonitoringJobService", "保活服务被杀死,请求广播重新启动")
        val i = Intent("com.prohua.liftwakeup.destroy")
        sendBroadcast(i)
        super.onDestroy()
    }

    //将任务作业发送到作业调度中去
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("LongLogTag")
    fun scheduleJob(t: JobInfo) {
        Log.i("Lift_HardwareMonitoringJobService", "调度job")
        val tm = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        tm.schedule(t)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getJobInfo(): JobInfo {
        val builder = JobInfo.Builder(kJobId++, ComponentName(this, HardwareMonitoringJobService::class.java))
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
        builder.setPersisted(true)
        builder.setRequiresCharging(false)
        builder.setRequiresDeviceIdle(false)
        //间隔1000毫秒
        builder.setPeriodic(1000)
        return builder.build()
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