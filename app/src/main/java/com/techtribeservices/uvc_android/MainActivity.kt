package com.techtribeservices.uvc_android

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.techtribeservices.uvc_android.ui.theme.UVC_AndroidTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.techtribeservices.uvc_android.AppReceivers.AirPlanceModeReceiver
import com.techtribeservices.uvc_android.AppReceivers.UsbDetectReceiver

data class DeviceClass(var deviceName:String = "",
    var deviceId:Any = "",
    var deviceClass: Any = "")

var TAG = "CAM"
var deviceName:String = "/dev/bus/usb/001/002"
private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

class MainActivity : ComponentActivity() {
    private val airPlaneModeReceiver = AirPlanceModeReceiver()
    private val usbDetectReceiver = UsbDetectReceiver()
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(airPlaneModeReceiver,
            IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        )

        registerReceiver(usbDetectReceiver,
            IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        )
    if(!hasRequiredPermissions()) {
        ActivityCompat.requestPermissions(this,
            CAMERAX_PERMISSIONS, 0)
    }

        setContent {
            UVC_AndroidTheme {
                // A surface container using the 'background' color from the theme
                /*Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraComponent()
                }*/

                val scaffoldState = rememberBottomSheetScaffoldState()
                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(
                            CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
                        )
                    }
                }
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {}) {paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            CameraPreview(controller = controller, 
                                modifier = Modifier
                                    .fillMaxSize())

                            IconButton(onClick = {
                                controller.cameraSelector =
                                    if(controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    }
                            },
                                modifier = Modifier
                                    .offset(16.dp, 16.dp)) {
                                Icon(imageVector = Icons.Default.Cameraswitch,
                                    contentDescription = "Switch Camera" )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                IconButton(onClick = {}) {
                                    Icon(imageVector = Icons.Default.Photo, contentDescription = "open gallery")
                                }

                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Take photo"
                                    )
                                }
                            }
                        }
                }
            }
        }

        fun takePhoto(controller: LifecycleCameraController,
                              onPhotoTaken:(Bitmap) -> Unit) {
            controller.takePicture(
                ContextCompat.getMainExecutor(applicationContext),
                object: OnImageCapturedCallback()
                {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        onPhotoTaken(image.toBitmap())
                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                        Log.e("Camera", "Couldn't take photo: ", exception)
                    }
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(airPlaneModeReceiver)
        unregisterReceiver(usbDetectReceiver)
    }

    override fun onStart() {
        super.onStart()
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = manager.deviceList
        val device = deviceList[deviceName]
    }

    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
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