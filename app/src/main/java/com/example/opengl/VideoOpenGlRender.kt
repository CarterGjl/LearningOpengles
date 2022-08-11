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

    var mTextureId: Int = -1

    private var loadWaterTexture: Int = -1
    private lateinit var waterSignature: WaterSignature

    override fun onCreated() {
        initPos()
        waterSignature = WaterSignature()
        waterSignature.setShaderProgram(WaterSignSProgram())
        loadWaterTexture = loadTexture(ChatApplication.context, R.drawable.test)
    }

    override fun onDrawFrame(output: GLSurface) {
        draw()
    }

    override fun onUpdate() {
    }

    private fun initPos() {
        // initialize vertex byte buffer for shape coordinates
        getVertices()
        // initialize byte buffer for the draw list
        val dlb = ByteBuffer.allocateDirect(drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer?.put(drawOrder)
        drawListBuffer?.position(0)
        val bb2 = ByteBuffer.allocateDirect(TEXTURE_FRONT.size * 4)
        bb2.order(ByteOrder.nativeOrder())
        textureVerticesBuffer = bb2.asFloatBuffer()
        textureVerticesBuffer?.put(TEXTURE_FRONT)
        textureVerticesBuffer?.position(0)

        mProgram = createProgram()
        // get handle to vertex shader's vPosition member
        // 获取着色器中的属性引用id(传入的字符串就是我们着色器脚本中的属性名)
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate")

    }

    /**
     * 获取图形的顶点
     * 特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
     * 转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
     *
     * @return 顶点Buffer
     */
    private fun getVertices() {
        // 创建顶点坐标数据缓冲
        // squareCoords.length*4是因为一个float占四个字节
        val bb = ByteBuffer.allocateDirect(squareCoords.size * 4)
        // 设置字节顺序
        bb.order(ByteOrder.nativeOrder())
        // 转换为Float型缓冲
        vertexBuffer = bb.asFloatBuffer()
        // 向缓冲区中放入顶点坐标数据
        vertexBuffer?.put(squareCoords)
        // 设置缓冲区起始位置
        vertexBuffer?.position(0)
    }

    /***
     * 创建着色器程序
     */
    private fun createProgram(): Int {
        // 初始化着色器
        // 基于顶点着色器与片元着色器创建程序
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
        return createProgram
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
        precision mediump float;
        varying vec2 textureCoordinate;
        uniform samplerExternalOES s_texture;
        void main() {  
           gl_FragColor = texture2D( s_texture, textureCoordinate );
        }
        """.trimIndent()

    private var vertexBuffer: FloatBuffer? = null
    private var textureVerticesBuffer: FloatBuffer? = null
    private var drawListBuffer: ShortBuffer? = null
    private var mProgram = 0
    private var mPositionHandle = 0
    private var mTextureCoordHandle = 0

    // OpenGL ES中绘制任何形状都是通过绘制多个三角形而组成，
    // 所以我们将这4个点分为2个三角形，分布为（V1,V2,V3）和（V1,V3,V4），
    // 因此定义三角形索引数组代码如下：
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

    // 前置摄像头使用的纹理坐标
    private val TEXTURE_FRONT = floatArrayOf(
        1.0f, 1.0f,
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f
    )

    // 后置摄像头使用的纹理坐标
    private val textureVertices = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 0.0f
    )


    private fun draw() {
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
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
        // GLES20.glVertexAttribPointer(属性索引,单顶点大小,数据类型,归一化,顶点间偏移量,顶点Buffer)
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


        GLES20.glViewport(20, 20, 288, 120)
        waterSignature.drawFrame(loadWaterTexture)
    }

    /**
     * 加载制定shader的方法
     * @param shaderType shader的类型  GLES20.GL_VERTEX_SHADER GLES20.GL_FRAGMENT_SHADER
     * @see GLES20.GL_VERTEX_SHADER
     * @see GLES20.GL_FRAGMENT_SHADER
     * @param shaderCode shader的脚本
     * @return shader索引
     */
    private fun loadShader(shaderType: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        // 创建一个新shader
        var shader = GLES20.glCreateShader(shaderType)

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        // 存放编译成功shader数量的数组
        // 存放编译成功shader数量的数组
        val compiled = IntArray(1)
        // 获取Shader的编译情况
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == GLES20.GL_FALSE) {
            // 若编译失败则显示错误日志并删除此shader
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
        options.inScaled = false //指定需要的是原始数据，非压缩数据
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
        if (bitmap == null) {
            Log.e(TAG, "Resource ID " + resourceId + "could not be decode")
            GLES20.glDeleteTextures(1, textureObjectIds, 0)
            return 0
        }
        //告诉OpenGL后面纹理调用应该是应用于哪个纹理对象
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0])
        //设置缩小的时候（GL_TEXTURE_MIN_FILTER）使用mipmap三线程过滤
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR_MIPMAP_LINEAR
        )
        //设置放大的时候（GL_TEXTURE_MAG_FILTER）使用双线程过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        //Android设备y坐标是反向的，正常图显示到设备上是水平颠倒的，解决方案就是设置纹理包装，纹理T坐标（y）设置镜面重复
        //ball读取纹理的时候  t范围坐标取正常值+1
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        //快速生成mipmap贴图
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        //解除纹理操作的绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textureObjectIds[0]
    }


}