package com.example.camera.camera;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;


public class MyEGLContextFactory implements GLSurfaceView.EGLContextFactory {
    /**
     * 共享的opengl上下文
     */
    private static EGLContext share_context = null;

    //创建第一个GLSurfaceView的时候，传null,后面的传
    public MyEGLContextFactory() {
    }

    private int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    @Override
    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};

        if (MyEGLContextFactory.share_context == null) {
            EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
            MyEGLContextFactory.share_context = context;
            return context;
        } else {
            return egl.eglCreateContext(display, eglConfig,  MyEGLContextFactory.share_context, attrib_list);
        }
    }


    @Override
    public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
        egl.eglDestroyContext(display, context);
    }
}