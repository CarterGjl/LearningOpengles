package com.example.camera.player

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.decoder.BaseDecoder
import com.example.decoder.Frame
import com.example.decoder.decode.AudioDecoder
import com.example.decoder.decode.VideoDecoder
import com.example.decoder.interfa.IDecoderStateListener
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityPlayerBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.Executors

class PlayerActivity : AppCompatActivity() {
    var videoDecoder: VideoDecoder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(inflate.root)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            videoDecoder?.goOn()
        }

        initPlayer(inflate)
    }

    companion object {
        private const val TAG = "PlayerActivity"
    }

    private fun initPlayer(inflate: ActivityPlayerBinding) {
        val path = Environment.getExternalStorageDirectory().absolutePath + "/mvtest.mp4"

        //创建线程池
        val threadPool = Executors.newFixedThreadPool(2)

//        sfv.holder.addCallback(object : SurfaceHolder.Callback {
//            override fun surfaceCreated(holder: SurfaceHolder) {
//
//                videoDecoder?.setStateListener(object : IDecoderStateListener {
//                    override fun decoderPrepare(baseDecoder: BaseDecoder) {
//                        Log.d(TAG, "decoderPrepare: ")
//                    }
//
//                    override fun decoderError(baseDecoder: BaseDecoder, msg: String) {
//                        Log.d(TAG, "decoderError: ")
//                    }
//
//                    override fun decoderRunning(baseDecoder: BaseDecoder) {
//                        Log.d(TAG, "decoderRunning: ")
//                    }
//
//                    override fun decoderPause(baseDecoder: BaseDecoder) {
//
//                        Log.d(TAG, "decoderPause: ")
//                    }
//
//                    override fun decoderFinish(baseDecoder: BaseDecoder) {
//                        Log.d(TAG, "decoderFinish: ")
//                    }
//
//                    override fun decoderDestroy(baseDecoder: BaseDecoder) {
//                        Log.d(TAG, "decoderDestroy: ")
//                    }
//
//                    override fun decodeOneFrame(baseDecoder: BaseDecoder, frame: Frame) {
//
//                        Log.d(TAG, "decodeOneFrame: ")
//                    }
//
//                })
//
//            }
//
//            override fun surfaceChanged(
//                holder: SurfaceHolder,
//                format: Int,
//                width: Int,
//                height: Int
//            ) {
//            }
//
//            override fun surfaceDestroyed(holder: SurfaceHolder) {
//            }
//
//        })
        videoDecoder = VideoDecoder(path, inflate.content.sfv, null)
        //创建视频解码器
        threadPool.execute(videoDecoder)
        videoDecoder?.setStateListener(object : IDecoderStateListener {
                    override fun decoderPrepare(baseDecoder: BaseDecoder) {
                        Log.d(TAG, "decoderPrepare: ")
                    }

                    override fun decoderError(baseDecoder: BaseDecoder, msg: String) {
                        Log.d(TAG, "decoderError: ")
                    }

                    override fun decoderRunning(baseDecoder: BaseDecoder) {
                        Log.d(TAG, "decoderRunning: ")
                    }

                    override fun decoderPause(baseDecoder: BaseDecoder) {

                        Log.d(TAG, "decoderPause: ")
                    }

                    override fun decoderFinish(baseDecoder: BaseDecoder) {
                        Log.d(TAG, "decoderFinish: ")
                    }

                    override fun decoderDestroy(baseDecoder: BaseDecoder) {
                        Log.d(TAG, "decoderDestroy: ")
                    }

                    override fun decodeOneFrame(baseDecoder: BaseDecoder, frame: Frame) {

                        Log.d(TAG, "decodeOneFrame: ")
                    }

                })
        //创建音频解码器
        val audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)

        Handler().postDelayed({
//开启播放
            videoDecoder?.goOn()

            audioDecoder.goOn()
        },200)
    }

}