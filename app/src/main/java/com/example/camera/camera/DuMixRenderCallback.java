package com.example.camera.camera;

import android.graphics.SurfaceTexture;

public interface DuMixRenderCallback {
    void onSurfaceCreated(SurfaceTexture cameraTex, SurfaceTexture arTex);

    void onSurfaceChanged(int width, int height);
}