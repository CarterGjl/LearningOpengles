package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.People
import com.example.myapplication.utils.dp
import com.example.myapplication.utils.getScreenHeight
import com.example.myapplication.utils.makeTopToast
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_text.*


class TextActivity : AppCompatActivity() {
    private var people:People? = null
    private var people1:People? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)
        getScreenHeight()

        text_tv.setOnClickListener {
            people = People()
            people1 = people
            people = null
            makeTopToast("您已静音，请开启麦克风后发言")
            Snackbar.make(it, "您已静音，请开启麦克风后发言", Snackbar.LENGTH_LONG).apply {
                val view: ViewGroup = this.view as ViewGroup
                val layoutParams = view.layoutParams as FrameLayout.LayoutParams
                layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                layoutParams.topMargin = 20f.dp.toInt()
                layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
                view.layoutParams = layoutParams


//内容的字体颜色与大小
                val tvSnackbarText:TextView = view.findViewById(R.id.snackbar_text)
                tvSnackbarText.textSize = 17f
                view.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.custom_toast_bg,
                    null
                )
                this.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
            }.show()
        }
    }

    override fun onResume() {
        super.onResume()
//        text_tv.postDelayed({
//            startActivity(Intent(this, TextActivity::class.java))
//        }, 5000)
    }
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
    }
}