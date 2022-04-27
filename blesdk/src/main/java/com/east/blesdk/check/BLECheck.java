/*
 * Copyright (c) 2017. xiaoyunfei
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.east.blesdk.check;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.east.blesdk.BLESdk;
import com.east.blesdk.util.BLELog;
import com.east.permission.PermissionCheckUtils;

/**
 *|---------------------------------------------------------------------------------------------------------------|
 *  蓝牙检测工具
 *  @date：2018/7/20 10:25
 *  @author East
 *|---------------------------------------------------------------------------------------------------------------|
 */
public class BLECheck{

    private BluetoothAdapter bluetoothAdapter;

    private BLECheck() {
    }

    private static volatile BLECheck bleCheck = null;

    public static void init() {
        if (bleCheck == null) {
            synchronized (BLECheck.class){
                if (bleCheck == null){
                    bleCheck = new BLECheck();
                    BLELog.i("BLECheck init ok");
                }
            }
        }
    }

    public static BLECheck getInstance() {
        if (bleCheck == null) {
            init();
        }
        return bleCheck;
    }


    /**
     * 检测蓝牙状态
     *
     * @param context
     * @param checkListener
     */
    public synchronized void checkBleState(Context context, BLECheckListener checkListener) {
        //是否支持蓝牙设备
        if (!isSupportBle(context)) {
            checkListener.notSupportBle();
            return;
        }

        //蓝牙是否打开
        if (!isBleEnable()) {
            checkListener.bleClosing();
            return;
        }

        //检测是否有蓝牙的权限
        if (!checkBlePermission(context)) {
            checkListener.noBluetoothPermission();
            return;
        }

        //蓝牙状态可用
        checkListener.bleStateOK();
    }

    /**
     * 蓝牙是否打开可用状态
     *
     * @return
     */
    public synchronized boolean isBleEnable() {

        bluetoothAdapter = BLESdk.getInstance().getBluetoothAdapter();
        if (bluetoothAdapter == null) {
            BLELog.i("BleCheck  isBleEnable() bluetoothAdapter is null");
            return false;
        }

        return bluetoothAdapter.isEnabled();
    }

    /**
     * 是否支持蓝牙设备
     *
     * @param context
     * @return
     */
    public synchronized boolean isSupportBle(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 是否具有访问蓝牙的权限蓝牙
     *
     * @param context
     * @return
     */
    public boolean checkBlePermission(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }

        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION};
        return PermissionCheckUtils.hasPermissions(context, perms);
    }

    /**
     * 打开蓝牙,软件内打开，不需要跳转的系统界面
     */
    public void openBle() {
        if (bluetoothAdapter == null) {
            return;
        }
        bluetoothAdapter.enable();
    }

    /**
     * 跳转到系统界面，打开蓝牙
     *
     * @param activity
     * @param requestCode
     */
    public void openBle(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }
}
