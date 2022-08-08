package com.example.audiorecord

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaprojection.MediaProjectionActivity
import com.example.myapplication.R

class AudioRecordActivity : AppCompatActivity() {

    private val mMediaProjectionManager: MediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)
        setSupportActionBar(findViewById(R.id.toolbar))

    }

}