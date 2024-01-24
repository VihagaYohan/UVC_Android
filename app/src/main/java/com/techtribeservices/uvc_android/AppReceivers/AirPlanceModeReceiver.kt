package com.techtribeservices.uvc_android.AppReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

class AirPlanceModeReceiver: BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
            val isTurnedOn = Settings.Global.getInt(
                context?.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON
            ) != 0
            println("Is airplane mode enabled? ${isTurnedOn}")
        }
    }
}