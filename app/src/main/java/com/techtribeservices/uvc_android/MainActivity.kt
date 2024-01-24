package com.techtribeservices.uvc_android

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.techtribeservices.uvc_android.ui.theme.UVC_AndroidTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat.getSystemService
import com.techtribeservices.uvc_android.AppReceivers.AirPlanceModeReceiver

data class DeviceClass(var deviceName:String = "",
    var deviceId:Any = "",
    var deviceClass: Any = "")

var TAG = "CAM"
var deviceName:String = "/dev/bus/usb/001/002"
private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

class MainActivity : ComponentActivity() {
    private val airPlaneModeReceiver = AirPlanceModeReceiver()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(airPlaneModeReceiver,
            IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        )

        setContent {
            UVC_AndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraComponent()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(airPlaneModeReceiver)
    }

    override fun onStart() {
        super.onStart()
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = manager.deviceList
        val device = deviceList[deviceName]
    }
}

@Composable
fun CameraComponent(modifier: Modifier = Modifier) {
    var isDetected by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if(isDetected) "Camera detected" else "Camera POC",
            modifier = modifier)

        Button(onClick = {
            print("button on click here")
            isDetected = !isDetected
        }) {
            Text(text = "Click Me")
        }
    }
}

fun detectDevices() {

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UVC_AndroidTheme {
        CameraComponent()
    }
}