package com.prohua.liftwakeup.service

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.Notification
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.prohua.liftwakeup.ProcessService
import com.prohua.liftwakeup.R
import android.content.ContentValues.TAG
import android.os.*
import com.prohua.liftwakeup.broadcast.AdminManageReceiver
import android.content.BroadcastReceiver
import android.content.IntentFilter


/**
 * 主后台逻辑服务
 * Created by Deep on 2017/11/29 0029.
 */
class HardwareMonitoringLocalService : Service(), SensorEventListener {

    private lateinit var mAdminName: ComponentName

    private lateinit var mDPM: DevicePolicyManager
    private lateinit var pm: PowerManager

    private lateinit var gyroSensor: Sensor
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorManager2: SensorManager

    private var binder: MyBinder? = null
    private var conn: MyConn? = null

    private var proximityX: Float = 0.0f

    // 判断谁关闭了屏幕
    private var appLock: Int = APP_PRESENT
    // 防抖动
    private var appInCome: Boolean = false

    private var screenState: Boolean = true

    companion object {
        // 程序处理
        private val APP_PRESENT: Int = 0
        // 用户处理
        private val USER_PRESENT: Int = 1

        // 抬起
        private val GESTURE_LIFT: Int = 0
        // 放下
        private val GESTURE_PUT: Int = 1
    }

    // 手势
    private var gesture: Int = GESTURE_LIFT

    // 2s存储的数据
    private var gestureTwoList: ArrayList<String> = ArrayList()

    /**
     * Start Service have two method
     *
     * 1) Call to startService()
     *
     * 1.onCreate()
     * 2.onStartCommand()
     * -- Active Lift time
     * Service running -- The service is stopped by itself or a client
     * 3.onDestroy()
     * -- Service shut down
     * -- Unbounded service
     *
     * 2) Call to bindService()
     *
     * 1.onCreate()
     * 2.onBind()
     * -- Active Lift time
     * Service running -- All clients unbind by calling unbindService()
     * 3.onUnbind()
     * 4.onDestroy()
     * -- Service shut down
     * -- Bounded service
     */

