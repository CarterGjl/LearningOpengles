package com.example.myapplication

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.test_recy.*

class RvActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_recy)
        val arrayList = ArrayList<String>()
        for (index in 1..100){
            arrayList.add("$index")
        }

        recycler_view.layoutManager = LinearLayoutManager(this)

        recycler_view.adapter = RvAdapter(arrayList)
    }
    fun sout1(): Unit {
        println("dfas ")
    }
}