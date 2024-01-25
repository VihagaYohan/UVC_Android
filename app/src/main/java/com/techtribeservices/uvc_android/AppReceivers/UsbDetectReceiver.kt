package com.techtribeservices.uvc_android.AppReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class UsbDetectReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            if (device != null) {
                println("isDevice - ${device.deviceName} ${device.deviceId}")
            }else {
                println("no device - ${device}")
            }
        }
    }
}