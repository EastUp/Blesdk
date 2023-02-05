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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.text.TextUtils;

import com.east.blesdk.bean.BLECharacteristic;
import com.east.blesdk.control.listener.BLEConnListener;
import com.east.blesdk.control.listener.BLEReadRssiListener;
import com.east.blesdk.control.listener.BLEStateChangeListener;
import com.east.blesdk.control.listener.BLETransportListener;
import com.east.blesdk.util.BLEByteUtil;
import com.east.blesdk.util.BLELog;
import com.east.blesdk.util.BLEUtil;

import java.util.UUID;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 * 连接的回调，继承自BluetoothGattCallback
 *
 * @author East
 * |---------------------------------------------------------------------------------------------------------------|
 * @date：2018/7/20 15:17
 */
class BLEGattCallBack extends BluetoothGattCallback {

    private Handler mHandler = new Handler();

    private BLEStateChangeListener mBLEStateChangeListener = null;//设备连接状态改变监听
    private BLEConnListener mBLEConnListener = null;//连接监听
    private BLEReadRssiListener mBLEReadRssiListener = null;//信号的监听
    private BLETransportListener mBLETransportListener = null; //数据传输的监听

    public BLEGattCallBack() {
    }

    public BLEGattCallBack(BLEStateChangeListener BLEStateChangeListener, BLEConnListener BLEConnListener, BLEReadRssiListener BLEReadRssiListener, BLETransportListener BLETransportListener) {
        mBLEStateChangeListener = BLEStateChangeListener;
        mBLEConnListener = BLEConnListener;
        mBLEReadRssiListener = BLEReadRssiListener;
        mBLETransportListener = BLETransportListener;
    }

    /**
     * 设备连接状态的改变
     *
     * @param gatt
     * @param status
     * @param newState
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt,
                                        int status,
                                        int newState) {

        //返回蓝牙连接状态
        updateConnectionState(gatt, status, newState);

        super.onConnectionStateChange(gatt, status, newState);
    }

    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {

        //蓝牙 设备所包含的 service 可被启用状态，
        // 注意:
        // 如果蓝牙回复数据是通过notify的方式
        // 此时，发数据给设备并不能接收到设备，
        // 必须先 ENABLE_NOTIFICATION_VALUE，才可用
        for (BluetoothGattService service: gatt.getServices()) {
            BLELog.dNew("find service：" + service.getUuid() + "\n");
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                int charaProp = characteristic.getProperties();
                String text = "";
                // 可读
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    text = "read";
                }
                // 可写，注：要 & 其可写的两个属性
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0
                        || (charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0
                        ) {
                    text = "write";
                }
                // 可通知，可指示
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0
                        || (charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0
                        ) {
                    text = "notify | indication";
                }
                BLELog.dNew("-------find characteristic：" + text + "----" + charaProp + "--uuid:" + characteristic.getUuid() +"\n");
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    BLELog.dNew("--------------find descriptor：" + descriptor.getUuid() + "\n");
                }
            }
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String address = BLEUtil.getConnectDevice(gatt);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (mBLEConnListener != null)
                        mBLEConnListener.onConnSuccessAndServicesDiscovered(gatt,address);
                } else {
                    if (mBLEConnListener != null)
                        mBLEConnListener.onConnError(address, status);
                }
            }
        });
        super.onServicesDiscovered(gatt, status);
    }

    /**
     * 回调报告特征读取操作的结果。
     */
    @Override
    public void onCharacteristicRead(final BluetoothGatt gatt,
                                     final BluetoothGattCharacteristic characteristic,
                                     final int status) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BLECharacteristic bleCharacteristic = BLEUtil.getBleCharacter(gatt, characteristic);
                    if (mBLETransportListener != null)
                        mBLETransportListener.onCharacteristicRead(gatt,bleCharacteristic);
                }
            }
        });

        super.onCharacteristicRead(gatt, characteristic, status);
    }

    /**
     * 特征写入操作的回调
     */
    @Override
    public void onCharacteristicWrite(final BluetoothGatt gatt,
                                      final BluetoothGattCharacteristic characteristic,
                                      final int status) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BLECharacteristic bleCharacteristic = BLEUtil.getBleCharacter(gatt, characteristic);
