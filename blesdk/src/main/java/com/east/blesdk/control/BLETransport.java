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

package com.east.blesdk.control;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.east.blesdk.BLESdk;
import com.east.blesdk.bean.BLEUuid;
import com.east.blesdk.util.BLELog;
import com.east.blesdk.util.BLEUtil;

import java.util.UUID;


/**
 *|---------------------------------------------------------------------------------------------------------------|
 *  蓝牙数据传输类
 *  @date：2018/7/19
 *  @author East
 *|---------------------------------------------------------------------------------------------------------------|
 */
class BLETransport{

    private static BLETransport bleTransport = null;

    private BLETransport() {
        ThreadLocal<BluetoothAdapter> bluetoothAdapter = new ThreadLocal<>();
        bluetoothAdapter.set(BLESdk.getInstance().getBluetoothAdapter());
    }

    public static BLETransport get() {
        if (bleTransport == null) {
            bleTransport = new BLETransport();
        }
        return bleTransport;
    }


    /**
     * 发送数据
     *
     * @param bleUuid
     */
    public boolean sendDataToDevice(final BLEUuid bleUuid) {

        final BluetoothGatt bluetoothGatt = getEnableBleGatt(bleUuid);
        if (bluetoothGatt == null) {
            return false;
        }

        UUID serviceUuid = bleUuid.getServiceUUID();
        UUID characteristicUuid = bleUuid.getCharacteristicUUID();
        BluetoothGattCharacteristic characteristic =
                getCharacteristicByUUID(bluetoothGatt, serviceUuid, characteristicUuid);
        if (characteristic == null) {
            return false;
        }
        BLELog.i("BLETransport  :: 发送数据-->>:");
//        BLEByteUtil.printHex(bleUuid.getDataBuffer());
        characteristic.setValue(bleUuid.getDataBuffer());
        return bluetoothGatt.writeCharacteristic(characteristic);

    }

    @Nullable
    private BluetoothGatt getEnableBleGatt(BLEUuid bleUuid) {
        String address = bleUuid.getAddress();
        if (TextUtils.isEmpty(address) || !BLEUtil.isConnected(address)) {
            return null;
        }

        return BLEUtil.getBluetoothGatt(address);
    }

    /**
     * 通知的启用和关闭
     *
     * @param bleUuid
     */
    public void enableNotify(BLEUuid bleUuid) {
        BluetoothGatt bluetoothGatt = getEnableBleGatt(bleUuid);
        if (bluetoothGatt == null) {
            return;
        }

        UUID serviceUuid = bleUuid.getServiceUUID();
        UUID characteristicUuid = bleUuid.getCharacteristicUUID();
        BluetoothGattCharacteristic characteristic = getCharacteristicByUUID(bluetoothGatt,
                serviceUuid,
                characteristicUuid);
        if (characteristic == null) {
            return;
        }
        UUID descriptorUuid = bleUuid.getDescriptorUUID();
        changeNotifyState(bluetoothGatt, characteristic,
                descriptorUuid, bleUuid.isEnable());
        BLELog.i("enableNotify()-->>characteristicUuid:" + characteristicUuid);

    }

    /**
     * 读取数据
     *
     * @param bleUuid
     */
    public boolean readDeviceData(BLEUuid bleUuid) {
        BluetoothGatt bluetoothGatt = getEnableBleGatt(bleUuid);
        if (bluetoothGatt == null) {
            return false;
        }
        UUID serviceUuid = bleUuid.getServiceUUID();
        UUID characteristicUuid = bleUuid.getCharacteristicUUID();

        BluetoothGattCharacteristic characteristic =
                getCharacteristicByUUID(bluetoothGatt,
                        serviceUuid,
                        characteristicUuid);
        if (characteristic == null) {
            return false;
        }
        return bluetoothGatt.readCharacteristic(characteristic);
    }


    /**
     * 获取指定的 GattCharacteristic
     *
     * @param serviceUuid
     * @param characteristicUuid
     * @return
     */
    private BluetoothGattCharacteristic getCharacteristicByUUID(
            BluetoothGatt bluetoothGatt,
            UUID serviceUuid,
            UUID characteristicUuid) {
        BluetoothGattService service = bluetoothGatt.getService(serviceUuid);
        if (service == null) {
            return null;
        }
        return service.getCharacteristic(characteristicUuid);
    }

    /**
     * 改变通知状态
     *
     * @param characteristic
     * @param descriptorUuid
     * @param enable
     */
    private void changeNotifyState(BluetoothGatt bluetoothGatt,
                                   BluetoothGattCharacteristic characteristic,
                                   UUID descriptorUuid,
                                   boolean enable) {
        bluetoothGatt.setCharacteristicNotification(characteristic, enable);//激活通知

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUuid);
        if (descriptor == null) {
            BLELog.e("descriptorUuid not find");
            return;
        }
        if (enable) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        BLELog.i("开启通知完成");
        bluetoothGatt.writeDescriptor(descriptor);
    }
}
