//package com.example.camera.camera2;
//
//import android.content.Context;
//import android.util.Log;
//
//import org.webrtc.Camera1Enumerator;
//import org.webrtc.Camera2Enumerator;
//import org.webrtc.CameraEnumerator;
//import org.webrtc.Logging;
//import org.webrtc.VideoCapturer;
//
//class test extends Context {
//
//
//    private static final String TAG = "test";
//
//    private VideoCapturer createVideoCapturer() {
//        if (Camera2Enumerator.isSupported(this)) {
//            return createCameraCapturer(new Camera2Enumerator(this));
//        } else {
//            return createCameraCapturer(new Camera1Enumerator(true));
//        }
//
//    }
//
//    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
//        final String[] deviceNames = enumerator.getDeviceNames();
//        // First, try to find front facing camera
//        Log.d(TAG, "Looking for front facing cameras.");
//        for (String deviceName : deviceNames) {
//            if (enumerator.isFrontFacing(deviceName)) {
//                Logging.d(TAG, "Creating front facing camera capturer.");
//                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
//                if (videoCapturer != null) {
//                    return videoCapturer;
//                }
//            }
//        }
//        // Front facing camera not found, try something
//        Log.d(TAG, "Looking for other cameras.");
//        for (String deviceName : deviceNames) {
//            if (!enumerator.isFrontFacing(deviceName)) {
//                Logging.d(TAG, "Creating other camera capturer.");
//                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
//                if (videoCapturer != null) {
//                    return videoCapturer;
//                }
//            }
//        }
//        return null;
//    }
//}