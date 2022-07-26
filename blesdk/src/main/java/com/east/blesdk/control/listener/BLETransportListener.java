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

package com.east.blesdk.control.listener;


import android.bluetooth.BluetoothGatt;

import com.east.blesdk.bean.BLECharacteristic;

/**
 *|---------------------------------------------------------------------------------------------------------------|
 *  开启通知，数据发送接收回调等
 *  @date：2018/7/24 16:52
 *  @author East
 *|---------------------------------------------------------------------------------------------------------------|
 */
public interface BLETransportListener {
    /**
     * 回调描述符读取操作结果
     */
    default void onDescriptorRead(BluetoothGatt gatt, String address){};

    /**
     * 回调描述符写入操作结果(订阅成功后会回调这个方法)
     */
    default void onDescriptorWrite(BluetoothGatt gatt,String address){};

    /**
     * 回调报告特征读取操作的结果。
     */
    default void onCharacteristicRead(BluetoothGatt gatt,BLECharacteristic bleCharacteristic){};

    /**
     * 特征写入操作的回调
     */
    default void onCharacteristicWrite(BluetoothGatt gatt,BLECharacteristic bleCharacteristic){};

    /**
     * 当特征改变的时候(设备通过Notification发送数据过来时)，回调该方法
     */
    default void onCharacteristicChanged(BluetoothGatt gatt,BLECharacteristic bleCharacteristic){};//当设备通过Notification发送数据过来时，回调该方法
}
