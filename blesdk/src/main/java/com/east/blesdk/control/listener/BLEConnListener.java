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

/**
 *|---------------------------------------------------------------------------------------------------------------|
 *  设备连接状态接口
 *  @date：2018/7/24 16:52
 *  @author East
 *|---------------------------------------------------------------------------------------------------------------|
 */
public interface BLEConnListener {

    /**
     * 连接设备异常
     * @param address
     * @param errorCode
     */
    default void onConnError(String address, int errorCode){};

    /**
     * 设备连接成功,设备服务 Discovered完毕后会回调(开启通知接受蓝牙数据返回)
     * 一般在这个方法里面开启通知，接受数据返回
     * @param address
     */
    default void onConnSuccessAndServicesDiscovered(BluetoothGatt gatt,String address){};

    /**
     * 设备已经连接
     * @param address
     */
    default void onAlreadyConnected(String address){};

}
