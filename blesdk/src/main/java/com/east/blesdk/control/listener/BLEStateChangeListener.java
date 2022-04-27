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

/**
 *|---------------------------------------------------------------------------------------------------------------|
 *  蓝牙连接状态改变的接口
 *  @date：2018/7/24 16:52
 *  @author East
 *|---------------------------------------------------------------------------------------------------------------|
 */
public interface BLEStateChangeListener {

	void onStateConnected(String address);

	void onStateConnecting(String address);

	void onStateDisConnected(String address);
}
