package com.example.camera.camera;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class CameraManager {

    private static final String TAG = "CameraManager";

    private static CameraManager mInstance;

    // 相机默认宽高，相机的宽度和高度跟屏幕坐标不一样，手机屏幕的宽度和高度是反过来的。
    public static final int DEFAULTWIDTH = 1280;
    public static final int DEFAULTHEIGHT = 720;
    // 期望fps
    public static final int DESIREDPREVIEWFPS = 30;

    // 这里反过来是因为相机的分辨率跟屏幕的分辨率宽高刚好反过来
    // 4:3
    public static final float Ratio43 = 0.75f;
    // 1:1
    public static final float Ratio11 = 1.0f;
    // 16:9
    public static final float Ratio169 = 0.5625f;

    private int mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private Camera mCamera;
    private int mWindowRotation = 0;

    // 当前的宽高比
    private float mCurrentRatio = Ratio169;

    private static final int RETRY_OPEN_CAMERA_MAX = 3;
    private static final int RETRY_OPEN_CAMERA_DELAY_MS = 150;

    /**
     * 获取单例
     *
     * @return
     */
    public static CameraManager getInstance() {
        if (mInstance == null) {
            mInstance = new CameraManager();
        }
        return mInstance;
    }

    private CameraManager() {
    }

    /**
     * 根据ID打开相机
     *
     * @param cameraID
     */
    public void openCamera(int cameraID) {
        openCamera(cameraID, DEFAULTWIDTH, DEFAULTHEIGHT, mWindowRotation);
    }

    /**
     * 打开相机
     *
     * @param cameraID
     * @param expectWidth
     * @param expectHeight
     */
    public void openCamera(int cameraID, int expectWidth, int expectHeight, int windowRotation) {
        mCameraID = cameraID;
        if (mCamera != null) {
            Log.e(TAG, "camera already initialized!");
            return;
        }
        if (!cameraOpen(mCameraID) || mCamera == null) {
            Log.e(TAG, "Unable to open camera!");
            return;
        }
        
        try {
            mWindowRotation = windowRotation;
            Camera.Parameters parameters = mCamera.getParameters();
            // 添加自动对焦
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            if (cameraID == Camera.CameraInfo.CAMERA_FACING_BACK || !Build.BRAND.contains("Xiaomi")) {
                parameters.setRecordingHint(true);
            }

            if (Build.MODEL.contains("Lenovo K520")) {
                // 设置防闪烁
                List<String> antibanding = parameters.getSupportedAntibanding();
                if (antibanding != null && antibanding.contains(Camera.Parameters.ANTIBANDING_50HZ)) {
                    parameters.setAntibanding(Camera.Parameters.ANTIBANDING_50HZ);
                }
            }
            mCamera.setParameters(parameters);

            try {
                setPreviewSize(mCamera, expectWidth, expectHeight);
            } catch (Exception e) {
                Log.e(TAG, "setPreviewSize Error");
            }
            chooseFixedPreviewFps(DESIREDPREVIEWFPS * 1000);

            int orientation = calculateCameraPreviewOrientation(windowRotation);
            mCamera.setDisplayOrientation(orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 避免部分手机打开失败，增加3次打开camera
     * @param cameraId
     * @return
     */
    private boolean cameraOpen(int cameraId) {
        for (int i = 0; i < RETRY_OPEN_CAMERA_MAX; i++) {
            try {
                mCamera = Camera.open(cameraId);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "camera open error!");
            }
            try {
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
                Thread.sleep(RETRY_OPEN_CAMERA_DELAY_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 设置camera callback buffer
     *
     * @param data
     */
    public void addCameraCallbackBuffer(byte[] data) {
        if (mCamera != null) {
            mCamera.addCallbackBuffer(data);
        }
    }

    /**
     * 重新打开相机
     */
    public void reopenCamera() {
        releaseCamera();
        openCamera(mCameraID);
    }

    /**
     * 重新打开相机
     *
     * @param expectWidth
     * @param expectHeight
     */
    public void reopenCamera(int expectWidth, int expectHeight) {
        releaseCamera();
        openCamera(mCameraID, expectWidth, expectHeight, mWindowRotation);
    }

    /**
     * 设置预览的Surface
     *
     * @param texture
     */
    public void setPreviewSurface(SurfaceTexture texture) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(texture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置预览的Surface
     *
     * @param holder
     */
    public void setPreviewSurface(SurfaceHolder holder) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加预览回调 备注：预览回调需要在setPreviewSurface之后调用
     *
     * @param callback
     */
    public void setPreviewCallback(Camera.PreviewCallback callback) {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(callback);
        }
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        if (mCamera != null) {
            Camera.Size previewSize = null;
            try {
                previewSize = mCamera.getParameters().getPreviewSize();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (previewSize != null) {
                int previewBufferSize =
                        ((previewSize.width * previewSize.height) * ImageFormat
                                .getBitsPerPixel(ImageFormat.NV21)) / 8;
                // 添加3个buffer，保障buffer足够使用，且不影响效率
                for (int i = 0; i < 3; i++) {
                    mCamera.addCallbackBuffer(new byte[previewBufferSize]);
                }
            }
            try {
                mCamera.startPreview();
            } catch (Exception e) {
                Log.e("startPreview camera", "Exception");
            }
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    /**
     * 切换相机
     *
     * @param cameraId     相机Id
     * @param expectWidth  期望宽度
     * @param expectHeight 期望高度
     */
    public void switchCamera(int cameraId, int expectWidth, int expectHeight) {
        if (mCameraID == cameraId) {
            return;
        }
        mCameraID = cameraId;
        releaseCamera();
        openCamera(cameraId, expectWidth, expectHeight, mWindowRotation);
    }

    /**
     * 释放相机
     */
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 拍照
     */
    public void takePicture(Camera.ShutterCallback shutterCallback,
                            Camera.PictureCallback rawCallback,
                            Camera.PictureCallback pictureCallback) {
        if (mCamera != null) {
            mCamera.takePicture(shutterCallback, rawCallback, pictureCallback);
        }
    }

    /**
     * 设置预览大小
     *
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    private void setPreviewSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPreviewSizes(),
                expectWidth, expectHeight);
        parameters.setPreviewSize(size.width, size.height);
        camera.setParameters(parameters);
        Log.d("setPreviewSize", "width = " + size.width + ", height = " + size.height);
    }

    /**
     * 设置拍摄的照片大小
     *
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    private void setPictureSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPictureSizes(),
                expectWidth, expectHeight);
        parameters.setPictureSize(size.width, size.height);
        camera.setParameters(parameters);
        Log.d("setPictureSize", "width = " + size.width + ", height = " + size.height);
    }

    /**
     * 设置预览角度，setDisplayOrientation本身只能改变预览的角度
     * previewFrameCallback以及拍摄出来的照片是不会发生改变的，拍摄出来的照片角度依旧不正常的
     * 拍摄的照片需要自行处理
     * 这里Nexus5X的相机简直没法吐槽，后置摄像头倒置了，切换摄像头之后就出现问题了。
     *
     * @param windowRotation
     */
    private int calculateCameraPreviewOrientation(int windowRotation) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, info);
        int degrees = 0;
        switch (windowRotation) {
            case Surface.ROTATION_0:
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                degrees = 0;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    // -------------------------------- setter and getter start
    // -------------------------------------

    /**
     * 获取当前的Camera ID
     *
     * @return
     */
    public int getCameraID() {
        return mCameraID;
    }

    /**
     * 获取相机信息
     *
     * @return
     */
    public Camera.CameraInfo getCameraInfo() {
        if (mCamera != null) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraID, info);
            return info;
        }
        return null;
    }

    /**
     * 获取当前预览的角度
     *
     * @return
     */
    public int getPreviewOrientation() {
        return mWindowRotation;
    }

    /**
     * 获取当前的宽高比
     *
     * @return
     */
    public float getCurrentRatio() {
        return mCurrentRatio;
    }

    /**
     * 计算最完美的Size
     *
     * @param sizes
     * @param expectWidth
     * @param expectHeight
     *
     * @return
     */
    private Camera.Size calculatePerfectSize(List<Camera.Size> sizes, int expectWidth,
                                             int expectHeight) {
        sortList(sizes); // 根据宽度进行排序

        // 根据当前期望的宽高判定
        List<Camera.Size> bigEnough = new ArrayList<>();
        List<Camera.Size> noBigEnough = new ArrayList<>();
        for (Camera.Size size : sizes) {
            if (size.height * expectWidth / expectHeight == size.width) {
                if (size.width >= expectWidth && size.height >= expectHeight) {
                    bigEnough.add(size);
                } else {
                    noBigEnough.add(size);
                }
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareAreaSize());
        } else if (noBigEnough.size() > 0) {
            return Collections.max(noBigEnough, new CompareAreaSize());
        } else { // 如果不存在满足要求的数值，则辗转计算宽高最接近的值
            Camera.Size result = sizes.get(0);
            boolean widthOrHeight = false; // 判断存在宽或高相等的Size
            // 辗转计算宽高最接近的值
            for (Camera.Size size : sizes) {
                // 如果宽高相等，则直接返回
                if (size.width == expectWidth && size.height == expectHeight
                        && ((float) size.height / (float) size.width) == mCurrentRatio) {
                    result = size;
                    break;
                }
                // 仅仅是宽度相等，计算高度最接近的size
                if (size.width == expectWidth) {
                    widthOrHeight = true;
                    if (Math.abs(result.height - expectHeight)
                            > Math.abs(size.height - expectHeight)
                            && ((float) size.height / (float) size.width) == mCurrentRatio) {
                        result = size;
                        break;
                    }
                }
                // 高度相等，则计算宽度最接近的Size
                else if (size.height == expectHeight) {
                    widthOrHeight = true;
                    if (Math.abs(result.width - expectWidth)
                            > Math.abs(size.width - expectWidth)
                            && ((float) size.height / (float) size.width) == mCurrentRatio) {
                        result = size;
                        break;
                    }
                }
                // 如果之前的查找不存在宽或高相等的情况，则计算宽度和高度都最接近的期望值的Size
                else if (!widthOrHeight) {
                    if (Math.abs(result.width - expectWidth)
                            > Math.abs(size.width - expectWidth)
                            && Math.abs(result.height - expectHeight)
                            > Math.abs(size.height - expectHeight)
                            && ((float) size.height / (float) size.width) == mCurrentRatio) {
                        result = size;
                    }
                }
            }
            return result;
        }
    }

    /**
     * 分辨率由大到小排序
     *
     * @param list
     */
    private void sortList(List<Camera.Size> list) {
        Collections.sort(list, new CompareAreaSize());
    }

    /**
     * 比较器
     */
    private class CompareAreaSize implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size pre, Camera.Size after) {
            return Long.signum((long) pre.width * pre.height - (long) after.width * after.height);
        }
    }

    /**
     * 设置camera 闪光灯状态
     *
     * @param flashMode
     */
    public boolean setCameraFlashMode(String flashMode) {
        try {
            if (mCamera != null) {
                Camera.Parameters mParameters = mCamera.getParameters();
                mParameters.setFlashMode(flashMode);
                mCamera.setParameters(mParameters);
                return true;
            }
        } catch (Exception ex) {
            Log.e("setCameraFlashMode ", ex.getLocalizedMessage());
            return false;
        }
        return false;
    }

    public void setOnCameraError(final Camera.ErrorCallback callback) {
        if (mCamera != null) {
            mCamera.setErrorCallback(callback);
        }
    }

    /**
     * Attempts to find a fixed preview frame rate that matches the desired frame rate.
     * <p>
     * It doesn't seem like there's a great deal of flexibility here.
     * <p>
     * TODO: follow the recipe from http://stackoverflow.com/questions/22639336/#22645327
     *
     * @return The expected frame rate, in thousands of frames per second.
     */
    public boolean chooseFixedPreviewFps(int desiredThousandFps) {
        try {
            if (mCamera != null) {
                Camera.Parameters mParameters = mCamera.getParameters();
                List<int[]> supported = mParameters.getSupportedPreviewFpsRange();

                int[] fastFps = new int[2];
                for (int[] entry : supported) {
                    Log.d(TAG, "entry: " + entry[0] + " - " + entry[1]);
                    if ((entry[0] == entry[1]) && (entry[0] == desiredThousandFps)) {
                        mParameters.setPreviewFpsRange(entry[0], entry[1]);
                        return true;
                    } else if (entry[0] >= fastFps[0] && entry[1] >= fastFps[1]) {
                        fastFps[0] = entry[0];
                        fastFps[1] = entry[1];
                    }
                }
                Log.d(TAG, "setting fps: " + fastFps[0] + " - " + fastFps[1]);
                mParameters.setPreviewFpsRange(fastFps[0], fastFps[1]);
                mCamera.setParameters(mParameters);
                return false;
            }
        } catch (Exception ex) {
            Log.e("chooseFixedPreviewFps ", ex.getLocalizedMessage());
            return false;
        }
        return false;
    }

}