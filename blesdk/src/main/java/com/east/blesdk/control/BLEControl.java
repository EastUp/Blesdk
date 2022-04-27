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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;

import com.east.blesdk.bean.BLEUuid;
import com.east.blesdk.control.listener.BLEConnListener;
import com.east.blesdk.control.listener.BLEReadRssiListener;
import com.east.blesdk.control.listener.BLEStateChangeListener;
import com.east.blesdk.control.listener.BLETransportListener;
import com.east.blesdk.scan.BLEScanner;
import com.east.blesdk.util.BLEUtil;

/**
 *|---------------------------------------------------------------------------------------------------------------|
 *  蓝牙控制类，包括设备的连接、断开、数据发送，回调设置
 *  @date：2018/7/18
 *  @author East
 *|---------------------------------------------------------------------------------------------------------------|
 */
public class BLEControl {

    private static volatile BLEControl bleControl = null;
    private static volatile BLEGattCallBack sGattCallBack;

//    private final ThreadLocal<BLEReceiverListener> mBLEReceiverListener = new ThreadLocal<>();

    private BLEControl() {
        if (sGattCallBack == null) {
            sGattCallBack = new BLEGattCallBack();
        }
    }

    public static void init() {
        if (bleControl == null) {
            bleControl = new BLEControl();
        }
    }


    public static BLEControl getInstance() {
        if (bleControl != null) {
            return bleControl;
        }
        synchronized (BLEControl.class) {
            init();
        }
        return bleControl;
    }


    /**
     * 连接到指定的设备
     *
     * @param context
     * @param address  设备的mac地址
     */
    public void connectDevice(Context context, String address) {
        BLEScanner.getInstance().stopScan();    //连接设备前停止扫描设备
        BLEConnection.getInstance().connectToAddress(context, address, sGattCallBack, BluetoothDevice.TRANSPORT_AUTO);
    }

    /**
     * 连接到指定的设备
     *
     * @param context
     * @param address  设备的mac地址
     * @param transport 传输类型，不传为BluetoothDevice.TRANSPORT_AUTO，但一般是BluetoothDevice.TRANSPORT_LE
     */
    public void connectDevice(Context context, String address, int transport) {
        BLEScanner.getInstance().stopScan();    //连接设备前停止扫描设备
        BLEConnection.getInstance().connectToAddress(context, address, sGattCallBack, transport);
    }


    /**
     * 判断一个设备是否为连接状态
     *
     * @param deviceAddress
     * @return
     */
    public boolean isConnect(String deviceAddress) {

        return BLEUtil.isConnected(deviceAddress);
    }


    /**
     * 断开所以的设备连接
     * 多连时可用
     */
    public void disconnectAll() {
        BLEConnection.getInstance().disConnectAll();
    }

    /**
     * 断开指定设备的连接
     *
     * @param deviceAddress
     */
    public void disconnect(String deviceAddress) {
        BLEConnection.getInstance().disConnect(deviceAddress);
    }


    /**
     * 发送数据
     *
     * @param bleUuid
     */
    public boolean sendData(BLEUuid bleUuid) {

        return BLETransport.get().sendDataToDevice(bleUuid);
    }

    /**
     * 启用通知,设置能接收到蓝牙数据返回
     *
     * @param bleUuid
     */
    public void enableNotify(BLEUuid bleUuid) {
        BLETransport.get().enableNotify(bleUuid);
    }

    /**
     * 读取数据
     *
     * @param bleUuid
     */
    public boolean readDeviceData(BLEUuid bleUuid) {
        return BLETransport.get().readDeviceData(bleUuid);
    }


	/*
      |------------------------------------------------------------------------------------------------------|
	  <p>
	  |  BLEControl 提供给外部访问的 API   读取设备的 Rssi 信号值
	  <p>
	  |-----------------------------------------------------------------------------------------------------|
	 */

    /**
     * 读取设备的 Rssi
     *
     * @param deviceAddress
     */
    public void readGattRssi(String deviceAddress) {
        BluetoothGatt gatt = BLEUtil.getBluetoothGatt(deviceAddress);
        if (gatt == null) {
            return;
        }
        gatt.readRemoteRssi();


    }

    public BluetoothGatt getBluetoothGatt(String connectMac) {

        return BLEUtil.getBluetoothGatt(connectMac);
    }

	/*
      |------------------------------------------------------------------------------------------------------|
	  <p>
	  |  BLEControl 提供给外部访问的 API   设备回调相关
	  <p>
	  |-----------------------------------------------------------------------------------------------------|
	 */

    /**
     * 设备发送数据，接收数据的回调
     *
     * @param bleTransportListener
     */
    public void setBleTransportListener(BLETransportListener bleTransportListener) {
        if (sGattCallBack != null) {
            sGattCallBack.setBLETransportListener(bleTransportListener);
        }
    }

    /**
     * 设备连接的回调
     *
     * @param connectListener
     */
    public void setBleConnectListener(BLEConnListener connectListener) {
        BLEConnection.getInstance().setBleConnectListener(connectListener);
        if (sGattCallBack != null) {
            sGattCallBack.setBLEConnListener(connectListener);
        }
    }

    /**
     * 设备状态回调
     *
     * @param stateChangedListener
     */
    public void setBleStateChangedListener(BLEStateChangeListener stateChangedListener) {
        if (sGattCallBack != null) {
            sGattCallBack.setBLEStateChangeListener(stateChangedListener);
        }
    }

    /**
     * 设置远程设备连接的RSSI信号监听
     */
    public void setBleReadRssiListener(BLEReadRssiListener readRssiListener) {
        if (sGattCallBack != null) {
            sGattCallBack.setBLEReadRssiListener(readRssiListener);
        }
    }

/*    public void setBLEReceiverListener(BLEReceiverListener bleReceiverListener) {

        mBLEReceiverListener.set(bleReceiverListener);
        if (mBLEReceiverListener.get() == null) {
            mBLEReceiverListener.set(getBLEReceiverListener());
        }
        BLEStateReceiver.setBLEReceiverListener(mBLEReceiverListener.get());
    }

    private BLEReceiverListener getBLEReceiverListener() {
        return new BLEReceiverListener() {
            @Override
            public void onStateOff() {
                disconnectAll();
            }

            @Override
            public void onStateOn() {

            }

            @Override
            public void onStateStartOff() {

            }

            @Override
            public void onStateStartOn() {

            }
        };
    }*/


}
