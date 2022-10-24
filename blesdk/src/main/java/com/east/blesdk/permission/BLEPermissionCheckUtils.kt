package com.east.blesdk.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.east.permission.rxpermissions.RxPermissions

/**
 * |---------------------------------------------------------------------------------------------------------------|
 *  @description:   Wifi的权限检测工具
 *  @author: East
 *  @date: 2019-12-04
 * |---------------------------------------------------------------------------------------------------------------|
 */
object BLEPermissionCheckUtils {
    fun checkPermission(activity: FragmentActivity,listener : PermissionListener) {
        val lm = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val open = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!open) {
            showNeedGpsOpenDialog(activity)
            return
        }

        val permissions = // Android 版本大于等于 12 时，申请新的蓝牙权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
                //根据实际需要申请定位权限
            } else {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }
        val rxPermissions = RxPermissions(activity)
        rxPermissions.requestEachCombined(*permissions).subscribe {
            when {
                it.granted -> {
                    listener.onGranted()
                }
                it.shouldShowRequestPermissionRationale -> {
                    checkPermission(
                        activity,
                        listener
                    )
                }
                else -> {
                    showMissingPermissionDialog(
                        activity,
                        listener
                    )
                }
            }
        }
    }

    fun checkPermission(fragment: Fragment, listener : PermissionListener) {
        val lm = fragment.requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val open = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!open) {
            showNeedGpsOpenDialog(fragment.requireContext())
            return
        }
        val permissions = // Android 版本大于等于 12 时，申请新的蓝牙权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
                //根据实际需要申请定位权限
            } else {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }
        val rxPermissions = RxPermissions(fragment)
        rxPermissions.requestEachCombined(*permissions).subscribe {
            when {
                it.granted -> {
                    listener.onGranted()
                }
                it.shouldShowRequestPermissionRationale -> {
                    checkPermission(
                        fragment,
                        listener
                    )
                }
                else -> {
                    showMissingPermissionDialog(
                        fragment.requireContext(),
                        listener
                    )
                }
            }
        }
    }


    fun showMissingPermissionDialog(context: Context,listener: PermissionListener) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("提示")
        builder.setMessage("当前应用缺少必要权限。请点击\"设置\"-\"权限\"-打开所需权限。")

        // 拒绝, 退出应用
        builder.setNegativeButton(
            "取消"
        ) { _, _ -> listener.onCancel() }

        builder.setPositiveButton(
            "设置"
        ) { _, _ ->
            startAppSettings(
                context
            )
        }

        builder.setCancelable(false)

        builder.show()
    }

    /**
     * 启动应用的设置
     */
    fun startAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        )
        intent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
//        finish()
    }

    /**
     * 进入位置gps界面
     */
    fun showNeedGpsOpenDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("提示")
        builder.setMessage("受权限影响，蓝牙需要打开gps才能使用")

        // 拒绝, 退出应用
        builder.setNegativeButton(
            "取消"
        ) { _, _ ->  }

        builder.setPositiveButton(
            "设置"
        ) { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }

        builder.setCancelable(false)

        builder.show()
    }
}