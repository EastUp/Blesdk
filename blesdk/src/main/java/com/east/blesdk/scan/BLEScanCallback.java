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

package com.east.blesdk.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.text.TextUtils;

import com.east.blesdk.bean.BLEDevice;
import com.east.blesdk.support.BluetoothUuid;
import com.east.blesdk.support.ScanRecord;
import com.east.blesdk.util.BLELog;

import java.util.List;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 * 自定义的扫描回调 继承自BluetoothAdapter.LeScanCallback
 *
 * @author East
 * |---------------------------------------------------------------------------------------------------------------|
 * @date：2018/7/19 17:18
 */
class BLEScanCallback implements BluetoothAdapter.LeScanCallback {
    //保证扫描到设备更新UI的时候是在主线程
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private String[] nameFilters; //名称过滤
    private ParcelUuid[] uuidFilters; //uuid过滤
    private String[] addressFilters; //地址过滤

    private BLEScanListener mBLEScanListener;//扫描的回调

    public BLEScanCallback(BLEScanListener bleScanListener) {
        this.mBLEScanListener = bleScanListener;
    }

    /**
     *  这些参数都从BleScanCfg中获取
     * @param nameFilters
     * @param uuidFilter
     * @param addressFilters
     * @param bleScanListener
     */
    public BLEScanCallback(String[] nameFilters, ParcelUuid[] uuidFilter,String[] addressFilters, BLEScanListener bleScanListener) {

        this.nameFilters = nameFilters;
        this.uuidFilters = uuidFilter;
        this.addressFilters = addressFilters;
        this.mBLEScanListener = bleScanListener;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        if (nameFilters != null && nameFilters.length != 0) {
            if (!matchingName(device)) {
                return;
            }
        }

        if (addressFilters != null && addressFilters.length != 0) {
            if (!matchingAddress(device)) {
                return;
            }
        }

        ScanRecord sr = ScanRecord.parseFromBytes(scanRecord);
        if (uuidFilters != null && uuidFilters.length != 0) {
            if (!matchingUUID(sr)) {
                return;
            }
        }

        final BLEDevice bleDevice = new BLEDevice(device.getName(), device.getAddress(), rssi, sr);

        if (mBLEScanListener == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBLEScanListener.onScanning(bleDevice);
            }
        });
    }

    public String[] getNameFilters() {
        return nameFilters;
    }

    public void setNameFilters(String[] nameFilters) {
        this.nameFilters = nameFilters;
    }

    public ParcelUuid[] getUuidFilters() {
        return uuidFilters;
    }

    public void setUuidFilters(ParcelUuid[] uuidFilters) {
        this.uuidFilters = uuidFilters;
    }

    public String[] getAddressFilters() {
        return addressFilters;
    }

    public void setAddressFilters(String[] addressFilters) {
        this.addressFilters = addressFilters;
    }

    /**
     * 名称过滤,区分大小写
     *
     * @param device
     * @return
     */
    private boolean matchingName(BluetoothDevice device) {

        if (nameFilters == null || nameFilters.length == 0) {
            return true;
        }

        String name = device.getName();
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        boolean matching = false;

        for (int i = 0; i < nameFilters.length; i++) {
            String filter = nameFilters[i];
            if (filter.isEmpty()) {
                continue;
            }
            if (TextUtils.equals(name, filter) || name.contains(filter)) {
                matching = true;
                break;
            }
        }

        return matching;
    }

    /**
     * 过滤UUIDs
     *
     * @param sr
     * @return
     */
    private boolean matchingUUID(ScanRecord sr) {
        if (sr == null || uuidFilters == null || uuidFilters.length == 0) {
            return true;
        }
        //获取广播出来的UUID,不只一个
        List<ParcelUuid> parcelUuids = sr.getServiceUuids();

        if (parcelUuids == null || parcelUuids.size() == 0) {
            return false;
        }
        ParcelUuid[] parcelUuid = new ParcelUuid[parcelUuids.size()];
        for (int i = 0; i < parcelUuids.size(); i++) {
            parcelUuid[i] = parcelUuids.get(i);
        }

        boolean matching = BluetoothUuid.containsAnyUuid(parcelUuid, uuidFilters);
        BLELog.e("findUUID == " + matching);
        return matching;
    }


    /**
     * 过滤mac地址
     *
     * @param device
     * @return
     */
    private boolean matchingAddress(BluetoothDevice device) {
        if (addressFilters == null || addressFilters.length == 0) {
            return true;
        }

        String address = device.getAddress();
        if (TextUtils.isEmpty(address)) {
            return false;
        }

        boolean matching = false;

        for (int i = 0; i < addressFilters.length; i++) {
            String filter = addressFilters[i];
            if (filter.isEmpty()) {
                continue;
            }
            if (TextUtils.equals(address, filter)) {
                matching = true;
                break;
            }
        }
        return matching;
    }

}