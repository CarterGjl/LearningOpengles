package com.example.camera.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraGlSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {


    private static int textureId = -1;
    private static final String TAG = "CameraGlSurfaceView";
    private SurfaceTexture mSurface;
    private DirectDrawer mARDrawer;
    private DuMixRenderCallback mRendererCallback;
    private String name;
    private int textureID;


    public CameraGlSurfaceView(Context context, DuMixRenderCallback duMixRenderCallback, String name) {
        super(context);
        mRendererCallback = duMixRenderCallback;
        setEGLContextClientVersion(0x3098);
        setEGLContextFactory(new MyEGLContextFactory());
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        this.name = name;
    }

    public void setTextureID(int textureID) {
        this.textureID = textureID;
    }

    public void setSurface(SurfaceTexture mSurface) {
        this.mSurface = mSurface;
    }

    public CameraGlSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

//        textureID = createTextureID();
//        mSurface = new SurfaceTexture(textureID);
//        mSurface.setOnFrameAvailableListener(this);
        mARDrawer = new DirectDrawer(textureID);
//        if (mRendererCallback != null) {
//            mRendererCallback.onSurfaceCreated(mSurface, null);
//        }
    }

    private int createTextureID() {
        int[] tex = new int[1];
        //生成一个纹理
        GLES20.glGenTextures(1, tex, 0);
        //将此纹理绑定到外部纹理上
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        //设置纹理过滤参数
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        //解除纹理绑定
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG, "onDrawFrame: " + name);
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        float[] mSTMatrix = new float[16];
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        if (mSurface != null) {
            mSurface.updateTexImage();

            mSurface.getTransformMatrix(mSTMatrix);
        }


        // Draw the video frame.

        mARDrawer.draw(mSTMatrix);
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }
}
