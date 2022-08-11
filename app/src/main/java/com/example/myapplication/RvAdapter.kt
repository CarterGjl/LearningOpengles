package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemBinding
import java.util.ArrayList

class RvAdapter(private val strings:ArrayList<String>) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflate = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(inflate.root,inflate)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(strings[position])
    }

    override fun getItemCount(): Int {
        return strings.size
    }
    class ViewHolder(itemView: View,private val inflate: ItemBinding) : RecyclerView.ViewHolder(itemView) {
        fun bindData(string:String) {
            inflate.tvTitle.text = string
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, string, Toast.LENGTH_SHORT).show()
            }
        }
    }
}