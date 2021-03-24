package com.example.camera.camera2

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Handler
import android.view.Surface
import java.util.ArrayList
import java.util.concurrent.Executors

fun CameraDevice.createCaptureSession2(
    outputs: List<Surface>,
    success: (session: CameraCaptureSession) -> Unit = {},
    failed: (session: CameraCaptureSession) -> Unit = {},
    handler: Handler? = null
) {

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        val arrayList = ArrayList<OutputConfiguration>()
        outputs.forEach {
            arrayList.add(OutputConfiguration(it))
        }
        val sessionConfiguration = SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            arrayList,
            Executors.newSingleThreadExecutor(),
            object :
                CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    success.invoke(session)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    failed.invoke(session)
                }

            })
        createCaptureSession(sessionConfiguration)
    }else{
        createCaptureSession(outputs, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                success.invoke(session)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                failed.invoke(session)
            }

        }, handler)
    }

}