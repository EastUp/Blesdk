package com.east.blesdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.east.blesdk.check.BLECheck;
import com.east.blesdk.control.BLEControl;
import com.east.blesdk.scan.BLEScanner;
import com.east.blesdk.util.BLELog;

/**
 *|---------------------------------------------------------------------------------------------------------------|
 *  蓝牙工具类的总入口
 *  @date：2018/7/19 16:28
 *  @author East 
 *|---------------------------------------------------------------------------------------------------------------|
 */
public class BLESdk {
    //是否允许多个设备设备同时连接，为运行多连
    private boolean permitConnectMore = true;

    //同时连接的最大个数
    private int maxConnect = 3;

    //私有方法，不允许外部使用 new 关键字
    private BLESdk() {
    }

    private static volatile BLESdk instance;

    static {
        instance = null;
    }

    public static BLESdk getInstance() {

        if (instance != null) {
            return instance;
        }
        synchronized (BLESdk.class) {
            if (instance == null) {
                instance = new BLESdk();
            }
        }
        return instance;
    }

    /**
     * <p>
     * /** 初始化与 SDK 相关的一些类
     * <p>
     * 此方法必须调用，最好是直接在 Application 里面调用
     *
     * @param context
     */
    public void init(Context context) {

        AndroidBLE.init(context);              //初始化 BluetoothManager,BluetoothAdapter.
        BLECheck.init();                    //初始化蓝牙检测类
        BLEScanner.init();                    //初始化蓝牙扫描类
        BLEControl.init();                  //	初始化蓝牙控制类

        BLELog.i("BLESdk init ok");
    }


    /**
     * 设置是否允许多连
     *
     * @return
     */
    public boolean isPermitConnectMore() {
        return permitConnectMore;
    }


    /**
     * 设置最大连接数
     *
     * @param maxConnect
     */
    public void setMaxConnect(int maxConnect) {

        if (maxConnect < 1) {
            maxConnect = 1;
        }

        if (maxConnect > 5) {
            maxConnect = 5;
        }
        permitConnectMore = maxConnect != 1;

        this.maxConnect = maxConnect;
    }

    /**
     * 获取最大连接数
     *
     * @return
     */
    public int getMaxConnect() {
        return maxConnect;
    }


    /**
     * 获取 BluetoothManager
     *
     * @return
     */
    public BluetoothManager getBluetoothManager() throws NullPointerException {
        return AndroidBLE.getInstance().getBluetoothManager();
    }

    /**
     * 获取 BluetoothAdapter
     *
     * @return
     */
    public BluetoothAdapter getBluetoothAdapter() throws NullPointerException {
        return AndroidBLE.getInstance().getBluetoothAdapter();
    }


    public void reset() throws NullPointerException {

        AndroidBLE.getInstance().reset();
    }
}

