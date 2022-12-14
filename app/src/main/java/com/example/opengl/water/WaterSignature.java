package com.example.opengl.water;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.Matrix;


import com.example.opengl.ShaderUtil;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class WaterSignature {

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = aTextureCoord.xy;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    public static final int SIZE_OF_FLOAT = 4;
    /**
     * 一个“完整”的正方形，从两维延伸到-1到1。
     * 当 模型/视图/投影矩阵是都为单位矩阵的时候，这将完全覆盖视口。
     * 纹理坐标相对于矩形是y反的。
     * (This seems to work out right with external textures from SurfaceTexture.)
     */
    private static final float[] FULL_RECTANGLE_COORDS = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f,  1.0f,   // 2 top left
            1.0f,  1.0f,   // 3 top right
    };
    private static final float[] FULL_RECTANGLE_TEX_COORDS = {
            0.0f, 1.0f,     //0 bottom left     //0.0f, 0.0f, // 0 bottom left
            1.0f, 1.0f,     //1 bottom right    //1.0f, 0.0f, // 1 bottom right
            0.0f, 0.0f,     //2 top left        //0.0f, 1.0f, // 2 top left
            1.0f, 0.0f,     //3 top right       //1.0f, 1.0f, // 3 top right
    };

    private final FloatBuffer mVertexArray;
    private final FloatBuffer mTexCoordArray;
    private final int mCoordsPerVertex;
    private final int mCoordsPerTexture;
    private final int mVertexCount;
    private final int mVertexStride;
    private final int mTexCoordStride;
    private final int bitmapTexture;
    private int hProgram;

    public float[] mProjectionMatrix = new float[16];// 投影矩阵
    public float[] mViewMatrix = new float[16]; // 摄像机位置朝向9参数矩阵
    public float[] mModelMatrix = new float[16];// 模型变换矩阵
    public float[] mMVPMatrix = new float[16];// 获取具体物体的总变换矩阵
    private float[] getFinalMatrix() {
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    public WaterSignature() {
        mVertexArray = createFloatBuffer(FULL_RECTANGLE_COORDS);
        mTexCoordArray = createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);
        mCoordsPerVertex = 2;
        mCoordsPerTexture = 2;
        mVertexCount = FULL_RECTANGLE_COORDS.length / mCoordsPerVertex; // 4
        mTexCoordStride = 2 * SIZE_OF_FLOAT;
        mVertexStride = 2 * SIZE_OF_FLOAT;

        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        hProgram = ShaderUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        GLES20.glUseProgram(hProgram);

        Bitmap waterText = createTextImage("我是水印test", 50, "#fff000", "#00000000", 0);
        bitmapTexture = loadBitmapTexture(waterText);
    }

    /**
     * 根据文字创建图片
     */
    public static Bitmap createTextImage(String text, int textSize, String textColor, String bgColor, int padding) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor(textColor));
        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        float width = paint.measureText(text,0,text.length());

        float top = paint.getFontMetrics().top;
        float bottom = paint.getFontMetrics().bottom;

        Bitmap bm = Bitmap.createBitmap((int) (width + padding * 2), (int) ((bottom - top) + padding * 2), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);

        canvas.drawColor(Color.parseColor(bgColor));
        canvas.drawText(text, padding, - top + padding, paint);
        return bm;
    }

    public static int loadBitmapTexture(Bitmap bitmap) {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1,textureIds,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureIds[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        ByteBuffer bitmapBuffer = ByteBuffer.allocate(bitmap.getHeight() * bitmap.getWidth() * 4);
        bitmap.copyPixelsToBuffer(bitmapBuffer);
        bitmapBuffer.flip();

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap.getWidth(),
                bitmap.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bitmapBuffer);
        return textureIds[0];
    }

    private FloatBuffer createFloatBuffer(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZE_OF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }

    private WaterSignSProgram mProgram;

    public void setShaderProgram(WaterSignSProgram mProgram) {
        this.mProgram = mProgram;
    }

    public void drawFrame(int mTextureId) {
        GLES20.glUseProgram(hProgram);
        // 设置纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bitmapTexture);
        GLES20.glUniform1i(mProgram.sTextureLoc, 0);
//        ShaderUtil.checkGlError("GL_TEXTURE_2D");
        // 设置 model / view / projection 矩阵
        GLES20.glUniformMatrix4fv(mProgram.uMVPMatrixLoc, 1, false, getFinalMatrix(), 0);
//        ShaderUtil.checkGlError("glUniformMatrix4fv");
        // 使用简单的VAO 设置顶点坐标数据
        GLES20.glEnableVertexAttribArray(mProgram.aPositionLoc);
        GLES20.glVertexAttribPointer(mProgram.aPositionLoc, mCoordsPerVertex,
                GLES20.GL_FLOAT, false, mVertexStride, mVertexArray);
//        ShaderUtil.checkGlError("VAO");
        // 使用简单的VAO 设置纹理坐标数据
        GLES20.glEnableVertexAttribArray(mProgram.aTextureCoordLoc);
        GLES20.glVertexAttribPointer(mProgram.aTextureCoordLoc, mCoordsPerTexture,
                GLES20.GL_FLOAT, false, mTexCoordStride, mTexCoordArray);
//        ShaderUtil.checkGlError("VAO aTextureCoordLoc");
        // GL_TRIANGLE_STRIP三角形带，这就为啥只需要指出4个坐标点，就能画出两个三角形了。
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        // Done -- 解绑~
        GLES20.glDisableVertexAttribArray(mProgram.aPositionLoc);
        GLES20.glDisableVertexAttribArray(mProgram.aTextureCoordLoc);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }


    /**
     * terminatinng, this should be called in GL context
     */
    public void release() {
        if (hProgram >= 0)
            GLES20.glDeleteProgram(hProgram);
        hProgram = -1;
    }

    /**
     * 删除texture
     */
    public static void deleteTex(final int hTex) {
        final int[] tex = new int[] {hTex};
        GLES20.glDeleteTextures(1, tex, 0);
    }



}

