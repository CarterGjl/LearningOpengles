package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.example.myapplication.databinding.ActivityScaleBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ScaleActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ScaleActivity"
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        println("rotation" + wm.defaultDisplay.rotation)
        Surface.ROTATION_90
        Log.d(TAG, "onConfigurationChanged: " + wm.defaultDisplay.rotation)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = ActivityScaleBinding.inflate(layoutInflater)
        setContentView(inflate.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        Surface.ROTATION_90
//        PopupMenu(this,btnClick).apply {
//            inflate(R.menu.menu_test)
//        }.show()


//        val externalStoragePublicDirectory =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        if (externalStoragePublicDirectory.exists()) {
//            for (listFile in externalStoragePublicDirectory.listFiles()!!) {
//                println("file name"+listFile.name)
//            }
//        }


        val registerForActivityResult = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { it ->
            if (it == null) {
                return@registerForActivityResult
            }
            val query = this.contentResolver.query(
                it,
                null, null, null, null
            )
            query?.let {
                it.moveToFirst()
                val columnIndex = query.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                println("RELATIVE_PATH" + query.getString(columnIndex))
//                println("DATA"+query.getString(query.getColumnIndex(MediaStore.Images.ImageColumns.DATA)))
                query.close()
            }


        }
        inflate.content.popup.setOnClickListener {
            popupWindow(it)
        }

//        stack.push("1")
//        stack.push("2")
//        stack.push("3")
        inflate.content.btnClick.setOnClickListener {
            Toast.makeText(this, getNextShowView(), Toast.LENGTH_SHORT).show();
//            registerForActivityResult.launch(null)
//            startActivityForResult(
//                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//                    addCategory(Intent.CATEGORY_OPENABLE)
//                    type = "*/*"
//                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
//                        "application/pdf", // .pdf
//                        "application/vnd.oasis.opendocument.text", // .odt
//                        "text/plain" // .txt
//                    ))
//                },
//                REQUEST_CODE
//            )
        }
//        val scaleGestureDetector =
//            ScaleGestureDetector(this, object : SimpleOnScaleGestureListener() {
//                override fun onScale(detector: ScaleGestureDetector): Boolean {
//                    Log.d(TAG, "onScale: ${detector.scaleFactor}")
//                    return super.onScale(detector)
//                }
//
//                override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
//                    Log.d(TAG, "onScaleBegin: ")
//                    return super.onScaleBegin(detector)
//                }
//
//                override fun onScaleEnd(detector: ScaleGestureDetector?) {
//                    Log.d(TAG, "onScaleEnd: ")
//                    super.onScaleEnd(detector)
//                }
//            })
//        scale_root.setOnTouchListener { _, event ->
//            return@setOnTouchListener scaleGestureDetector.onTouchEvent(event)
//        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            popupWindow(it)
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
        }


    }

    private fun getNextShowView(): String? {
//        return if (!stack.isEmpty()){
//            stack.pop()
//            stack.peek()
//        }else{
//            null
//        }
        return null
    }

    private fun popupWindow(it: View) {
        val popupMenu = PopupMenu(this, it)
        popupMenu.menuInflater.inflate(R.menu.menu_test, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.tv1 -> {
                    Toast.makeText(this, "tv1", Toast.LENGTH_SHORT).show();
                }
                R.id.tv2 -> {
                    Toast.makeText(this, "tv2", Toast.LENGTH_SHORT).show();
                }
                else -> {
                }
            }
            return@setOnMenuItemClickListener super.onOptionsItemSelected(it)
        }
        popupMenu.show()
    }
}