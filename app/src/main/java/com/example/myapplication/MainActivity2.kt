package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.Test1Binding

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val test1Binding = Test1Binding.inflate(layoutInflater)
        setContentView(R.layout.test1)
        test1Binding.rbG.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_1 -> {
                    Toast.makeText(this, "R.id.rb_1$checkedId", Toast.LENGTH_SHORT).show();
                }
                R.id.rb_2 -> {
                    Toast.makeText(this, "R.id.rb_2$checkedId", Toast.LENGTH_SHORT).show();
                }
            }
        }
        test1Binding.btnCommit.setOnClickListener {
            when (test1Binding.rbG.checkedRadioButtonId) {
                R.id.rb_1 -> {
                    Toast.makeText(this, "R.id.rb_1", Toast.LENGTH_SHORT).show();
                }
                R.id.rb_2 -> {
                    Toast.makeText(this, "R.id.rb_2", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}