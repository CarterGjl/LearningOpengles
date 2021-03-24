package com.example.camera.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * Camera线程控制
 */
public class CameraHandlerThread extends HandlerThread {

    private static final String TAG = "CameraHandlerThread";

    private final Handler mHandler;

    public CameraHandlerThread() {
        super(TAG);
        start();
        mHandler = new Handler(getLooper());
    }

    public CameraHandlerThread(String name) {
        super(name);
        start();
        mHandler = new Handler(getLooper());
    }

    /**
     * 销毁线程
     */
    public void destoryThread() {
        mHandler.removeCallbacksAndMessages(null);
        quitSafely();
    }

    /**
     * 检查handler是否可用
     */
    private void checkHandleAvailable() {
        if (mHandler == null) {
            throw new NullPointerException("Handler is not available!");
        }
    }

    /**
     * 等待操作完成
     */
    private void waitUntilReady() {
        try {
            wait();
        } catch (InterruptedException e) {
            Log.w(TAG, "wait was interrupted");
        }
    }

    /**
     * 打开相机
     *
     * @param cameraId     相机Id
     * @param expectWidth  期望宽度
     * @param expectHeight 期望高度
     */
    public synchronized void openCamera(final int cameraId, final int expectWidth, final int expectHeight,
                                        final int windowRotation) {
        checkHandleAvailable();
        mHandler.post(() -> {
            internalOpenCamera(cameraId, expectWidth, expectHeight, windowRotation);
            notifyCameraOpened();
        });
        waitUntilReady();
    }

    /**
     * 通知相机已打开，主要的作用是，如果在打开之后要立即获得mCamera实例，则需要添加wait()-notify()
     * wait() - notify() 不是必须的
     */
    private synchronized void notifyCameraOpened() {
        notify();
    }

    /**
     * 重新打开相机
     *
     * @param expectWidth  期望宽度
     * @param expectHeight 期望高度
     */
    public synchronized void reopenCamera(final int expectWidth, final int expectHeight) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalReopenCamera(expectWidth, expectHeight);
                notifyCameraOpened();
            }
        });
        waitUntilReady();
    }

    /**
     * 设置预览Surface
     *
     * @param holder SurfaceHolder
     */
    public void setPreviewSurface(final SurfaceHolder holder) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalPreviewSurface(holder);
            }
        });
    }

    /**
     * 设置预览Surface
     *
     * @param texture SurfaceTexture
     */
    public void setPreviewSurface(final SurfaceTexture texture) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalPreviewSurface(texture);
            }
        });
    }

    /**
     * 设置预览回调
     *
     * @param callback 回调
     */
    public void setPreviewCallback(final Camera.PreviewCallback callback) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalPreviewCallback(callback);
            }
        });
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalStartPreview();
            }
        });
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalStopPreview();
            }
        });
    }

    /**
     * 切换相机
     *
     * @param cameraId     相机Id
     * @param expectWidth  期望宽度
     * @param expectHeight 期望高度
     */
    public void switchCamera(final int cameraId, final int expectWidth, final int expectHeight) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalSwitchCamera(cameraId, expectWidth, expectHeight);
            }
        });
    }

    /**
     * 释放相机
     */
    public synchronized void releaseCamera() {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalReleaseCamera();
                notifyCameraReleased();
            }
        });
        waitUntilReady();
    }

    /**
     * 通知销毁成功
     */
    private synchronized void notifyCameraReleased() {
        notify();
    }

    /**
     * 通知计算预览角度完成
     */
    private synchronized void notifyPreviewOrientationCalculated() {
        notify();
    }

    // ------------------------------- 内部方法 -----------------------------

    /**
     * 打开相机
     *
     * @param cameraId     相机帧率
     * @param expectWidth  期望宽度
     * @param expectHeight 期望高度
     */
    private void internalOpenCamera(int cameraId, int expectWidth, int expectHeight, int windowRotation) {
        CameraManager.getInstance().openCamera(cameraId, expectWidth, expectHeight, windowRotation);
    }

    /**
     * 重新打开相机
     */
    private void internalReopenCamera() {
        CameraManager.getInstance().reopenCamera();
    }

    /**
     * 重新打开相机
     *
     * @param expectWidth  期望宽度
     * @param expectHeight 期望高度
     */
    public void internalReopenCamera(int expectWidth, int expectHeight) {
        CameraManager.getInstance().reopenCamera(expectWidth, expectHeight);
    }

    /**
     * 预览Surface
     *
     * @param holder
     */
    private void internalPreviewSurface(SurfaceHolder holder) {
        CameraManager.getInstance().setPreviewSurface(holder);
    }

    /**
     * 预览Surface
     *
     * @param texture
     */
    private void internalPreviewSurface(SurfaceTexture texture) {
        CameraManager.getInstance().setPreviewSurface(texture);
    }

    /**
     * 设置预览回调
     *
     * @param callback 预览回调
     */
    private void internalPreviewCallback(Camera.PreviewCallback callback) {
        CameraManager.getInstance().setPreviewCallback(callback);
    }

    /**
     * 开始预览
     */
    private void internalStartPreview() {
        CameraManager.getInstance().startPreview();
    }

    /**
     * 停止预览
     */
    private void internalStopPreview() {
        CameraManager.getInstance().stopPreview();
    }

    /**
     * 切换相机
     *
     * @param cameraId     相机Id
     * @param expectWidth  期望宽度
     * @param expectHeight 期望高度
     */
    private void internalSwitchCamera(int cameraId, int expectWidth, int expectHeight) {
        CameraManager.getInstance().switchCamera(cameraId, expectWidth, expectHeight);
    }

    /**
     * 释放相机
     */
    private void internalReleaseCamera() {
        CameraManager.getInstance().releaseCamera();
    }

    // ------------------------------------- setter and getter -------------------------------------

    /**
     * 获取回调
     *
     * @return
     */
    public Handler getHandler() {
        return mHandler;
    }

    /**
     * 获取相机Id
     *
     * @return
     */
    public int getCameraId() {
        return CameraManager.getInstance().getCameraID();
    }

    /**
     * 打开camera 闪光灯状态
     *
     * @param flashMode
     *
     * @return
     */
    public boolean setCameraFlashMode(String flashMode) {
        return CameraManager.getInstance().setCameraFlashMode(flashMode);
    }

    /**
     * camera 运行异常callback
     *
     * @param callback
     */
    public void setOnCameraError(final Camera.ErrorCallback callback) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CameraManager.getInstance().setOnCameraError(callback);
            }
        });
    }

    public void addCameraCallbackBuffer(final byte[] data) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CameraManager.getInstance().addCameraCallbackBuffer(data);
            }
        });
    }
}
