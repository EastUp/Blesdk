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

import android.os.ParcelUuid;

import java.util.UUID;

/**
 *|---------------------------------------------------------------------------------------------------------------|
 *  扫描的参数类（扫描时长，名称过滤，uuid过滤，mac地址过滤）
 *  @date：2018/7/20 9:58
 *  @author East
 *|---------------------------------------------------------------------------------------------------------------|
 */
public class BLEScanCfg {
    /**
     * 扫描时长
     */
    private int scanTime = 5000; // 默认扫描5秒
    private String[] nameFilter = null;
    private UUID[] uuidFilter = null;
    private String[] addressFilter = null;
    BLEScanCfg() {
        //私有方法，不允许外包调用
    }

    public int getScanTime() {
        return scanTime;
    }

    public void setScanTime(int scanTime) {
        this.scanTime = scanTime;
    }

    public String[] getNameFilter() {
        return nameFilter;
    }

    public void setNameFilter(String[] nameFilter) {
        this.nameFilter = nameFilter;
    }

    public String[] getAddressFilter() {
        return addressFilter;
    }

    public void setAddressFilter(String[] addressFilter) {
        this.addressFilter = addressFilter;
    }

    public UUID[] getUuidFilter() {
        return uuidFilter;
    }

    public void setUuidFilter(UUID[] uuidFilter) {
        this.uuidFilter = uuidFilter;
    }

    public ParcelUuid[] getParcelUuidFilters() {
        if (uuidFilter == null || uuidFilter.length == 0) {
            return null;
        }
        int length = uuidFilter.length;
        ParcelUuid[] filters = new ParcelUuid[length];
        for (int i = 0; i < length; i++) {
            filters[i] = new ParcelUuid(uuidFilter[i]);
        }
        return filters;
    }

    /**
     * BLEScanCfg 的 构造器
     */
    public static class ScanCfgBuilder {
        private BLEScanCfg mBLEScanCfg = new BLEScanCfg();

        public ScanCfgBuilder() {

        }

        public ScanCfgBuilder(int scanTime) {
            mBLEScanCfg.setScanTime(scanTime);
        }

        public ScanCfgBuilder setScanTime(int scanTime) {
            mBLEScanCfg.setScanTime(scanTime);
            return this;
        }

        public ScanCfgBuilder addNameFilter(String... names) {

            if (names == null || names.length == 0) {
                return this;
            }
            String[] nameFilter = names.clone();
            mBLEScanCfg.setNameFilter(nameFilter);

            return this;
        }

        public ScanCfgBuilder addUUIDFilter(UUID... uuids) {
            if (uuids == null || uuids.length == 0) {
                return this;
            }
            UUID[] uuidFilter = uuids.clone();
            mBLEScanCfg.setUuidFilter(uuidFilter);
            return this;
        }

        public ScanCfgBuilder addAddressFilter(String... addresses){
            if(addresses == null || addresses.length == 0){
                return this;
            }
            String[] addressFilter = addresses.clone();
            mBLEScanCfg.setAddressFilter(addressFilter);
            return this;
        }

        public BLEScanCfg builder() {
            return mBLEScanCfg;
        }

    }
}
