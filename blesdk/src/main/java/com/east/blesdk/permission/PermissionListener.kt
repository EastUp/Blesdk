package com.east.blesdk.permission

/**
 * |---------------------------------------------------------------------------------------------------------------|
 *  @description:
 *  @author: East
 *  @date: 2019-12-04
 * |---------------------------------------------------------------------------------------------------------------|
 */
interface PermissionListener {
    fun onGranted()
    fun onCancel()
}