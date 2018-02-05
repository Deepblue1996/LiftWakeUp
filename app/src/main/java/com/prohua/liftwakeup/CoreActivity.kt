package com.prohua.liftwakeup

import android.app.AlarmManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.gyf.barlibrary.ImmersionBar
import com.prohua.liftwakeup.broadcast.AdminManageReceiver
import com.prohua.liftwakeup.service.HardwareMonitoringJobService
import com.prohua.liftwakeup.service.HardwareMonitoringLocalService
import com.prohua.liftwakeup.service.HardwareMonitoringRemoteService
import com.prohua.liftwakeup.util.AlarmTimer
import com.prohua.liftwakeup.weight.TitleBar
import kotlinx.android.synthetic.main.activity_main.*


/**
 * 主Activity
 */
class CoreActivity : AppCompatActivity() {

    private lateinit var mAdminName: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 状态栏透明和间距处理
        ImmersionBar.with(this)
                .statusBarDarkFont(false, 0.2f)
                // 原理：如果当前设备支持状态栏字体变色，会设置状态栏字体为黑色，
                // 如果当前设备不支持状态栏字体变色，会使当前状态栏加上透明度，否则不执行透明度
                .init()

        titleBar.setBack(R.drawable.ic_menu, getString(R.string.app_name), object : TitleBar.BackListener {
            override fun back() {
            }
        })

        startService(Intent(this, HardwareMonitoringLocalService::class.java))
        startService(Intent(this, HardwareMonitoringRemoteService::class.java))
        startService(Intent(this, HardwareMonitoringJobService::class.java))

        mAdminName = ComponentName(this, AdminManageReceiver::class.java)

        val mDPM = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager //在设备上执行管理政策

        if (!mDPM.isAdminActive(mAdminName)) {//如果未激活
            openCloseScreenText?.text = "①点击激活\n自动锁屏功能"
        } else {
            openCloseScreenText?.text = "①已激活\n自动锁屏功能"
        }
        openCloseScreen?.setOnClickListener {
            if (!mDPM.isAdminActive(mAdminName)) {//如果未激活
                showAdminManagement() //打开手机设备管理器
            }
        }

        AlarmTimer.setRepeatingAlarmTimer(this,
                System.currentTimeMillis(), 1 * 1000,
                "com.prohua.liftwakeup.destroy_timer",
                AlarmManager.RTC_WAKEUP)
    }

    private fun showAdminManagement() {
        // TODO Auto-generated method stub
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)//打开手机设备管理器的intent
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "自动锁屏")
        startActivityForResult(intent, 1200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 1200) {
            val mDPM = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager //在设备上执行管理政策

            if (!mDPM.isAdminActive(mAdminName)) {//如果未激活
                Toast.makeText(baseContext, "未激活", Toast.LENGTH_SHORT).show()
                openCloseScreenText?.text = "①点击激活\n自动锁屏功能"
            } else {
                Toast.makeText(baseContext, "激活成功", Toast.LENGTH_SHORT).show()
                openCloseScreenText?.text = "①已激活\n自动锁屏功能"
            }
        }
    }

}
