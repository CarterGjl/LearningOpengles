@file:Suppress("unused")

package com.example.opengl

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import com.example.myapplication.ChatApplication
import com.example.myapplication.R
import com.example.opengl.water.WaterSignSProgram
import com.example.opengl.water.WaterSignature
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer


private const val TAG = "VideoGlRender"

class VideoOpenGlRender : OpenglRender() {

    private var matrixLoc: Int = 0
    private var textureLoc: Int = 0
    private var mTextureMatrixLoc: Int = 0
    var mTextureId: Int = -1
    var mMatrix = FloatArray(16)
    private var loadWaterTexture: Int = -1
    private lateinit var waterSignature: WaterSignature

    override fun onCreated() {
        initPos()
        waterSignature = WaterSignature()
        waterSignature.setShaderProgram(WaterSignSProgram())
        loadWaterTexture = loadTexture(ChatApplication.context, R.drawable.test)
    }

    var mTextureMatrix = FloatArray(16)
    private var screenWidth = 0
    private var screenHeight = 0
    private var cameraWidth = 720
    private var cameraHeight = 1280

    override fun onDrawFrame(output: GLSurface) {
        screenWidth = output.viewport.width
        screenHeight = output.viewport.height
        computeTextureMatrix()
        Matrix.setIdentityM(mMatrix, 0)
        Matrix.rotateM(mMatrix,0,180F,0F,1F,0F)
        Matrix.rotateM(mMatrix,0,90F,0F,0F,1F)
        draw()
    }

    private fun computeTextureMatrix() {
        val cameraRatio = cameraWidth / cameraHeight.toFloat()
        val screenRatio = screenWidth / screenHeight.toFloat()
        Matrix.setIdentityM(mTextureMatrix, 0)
        if (cameraRatio > screenRatio) {
            Matrix.scaleM(mTextureMatrix, 0, 1F, 1 - ((cameraRatio - screenRatio) / 2), 1F)
        } else if (cameraRatio < screenRatio) {
            Matrix.scaleM(mTextureMatrix, 0, 1 - ((screenRatio - cameraRatio) / 2),1F , 1F)
        }
    }

    override fun onUpdate() {
    }

    private fun initPos() {
        // ???????????????shader???????????????????????????????????????????????????
        // initialize vertex byte buffer for shape coordinates
        getVertices()
        // initialize byte buffer for the draw list short ??????2??????
        getFragmentVertices()
        // ??????????????????????????????
        initDrawOrder()
        mProgram = createProgram()
        // get handle to vertex shader's vPosition member
        // ?????????????????????????????????id(????????????????????????????????????????????????????????????)
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate")
        textureLoc = GLES20.glGetUniformLocation(mProgram, "s_Texture")
        matrixLoc = GLES20.glGetUniformLocation(mProgram, "mMatrix")
        mTextureMatrixLoc = GLES20.glGetUniformLocation(mProgram, "mTextureMatrix")

    }

    private fun initDrawOrder() {
        val dlb = ByteBuffer.allocateDirect(drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer?.put(drawOrder)
        drawListBuffer?.position(0)
    }

    private fun getFragmentVertices() {
        val bb2 = ByteBuffer.allocateDirect(texBuffer.size * 4)
        bb2.order(ByteOrder.nativeOrder())
        textureVerticesBuffer = bb2.asFloatBuffer()
        textureVerticesBuffer?.put(texBuffer)
        textureVerticesBuffer?.position(0)
    }

    /**
     * ?????????????????????
     * ?????????????????????????????????????????????????????????????????????????????????????????????ByteBuffer
     * ???????????????????????????ByteOrder??????nativeOrder()??????????????????????????????
     *
     * @return ??????Buffer
     */
    private fun getVertices() {
        // ??????????????????????????????
        // squareCoords.length*4???????????????float???????????????
        val bb = ByteBuffer.allocateDirect(squareCoords.size * 4)
        // ??????????????????
        bb.order(ByteOrder.nativeOrder())
        // ?????????Float?????????
        vertexBuffer = bb.asFloatBuffer()
        // ???????????????????????????????????????
        vertexBuffer?.put(squareCoords)
        // ???????????????????????????
        vertexBuffer?.position(0)
    }

    /***
     * ?????????????????????
     */
    private fun createProgram(): Int {
        // ??????????????????
        // ???????????????????????????????????????????????????
        // ?????????????????????
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        // ?????????????????????
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // ????????????
        var createProgram = GLES20.glCreateProgram() // create empty OpenGL ES Program
        // ???????????????????????????????????????????????????????????????????????????
        // ?????????????????????????????????
        GLES20.glAttachShader(createProgram, vertexShader) // add the vertex shader to program
        // ?????????????????????????????????
        GLES20.glAttachShader(createProgram, fragmentShader) // add the fragment shader to program
        // ????????????
        GLES20.glLinkProgram(createProgram) // creates OpenGL ES program executables
        // ??????????????????program???????????????
        val linkStatus = IntArray(1)
        // ??????program???????????????
        GLES20.glGetProgramiv(createProgram, GLES20.GL_LINK_STATUS, linkStatus, 0)
        // ???????????????????????????????????????
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e("ES20_ERROR", "Could not link program: ")
            Log.e("ES20_ERROR", GLES20.glGetProgramInfoLog(createProgram))
            GLES20.glDeleteProgram(createProgram)
            createProgram = 0
        }
        Log.d(TAG, "initPos: ${linkStatus[0]}")
        return createProgram
    }

