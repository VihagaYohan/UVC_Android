package com.techtribeservices.uvc_android

import android.content.Context
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

data class DeviceClass(var deviceName:String = "",
    var deviceId:Any = "",
    var deviceClass: Any = "")

var TAG = "CAM"
private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

class MainActivity : ComponentActivity() {
    val manager = getSystemService(Context.USB_SERVICE) as UsbManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UVC_AndroidTheme {
        CameraComponent()
    }
}