//            if (bleCharacter != null) {
//                BLELog.i("----->>>BLEGattCallBack::" +
//                        "\nonCharacteristicWrite::  "
//                        + BLEByteUtil.getHexString(bleCharacter.getDataBuffer()));
//            }
                    if (mBLETransportListener != null)
                        mBLETransportListener.onCharacteristicWrite(gatt,bleCharacteristic);
                }
            }
        });
        super.onCharacteristicWrite(gatt, characteristic, status);

    }

    /**
     * 当特征改变的时候(设备通过Notification发送数据过来时)，回调该方法
     */
    @Override
    public void onCharacteristicChanged(final BluetoothGatt gatt,
                                        final BluetoothGattCharacteristic characteristic) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                BLECharacteristic bleCharacteristic = BLEUtil.getBleCharacter(gatt, characteristic);
                if (mBLETransportListener != null)
                    mBLETransportListener.onCharacteristicChanged(gatt,bleCharacteristic);
            }
        });

        super.onCharacteristicChanged(gatt, characteristic);
    }

    /**
     * 回调描述符读取操作结果
     */
    @Override
    public void onDescriptorRead(final BluetoothGatt gatt,
                                 BluetoothGattDescriptor descriptor,
                                 int status) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String address = BLEUtil.getConnectDevice(gatt);
                if (mBLETransportListener != null)
                    mBLETransportListener.onDescriptorRead(gatt,address);
            }
        });

        super.onDescriptorRead(gatt, descriptor, status);
    }

    /**
     * 回调描述符写入操作结果
     */
    @Override
    public void onDescriptorWrite(final BluetoothGatt gatt,
                                  final BluetoothGattDescriptor descriptor,
                                  final int status) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String address = BLEUtil.getConnectDevice(gatt);
                if (mBLETransportListener != null)
                    mBLETransportListener.onDescriptorWrite(gatt,address);
                UUID uuid = descriptor.getUuid();
                byte[] bytes = descriptor.getValue();
                BLELog.i(" onDescriptorWrite() address::" + address +
                        "\n status:" + status +
                        "\n value:" + BLEByteUtil.getHexString(bytes) +
                        "\n uuid:" + uuid.toString());
            }
        });

        super.onDescriptorWrite(gatt, descriptor, status);
    }

    /**
     * 完成可靠的写入事务时调用回调
     */
    @Override
    public void onReliableWriteCompleted(final BluetoothGatt gatt,
                                         final int status) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String address = BLEUtil.getConnectDevice(gatt);
                BLELog.e("onReliableWriteCompleted() address::" + address + ";status:" + status);
            }
        });

        super.onReliableWriteCompleted(gatt, status);
    }

    /**
     * 回调报告远程设备连接的RSSI。
     */
    @Override
    public void onReadRemoteRssi(final BluetoothGatt gatt,
                                 final int rssi,
                                 final int status) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String address = BLEUtil.getConnectDevice(gatt);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (mBLEReadRssiListener != null)
                        mBLEReadRssiListener.onReadRssi(address, rssi);
                } else {
                    if (mBLEReadRssiListener != null)
                        mBLEReadRssiListener.onReadRssiError(address, status);
                }
            }
        });

        super.onReadRemoteRssi(gatt, rssi, status);
    }

    //--------------------------------------------------------------------------------------------//
    //-------------------BLEGattCallBack 内部逻辑代码-----------------------------------------------//
    //--------------------------------------------------------------------------------------------//

    /**
     * 更新与设备的连接状态，
     * GATT_SUCCESS 时调用gatt.discoverServices()，
     * 不返会设备连接成功，当discoverServices 成功时返回连接成功
     *
     * @param gatt
     * @param status
     * @param newState
     */
    private void updateConnectionState(final BluetoothGatt gatt, final int status, final int newState) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String address = BLEUtil.getConnectDevice(gatt);
                if (TextUtils.isEmpty(address)) {
                    if (mBLEConnListener != null)
                        mBLEConnListener.onConnError(address, 0);
                }
                /*BLEConnBean connBean = BLEConnList.get().getContainBean(address);
                if (connBean == null) {
                    return;
                }*/
                BLELog.i("BLEGattCallBack :: updateConnectionState()" +
                        "\n address: " + address +
                        "\n status == " + status +
                        "\n newState == " + newState);


                if (status != BluetoothGatt.GATT_SUCCESS) {
                    //蓝牙异常断开
                    if (mBLEConnListener != null)
                        mBLEConnListener.onConnError(address, status);
                    if (mBLEStateChangeListener != null)
                        mBLEStateChangeListener.onStateDisConnected(address);
                    //断开连接
                    BLEConnList.get().delConnDevice(address);
                    //关闭gatt （这里添加上，避免重新连接时接收到的数据重复）
                    if(gatt!= null)
                        gatt.close();
                    return;
                }

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (mBLEStateChangeListener != null)
                        mBLEStateChangeListener.onStateDisConnected(address);
                    BLEConnList.get().delConnDevice(address);
                    //关闭gatt
                    if(gatt!= null)
                        gatt.close();
                }else if (newState == BluetoothProfile.STATE_CONNECTED) {
                    //已连接成功,开启扫描服务
                    gatt.discoverServices();
                    if (mBLEStateChangeListener != null)
                        mBLEStateChangeListener.onStateConnected(address);
                    return;
                } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                    if (mBLEStateChangeListener != null)
                        mBLEStateChangeListener.onStateConnecting(address);
                }
            }
        });
    }

    public BLEStateChangeListener getBLEStateChangeListener() {
        return mBLEStateChangeListener;
    }

    public void setBLEStateChangeListener(BLEStateChangeListener BLEStateChangeListener) {
        mBLEStateChangeListener = BLEStateChangeListener;
    }

    public BLEConnListener getBLEConnListener() {
        return mBLEConnListener;
    }

    public void setBLEConnListener(BLEConnListener BLEConnListener) {
        mBLEConnListener = BLEConnListener;
    }

    public BLEReadRssiListener getBLEReadRssiListener() {
        return mBLEReadRssiListener;
    }

    public void setBLEReadRssiListener(BLEReadRssiListener BLEReadRssiListener) {
        mBLEReadRssiListener = BLEReadRssiListener;
    }

    public BLETransportListener getBLETransportListener() {
        return mBLETransportListener;
    }

    public void setBLETransportListener(BLETransportListener BLETransportListener) {
        mBLETransportListener = BLETransportListener;
    }
}