    override fun onDestroy() {

    }


    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "attribute vec4 inputTextureCoordinate;" +
            "varying vec4 textureCoordinate;" +
            "uniform mat4 mMatrix;" +
            "uniform mat4 mTextureMatrix;"+
            "void main()" +
            "{" +
            "gl_Position = mMatrix * vPosition;" +
            "textureCoordinate = mTextureMatrix * inputTextureCoordinate;" +
            "}"

    private val fragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec4 textureCoordinate;
        uniform samplerExternalOES s_texture;
        void main() {  
           gl_FragColor = texture2D( s_texture, textureCoordinate.xy );
        }
        """.trimIndent()

    private var vertexBuffer: FloatBuffer? = null
    private var textureVerticesBuffer: FloatBuffer? = null
    private var drawListBuffer: ShortBuffer? = null
    private var mProgram = 0
    private var mPositionHandle = 0
    private var mTextureCoordHandle = 0

    // OpenGL ES??????????????????????????????????????????????????????????????????
    // ??????????????????4????????????2???????????????????????????V1,V2,V3?????????V1,V3,V4??????
    // ????????????????????????????????????????????????
    private val drawOrder = shortArrayOf(
        // first Triangle
        0, 1, 2,
        // second Triangle
        0, 2, 3
    ) // order to draw vertices


    // number of coordinates per vertex in this array
    private val COORDS_PER_VERTEX = 2

    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex


    private var squareCoords = floatArrayOf(
        -1.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, -1.0f,
        1.0f, 1.0f
    )

    // ????????????????????????????????????
    private val TEXTURE_FRONT = floatArrayOf(
        1.0f, 1.0f,
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f
    )

    // ????????????????????????????????????
    private val textureVertices = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 0.0f
    )
    var texBuffer = floatArrayOf(
    0.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f,
    1.0f, 0.0f
    )


    private fun draw() {

        GLES20.glEnable(GLES20.GL_BLEND)

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        // ????????????????????????
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // ????????????ID???????????????
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)


        // ????????????shader??????
        GLES20.glUseProgram(mProgram)
        vertexBuffer?.position(0)
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // Prepare the <insert shape here> coordinate data
        // ?????????????????????????????????(vPosition)
        // GLES20.glVertexAttribPointer(????????????,???????????????,????????????,?????????,??????????????????,??????Buffer)
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        GLES20.glEnableVertexAttribArray(mTextureCoordHandle)


        textureVerticesBuffer?.position(0)
        GLES20.glVertexAttribPointer(
            mTextureCoordHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            textureVerticesBuffer
        )
        GLES20.glUniform1i(textureLoc, 0)
        GLES20.glUniformMatrix4fv(matrixLoc, 1, false, mMatrix, 0)
        GLES20.glUniformMatrix4fv(mTextureMatrixLoc, 1, false, mTextureMatrix, 0)

        // ??????
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            drawOrder.size,
            GLES20.GL_UNSIGNED_SHORT,
            drawListBuffer
        )

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle)


        GLES20.glViewport(20, 20, 288, 120)
        waterSignature.drawFrame(loadWaterTexture)
    }

    /**
     * ????????????shader?????????
     * @param shaderType shader?????????  GLES20.GL_VERTEX_SHADER GLES20.GL_FRAGMENT_SHADER
     * @see GLES20.GL_VERTEX_SHADER
     * @see GLES20.GL_FRAGMENT_SHADER
     * @param shaderCode shader?????????
     * @return shader??????
     */
    private fun loadShader(shaderType: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        // ???????????????shader
        var shader = GLES20.glCreateShader(shaderType)

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        // ??????????????????shader???????????????
        // ??????????????????shader???????????????
        val compiled = IntArray(1)
        // ??????Shader???????????????
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == GLES20.GL_FALSE) {
            // ????????????????????????????????????????????????shader
            Log.e("ES20_ERROR", "Could not compile shader $shaderType:")
            Log.e("ES20_ERROR", GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            shader = 0
        }
        return shader
    }

    private fun transformTextureCoordinates(coords: FloatArray, matrix: FloatArray): FloatArray {
        val result = FloatArray(coords.size)
        val vt = FloatArray(4)
        var i = 0
        while (i < coords.size) {
            val v = floatArrayOf(coords[i], coords[i + 1], 0f, 1f)
            Matrix.multiplyMV(vt, 0, matrix, 0, v, 0)
            result[i] = vt[0]
            result[i + 1] = vt[1]
            i += 2
        }
        return result
    }

    private fun loadTexture(context: Context, resourceId: Int): Int {
        val textureObjectIds = IntArray(1)
        GLES20.glGenTextures(1, textureObjectIds, 0)
        if (textureObjectIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL texture object!")
            return 0
        }
        val options = BitmapFactory.Options()
        options.inScaled = false //????????????????????????????????????????????????
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
        if (bitmap == null) {
            Log.e(TAG, "Resource ID " + resourceId + "could not be decode")
            GLES20.glDeleteTextures(1, textureObjectIds, 0)
            return 0
        }
        //??????OpenGL??????????????????????????????????????????????????????
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0])
        //????????????????????????GL_TEXTURE_MIN_FILTER?????????mipmap???????????????
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR_MIPMAP_LINEAR
        )
        //????????????????????????GL_TEXTURE_MAG_FILTER????????????????????????
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        //Android??????y??????????????????????????????????????????????????????????????????????????????????????????????????????????????????T?????????y?????????????????????
        //ball?????????????????????  t????????????????????????+1
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        //????????????mipmap??????
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        //???????????????????????????
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textureObjectIds[0]
    }


}