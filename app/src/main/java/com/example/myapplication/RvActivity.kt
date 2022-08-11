package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.TestRecyBinding

class RvActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = TestRecyBinding.inflate(layoutInflater)
        setContentView(inflate.root)
        val arrayList = ArrayList<String>()
        for (index in 1..100) {
            arrayList.add("$index")
        }

        inflate.recyclerView.layoutManager = LinearLayoutManager(this)

        inflate.recyclerView.adapter = RvAdapter(arrayList)
    }

}