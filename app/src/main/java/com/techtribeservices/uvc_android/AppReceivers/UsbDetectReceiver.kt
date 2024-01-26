package com.techtribeservices.uvc_android.AppReceivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.core.content.ContextCompat.getSystemService

class UsbDetectReceiver: BroadcastReceiver() {
    private val ACTION_USB_Permission = "com.android.example.USB_PERMISSION"
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == ACTION_USB_Permission) {
            val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            if (device != null) {
                println("isDevice - ${device.deviceName} ${device.deviceId}")
            }else {
                println("no device - ${device}")
            }
        }
    }
}