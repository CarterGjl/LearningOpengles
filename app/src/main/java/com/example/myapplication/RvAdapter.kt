package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item.view.*
import java.util.ArrayList

class RvAdapter(private val strings:ArrayList<String>) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item,parent,false))

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(strings[position])
    }

    override fun getItemCount(): Int {
        return strings.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(string:String) {
            itemView.tvTitle.text = string
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, string, Toast.LENGTH_SHORT).show();
            }
        }
    }
}