package com.techtribeservices.uvc_android

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.techtribeservices.uvc_android.ui.theme.UVC_AndroidTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import com.techtribeservices.uvc_android.AppReceivers.AirPlanceModeReceiver
import com.techtribeservices.uvc_android.AppReceivers.UsbDetectReceiver
import kotlinx.coroutines.awaitAll

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
            IntentFilter(ACTION_USB_PERMISSION)
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
                CameraComponent()

                /* BottomSheetScaffold(
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


                            }
                        }
                }*/
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStart() {
        super.onStart()
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val permissionIntent = PendingIntent.getBroadcast(this,0,
            Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE)
        val deviceList = manager.deviceList
        val device = deviceList[deviceName]

        Log.d(TAG, "camera name${device?.deviceName}")
       /* var result = manager.openDevice(device)
        Log.d(TAG, result.serial)*/

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraList = cameraManager.cameraIdList

        for(cameraId in cameraList) {
            val characteristic = cameraManager.getCameraCharacteristics(cameraId)
            val facing = characteristic.get(CameraCharacteristics.LENS_FACING)
            val capabilities = characteristic.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
        }

        var bytes: ByteArray
        val TIEMOUT = 0
        val forceClaim = true

        if(device != null) {
            val permissionResponse = manager.requestPermission(device,permissionIntent)
            if(permissionResponse != null) {
                print("permission result: ${permissionIntent.toString()}")
                Log.d(TAG,"cam found")
                var bytes: ByteArray
                val TIMEOUT = 0
                val forceClaim = true

                print("interface " + device.getInterface(0).toString())
                print("endpoint " + device.getInterface(0).getEndpoint(0))

                val hasPermission = manager.hasPermission(device)
                Log.d(TAG, "permission: ${hasPermission}")

                if(hasPermission == false) {
                } else {
                    Log.d(TAG, "granted: ${hasPermission}")
                    val result  = manager.openDevice(device)
                    if(result !== null) {
                        print("claim : ${result.serial}")
                    } else {
                        print("open device null")
                    }
                }

                /*
                val openState = manager.openDevice(device)


                if(openState != null) {
                    print("device opened")
                } else {
                    print("device open failed")
                }*/
            } else {
                print("permission result : ${null}")
            }



            /*device?.getInterface(0)?.also{intf ->
                intf.getEndpoint(0)?.also{endpoint ->
                    manager.openDevice(device)?.apply {
                        claimInterface(intf, forceClaim)
                        bulkTransfer(endpoint, bytes, bytes.size, TIMEOUT)
                    }
                }
            }*/
        } else {
            Log.d(TAG, "Device not connected")
        }
    }

    suspend fun usbPermission(device: UsbDevice,
                              manager: UsbManager,
                              pendingIntent: PendingIntent) {
        manager.requestPermission(device, pendingIntent)
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

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@Composable
fun CameraComponent(modifier: Modifier = Modifier) {
    var isDetected by remember {
        mutableStateOf(false)
    }
    val cameraListState = remember {
        mutableStateOf(emptyList<com.techtribeservices.uvc_android.data.CameraInfo>())
    }

    val context = LocalContext.current
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    LaunchedEffect(Unit) {
        updatedCameraList(cameraManager, cameraListState, context)
    }

    CameraListContent(cameraList = cameraListState.value)

/*    Column(
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
    }*/
}

@Composable
fun CameraListContent(cameraList: List<com.techtribeservices.uvc_android.data.CameraInfo>) {
    Column {
        Text("Available cameras")
        Spacer(modifier = Modifier.height(8.dp))
        cameraList.forEach{camItem ->
            CameraListItem(camera = camItem)
        }
    }
}

@Composable
fun CameraListItem(camera:com.techtribeservices.uvc_android.data.CameraInfo) {
    Text(
        text = "${camera.name} (ID: ${camera.id}) - ${if(camera.isOpen) "Open" else "Closed"}",
        modifier = Modifier
            .padding(8.dp)
            .clickable { print("${camera.name} - ${camera.id}") }
    )
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun updatedCameraList(cameraManager: CameraManager,
                      cameraListState: MutableState<List<com.techtribeservices.uvc_android.data.CameraInfo>>,
                      context: Context) {
    val idList = cameraManager.cameraIdList
    val newList = mutableListOf<com.techtribeservices.uvc_android.data.CameraInfo>()

    for(id in idList) {
        val characteristics = cameraManager.getCameraCharacteristics(id)
        val name = getCameraName(characteristics)
        val isOpen = isOpenCamera(id, context )

        newList.add(com.techtribeservices.uvc_android.data.CameraInfo(id, name, isOpen))
    }
    cameraListState.value = newList
}

fun isOpenCamera(id: String, context: Context): Boolean {
    return false
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun getCameraName(characteristics: CameraCharacteristics): String {
    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
    return when(facing) {
        CameraMetadata.LENS_FACING_BACK -> "Rear Camera"
        CameraMetadata.LENS_FACING_FRONT -> "Font Camera"
        // CameraMetadata.LENS_FACING_EXTERNAL -> "External Camera"
        else -> "Unknown Camera"
    }
}


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UVC_AndroidTheme {
        CameraComponent(modifier = Modifier)
    }
}