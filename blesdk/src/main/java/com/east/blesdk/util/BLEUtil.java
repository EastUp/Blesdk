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

package com.east.blesdk.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.os.DeadObjectException;

import com.east.blesdk.BLESdk;
import com.east.blesdk.bean.BLECharacteristic;
import com.east.blesdk.bean.BLEConnBean;
import com.east.blesdk.control.BLEConnList;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 * 蓝牙工具类
 * 在进行BLE开发过程中可能会遇到操作失败等情况,这个时候可能需要断开与BLE的连接或者清理相关资源.在BluetoothGatt类中有两个相关的方法
 * 1. disconnect()
 * 2. close()
 * 那么这个两个方法有什么区别,又该如何使用呢.
 * disconnect()方法: 如果调用了该方法之后可以调用connect()方法进行重连,这样还可以继续进行断开前的操作.
 * close()方法: 一但调用了该方法, 如果你想再次连接,必须调用BluetoothDevice的connectGatt()方法. 因为close()方法将释放BluetootheGatt的所有资源.
 * 需要注意的问题:
 * 当你需要手动断开时,调用disconnect()方法，此时断开成功后会回调onConnectionStateChange方法,在这个方法中再调用close方法释放资源。
 * 如果在disconnect后立即调用close，会导致无法回调onConnectionStateChange方法。
 *
 * @author East
 * |---------------------------------------------------------------------------------------------------------------|
 * @date：2018/7/24 16:17
 */
public class BLEUtil {

    /**
     * 判断设备是否已经连接
     *
     * @param address
     * @return
     */
    public static boolean isConnected(String address) {
        BluetoothManager manager = BLESdk.getInstance().getBluetoothManager();
        if (manager == null) {
            return false;
        }
//        List<BluetoothDevice> bindList = getBindDevices();
//        if (bindList == null) {
//            return false;
//        }
////        BLELog.i("--->>>bindList::" + bindList.size());
//
//        BluetoothDevice device = null;
//        for (BluetoothDevice bluetoothDevice : bindList) {
//            String bindDevice = bluetoothDevice.getAddress();
//
//            if (TextUtils.equals(bindDevice, address)) {
//                device = bluetoothDevice;
////                BLELog.i("--->>>contain bindDevice::" + bindDevice);
//            }
//        }
//        if (device == null) {
//            return false;
//        }
        BluetoothAdapter adapter = BLESdk.getInstance().getBluetoothAdapter();
        if (adapter == null) {
            return false;
        }

        BluetoothDevice device = adapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        }

        int state = manager.getConnectionState(device, BluetoothProfile.GATT);
//        BLELog.i("--->>> " + address + " " + getStateString(state));
        return state == BluetoothProfile.STATE_CONNECTED;
    }

    public static List<BluetoothDevice> getBindDevices() {

        BluetoothManager manager = BLESdk.getInstance().getBluetoothManager();
        if (manager == null) {
            return null;
        }

        return manager.getConnectedDevices(BluetoothProfile.GATT_SERVER);
    }


    public static void disConnect(String address, BluetoothGatt gatt) {

        BluetoothAdapter adapter = BLESdk.getInstance().getBluetoothAdapter();
        if (adapter == null || !adapter.isEnabled() || gatt == null) {
            return;
        }
        if (isConnected(address)) {
            gatt.disconnect();
        }
        try {
            refreshCache(gatt);
        } catch (DeadObjectException e) {
            e.printStackTrace();
        }

        //不能立即调用close方法，否则会导致无法回调onConnectionStateChange方法（在回调的onConnectionStateChange中再关闭）
//        gatt.close();
    }


    public static BluetoothGatt getBluetoothGatt(String address) {
        BLEConnBean connBean = BLEConnList.get().getContainBean(address);
        if (connBean == null) {
            return null;
        }
        return connBean.getBluetoothGatt();
    }


    private static void refreshCache(BluetoothGatt gatt) throws DeadObjectException {

        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod(
                    "refresh");
            if (localMethod == null) {

                return;
            }

            localMethod.invoke(localBluetoothGatt);

        } catch (Exception localException) {
            BLELog.e("An exception occured while refreshing device");
        }
    }

    /**
     * 组装 BLECharacter
     *
     * @param gatt
     * @param characteristic
     * @return
     */
    public static BLECharacteristic getBleCharacter(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {
        String address = getConnectDevice(gatt);
        UUID uuid = characteristic.getUuid();
        byte[] value = characteristic.getValue();

        BLECharacteristic.BLECharacterBuilder bleCharacterBuilder =
                new BLECharacteristic.BLECharacterBuilder(value);
        return bleCharacterBuilder
                .setDeviceAddress(address)
                .setCharacteristicUUID(uuid).builder();
    }

    /**
     * 从连接的 BluetoothGatt 中获取当前设备的 mac 地址
     *
     * @param gatt
     * @return
     */
    public static String getConnectDevice(BluetoothGatt gatt) {
        String address = "";
        if (gatt == null) {
            return address;
        }
        BluetoothDevice device = gatt.getDevice();
        if (device != null) {
            address = device.getAddress();
        }
        return address;
    }


    private static String getStateString(int state) {
        String message = "";
        switch (state) {
            case BluetoothProfile.STATE_CONNECTED:
                message = "已连接";
                break;
            case BluetoothProfile.STATE_CONNECTING:
                message = "正在连接";
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                message = "已断开连接";
                break;
            case BluetoothProfile.STATE_DISCONNECTING:
                message = "正在断开连接";
                break;
        }
        return message;
    }

}