    /**
     * Now Service bind in this
     */
    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        return binder
    }

    /**
     * Service Create
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate() {
        super.onCreate()
        binder = MyBinder()
        if (conn == null)
            conn = MyConn()

        /* 注册屏幕唤醒时的广播 */
        val mScreenOnFilter = IntentFilter("android.intent.action.SCREEN_ON")
        this@HardwareMonitoringLocalService.registerReceiver(mScreenOReceiver, mScreenOnFilter)

        /* 注册机器锁屏时的广播 */
        val mScreenOffFilter = IntentFilter("android.intent.action.SCREEN_OFF")
        this@HardwareMonitoringLocalService.registerReceiver(mScreenOReceiver, mScreenOffFilter)

        /* 注册机器解锁屏时的广播 */
        val mScreenPresentFilter = IntentFilter("android.intent.action.USER_PRESENT")
        this@HardwareMonitoringLocalService.registerReceiver(mScreenOReceiver, mScreenPresentFilter)
    }

    override fun onDestroy() {
        this@HardwareMonitoringLocalService.unregisterReceiver(mScreenOReceiver)
    }

    /**
     *
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // 通知
        val builder = Notification.Builder(this)
        // 小图标
        builder.setSmallIcon(R.mipmap.ic_launcher)
        // 前台服务
        startForeground(250, builder.build())

        // 绑定服务
        this@HardwareMonitoringLocalService.bindService(Intent(this,
                HardwareMonitoringRemoteService::class.java), conn, Context.BIND_IMPORTANT)

        // 获取硬件传感器管理者
        sensorManager = getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
        // 获取硬件传感器管理者
        sensorManager2 = getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager

        // 用于判断接收器接受设备管理器是否激活
        mAdminName = ComponentName(this, AdminManageReceiver::class.java)

        // 电源管理者
        pm = getSystemService(Context.POWER_SERVICE) as PowerManager

        // 获取方向传感器
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        // 监听方向传感器(全局实现)
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL)

        // 监听距离传感器
        sensorManager2.registerListener(object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

            }

            @SuppressLint("SetTextI18n")
            override fun onSensorChanged(p0: SensorEvent?) {
                // 判断距离1.近 0.远
                proximityX = p0!!.values[0]
            }

        }, sensorManager2.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_UI)


        mDPM = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager //在设备上执行管理政策

        /**
         * 它们的含义分别是：
         *
         * 1):START_STICKY：如果service进程被kill掉，保留service的状态为开始状态，
         * 但不保留递送的intent对象。随后系统会尝试重新创建service，由于服务状态为开始状态，
         * 所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。
         *
         * 2):START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务
         *
         * 3):START_REDELIVER_INTENT：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，
         * 系统会自动重启该服务，并将Intent的值传入。
         *
         * 4):START_STICKY_COMPATIBILITY：START_STICKY的兼容版本，但不保证服务被kill后一定能重启。
         */

        return START_REDELIVER_INTENT
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    @SuppressLint("WakelockTimeout")
    override fun onSensorChanged(p0: SensorEvent?) {

        // p0.values[0]: 左右角度
        // p0.values[1]: 上下角度
        // p0.values[2]: 东南西北

        // 如果APP负责亮屏
        if (appLock == APP_PRESENT) {

            Log.i("屏幕", "app开始处理逻辑")

            // 如果向上角度大于20度
            if (p0!!.values[1] < -20) {

                Log.i("屏幕", "app开始开屏处理逻辑")

                // 如果没有障碍物
                if (proximityX > 0) {

                    // 进了
                    if (!appInCome) {
                        // 如果已关屏
                        appInCome = true

                        if (!screenState) {

                            // 实现开屏
                            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                            val mWakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                                    or PowerManager.ACQUIRE_CAUSES_WAKEUP
                                    or PowerManager.ON_AFTER_RELEASE, TAG)

                            mWakelock.acquire()
                            mWakelock.release()

                            Log.i("屏幕", "app开屏")

                        }
                    }
                }
            } else if (p0.values[1] > -20) {

                // 延时两秒判断
                Handler().postDelayed({

                    if(!appInCome) {
                        Log.i("屏幕", "app开始关屏处理逻辑")

                        // 如果用户已激活锁屏
                        if (mDPM.isAdminActive(mAdminName)) {

                            Handler().postDelayed({

                                if (screenState) {

                                    Log.i("屏幕", "app开始关屏处理逻辑2")

                                    if (appLock == APP_PRESENT) {
                                        // 锁屏
                                        mDPM.lockNow()
                                        appInCome = false

                                        Log.i("屏幕", "app锁屏")
                                    }
                                }

                            }, 2000)

                        }
                    } else {

                    }
                }, 2000)
            }
        }
    }

    internal inner class MyBinder : ProcessService.Stub() {

        @Throws(RemoteException::class)
        override fun getServiceName(): String {
            return "I am HardwareMonitoringLocalService"
        }
    }

    internal inner class MyConn : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.i("Lift_Info", "与HardwareMonitoringRemoteService连接成功")
            val activityManager = this@HardwareMonitoringLocalService
                    .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            // 启动HardwareMonitoringRemoteService
            this@HardwareMonitoringLocalService.startService(Intent(this@HardwareMonitoringLocalService,
                    HardwareMonitoringRemoteService::class.java))
            //绑定HardwareMonitoringRemoteService
            this@HardwareMonitoringLocalService.bindService(Intent(this@HardwareMonitoringLocalService,
                    HardwareMonitoringRemoteService::class.java), conn, Context.BIND_IMPORTANT)
        }
    }

    /**
     * 锁屏的管理类叫KeyguardManager，
     * 通过调用其内部类KeyguardLockmKeyguardLock的对象的disableKeyguard方法可以取消系统锁屏，
     * newKeyguardLock的参数用于标识是谁隐藏了系统锁屏
     */
    private val mScreenOReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == "android.intent.action.SCREEN_ON") {
                Log.i("屏幕", "屏幕开了")
                screenState = true
            } else if (action == "android.intent.action.SCREEN_OFF") {
                // 如果用户解锁了,则锁屏
                if (appLock == USER_PRESENT) {
                    // 给回app处理
                    appLock = APP_PRESENT
                    Log.i("屏幕", "用户锁屏,交回app处理逻辑")
                }
                appInCome = false
                screenState = false
            } else if (action == "android.intent.action.USER_PRESENT") {
                appLock = USER_PRESENT
                Log.i("屏幕", "用户解屏")
            }
        }

    }

}