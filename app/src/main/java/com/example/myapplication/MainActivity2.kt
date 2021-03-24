package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.test1.*

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test1)
        rbG.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.rb_1 -> {
                    Toast.makeText(this, "R.id.rb_1$checkedId", Toast.LENGTH_SHORT).show();
                }
                R.id.rb_2 -> {
                    Toast.makeText(this, "R.id.rb_2$checkedId", Toast.LENGTH_SHORT).show();
                }
            }
        }
        btnCommit.setOnClickListener {
            when (rbG.checkedRadioButtonId) {
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