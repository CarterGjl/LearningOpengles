package com.example.opengl

import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

private const val TAG = "VideoGlRender"

class VideoGlRender : GLRenderer() {

    var mTextureId: Int = -1

    override fun onCreated() {
        initPos()
    }

    override fun onUpdate() {
    }

    override fun onDrawFrame(outputSurface: GLSurface?) {
        draw()
    }


    private fun initPos() {
        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(squareCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer?.put(squareCoords)
        vertexBuffer?.position(0)

        // initialize byte buffer for the draw list
        val dlb = ByteBuffer.allocateDirect(drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer?.put(drawOrder)
        drawListBuffer?.position(0)
        val bb2 = ByteBuffer.allocateDirect(textureVertices.size * 4)
        bb2.order(ByteOrder.nativeOrder())
        textureVerticesBuffer = bb2.asFloatBuffer()
        textureVerticesBuffer?.put(textureVertices)
        textureVerticesBuffer?.position(0)

        mProgram = createProgram()
        // get handle to vertex shader's vPosition member
        // 获取着色器中的属性引用id(传入的字符串就是我们着色器脚本中的属性名)
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate")

    }

    private fun createProgram(): Int {
        // 加载顶点着色器
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        // 加载片元着色器
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // 创建程序
        var createProgram = GLES20.glCreateProgram() // create empty OpenGL ES Program
        // 若程序创建成功则向程序中加入顶点着色器与片元着色器
        // 向程序中加入顶点着色器
        GLES20.glAttachShader(createProgram, vertexShader) // add the vertex shader to program
        // 向程序中加入片元着色器
        GLES20.glAttachShader(createProgram, fragmentShader) // add the fragment shader to program
        // 链接程序
        GLES20.glLinkProgram(createProgram) // creates OpenGL ES program executables
        // 存放链接成功program数量的数组
        val linkStatus = IntArray(1)
        // 获取program的链接情况
        GLES20.glGetProgramiv(createProgram, GLES20.GL_LINK_STATUS, linkStatus, 0)
        // 若链接失败则报错并删除程序
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e("ES20_ERROR", "Could not link program: ")
            Log.e("ES20_ERROR", GLES20.glGetProgramInfoLog(createProgram))
            GLES20.glDeleteProgram(createProgram)
            createProgram = 0
        }
        Log.d(TAG, "initPos: ${linkStatus[0]}")
        return createProgram;
    }

    override fun onDestroy() {

    }


    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "attribute vec2 inputTextureCoordinate;" +
            "varying vec2 textureCoordinate;" +
            "void main()" +
            "{" +
            "gl_Position = vPosition;" +
            "textureCoordinate = inputTextureCoordinate;" +
            "}"

    private val fragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;varying vec2 textureCoordinate;
        uniform samplerExternalOES s_texture;
        void main() {  gl_FragColor = texture2D( s_texture, textureCoordinate );
        }
        """.trimIndent()

    private var vertexBuffer: FloatBuffer? =
        null
    var textureVerticesBuffer: FloatBuffer? = null
    private var drawListBuffer: ShortBuffer? = null
    private var mProgram = 0
    private var mPositionHandle = 0
    private var mTextureCoordHandle = 0

    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices


    // number of coordinates per vertex in this array
    private val COORDS_PER_VERTEX = 2

    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex


    private var squareCoords = floatArrayOf(
        -1.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, -1.0f,
        1.0f, 1.0f
    )

    private var textureVertices = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 0.0f
    )


    private fun draw() {

        // 激活指定纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // 绑定纹理ID到纹理单元
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)
        // 使用某套shader程序
        GLES20.glUseProgram(mProgram)


        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        GLES20.glEnableVertexAttribArray(mTextureCoordHandle)

        // Prepare the <insert shape here> coordinate data
        // 为画笔指定顶点位置数据(vPosition)
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        GLES20.glVertexAttribPointer(
            mTextureCoordHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            textureVerticesBuffer
        )
        // 绘制
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            drawOrder.size,
            GLES20.GL_UNSIGNED_SHORT,
            drawListBuffer
        )

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        val shader = GLES20.glCreateShader(type)

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
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
}