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

import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import com.east.blesdk.BLESdk;
import com.east.blesdk.check.BLECheckUtil;
import com.east.blesdk.util.BLEError;
import com.east.blesdk.util.BLELog;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 * 蓝牙扫描工具类只提供 开始扫描&停止扫描
 *
 * @author East
 * |---------------------------------------------------------------------------------------------------------------|
 * @date：2018/7/18
 */
public class BLEScanner {
    /**
     * INSTANCE
     */
    private static volatile BLEScanner bleScanner = null;

    /**
     * 无限时长扫描，用户手动调用停止扫描
     */
    public static final int INFINITE = -1;

    /**
     * 扫描时长
     */
    private int scanTime = 5000;
    /**
     * 名称过滤
     */
    private String[] nameFilter = null;
    /**
     * UUID 过滤
     */
    private ParcelUuid[] uuidFilter = null;

    /**
     * 地址过滤
     */
    private String[] addressFilter = null;

    private BLEScanCallback mBLEScanCallback;

    /**
     * 扫描监听
     */
    private BLEScanListener bleScanListener;

    private Handler handler = null;

    private BLEScanner() {

    }

    /**
     * 初始化
     */
    public static void init() {
        if (bleScanner == null) {
            synchronized (BLEScanner.class) {
                if (bleScanner == null) {
                    bleScanner = new BLEScanner();
                    BLELog.i("BLEScannerCompat init ok");
                }
            }
        }
    }

    public static BLEScanner getInstance() {
        if (bleScanner == null) {
            init();
        }
        return bleScanner;
    }


    /**
     * 开始扫描设备<br/>
     *
     * @param bleScanCfg
     * @param scanListener
     */
    public boolean startScanner(BLEScanCfg bleScanCfg, BLEScanListener scanListener) {

        if (scanListener == null) {
            BLELog.e("scanListener is null");
            return false;
        }
        this.bleScanListener = scanListener;

        if (!BLECheckUtil.getBleEnable()) {
            if (bleScanListener != null)
                bleScanListener.onScannerError(BLEError.BLE_CLOSE);

            tryToStopScanner();
            if (bleScanListener != null)
                bleScanListener.onScannerStop();
            return false;
        }

        //先尝试关闭之前的扫描
        tryToStopScanner();

        //创建新的扫描
        initScanConfig(bleScanCfg);

        return startScanner();
    }

    /**
     * 结束扫描
     */
    public void stopScan() {
        tryToStopScanner();
        if (bleScanListener != null)
            bleScanListener.onScannerStop();

    }


    private void initScanConfig(BLEScanCfg bleScanCfg) {
        if (bleScanCfg == null) {
            bleScanCfg = getDefaultScanCfg();
        }
        scanTime = bleScanCfg.getScanTime();
        nameFilter = bleScanCfg.getNameFilter();
        uuidFilter = bleScanCfg.getParcelUuidFilters();
        addressFilter = bleScanCfg.getAddressFilter();
        mBLEScanCallback = new BLEScanCallback(nameFilter, uuidFilter, addressFilter, bleScanListener);
    }

    /**
     * 尝试停止扫描设备
     */
    private void tryToStopScanner() {
        if (handler != null) {
            handler.removeCallbacks(stopScanRunnable);
            handler = null;
        }

        if (mBLEScanCallback != null) {
            try {
                BLESdk.getInstance().getBluetoothAdapter().stopLeScan(mBLEScanCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 开始扫描
     */
    private boolean startScanner() {

        boolean scanState = false;
        try {
            scanState = BLESdk.getInstance().getBluetoothAdapter().startLeScan(mBLEScanCallback);
        } catch (Exception e) {
            e.printStackTrace();
            scanState = false;
            if (bleScanListener != null)
                bleScanListener.onScannerError(BLEError.BLE_CLOSE);
        }
        if (!scanState) {
            return scanState;
        }
        if (bleScanListener != null)
            bleScanListener.onScannerStart();

        if (scanTime == INFINITE) {
            return scanState;
        }
        startTimer();
        return scanState;
    }

    /**
     * 开始扫描时长倒计时
     * <p>
     * 开始之前先尝试停止上一次的倒计时
     */
    private void startTimer() {
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(stopScanRunnable, scanTime);
    }

    /**
     * 定时需要执行的任务
     * <p>
     * 停止设备扫描
     */
    private Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            tryToStopScanner();
            //回调扫描结束状态
            if (bleScanListener != null)
                bleScanListener.onScannerStop();

        }
    };

    /**
     * 获取默认的扫描配置
     *
     * @return
     */
    private BLEScanCfg getDefaultScanCfg() {
        BLEScanCfg bleScanCfg = new BLEScanCfg.ScanCfgBuilder().builder();
        return bleScanCfg;
    }
}