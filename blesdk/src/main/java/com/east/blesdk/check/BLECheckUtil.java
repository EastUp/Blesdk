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

import com.east.blesdk.BLESdk;
import com.east.permission.PermissionCheckUtils;

/**
 * @package_name com.e.ble.check
 * @name ${name}
 * <p>
 * Created by xiaoyunfei on 2017/7/4.
 * @description
 */

public class BLECheckUtil {

    /**
     * API>= 23
     *
     * @param context
     * @return
     */
    public static boolean hasBlePermission(Context context) {
        return PermissionCheckUtils.hasPermissions(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public static boolean isSupportBle(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static boolean getBleEnable() {
        BluetoothAdapter bluetoothAdapter = BLESdk.getInstance().getBluetoothAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }
        return bluetoothAdapter.isEnabled();
    }

    public static void openBle() {
        BluetoothAdapter bluetoothAdapter = BLESdk.getInstance().getBluetoothAdapter();
        if (bluetoothAdapter == null) {
            return;
        }
        bluetoothAdapter.enable();
    }

    public void openBle(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

}
