package com.example.expanded

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import kotlinx.android.synthetic.main.child_view.view.*
import kotlinx.android.synthetic.main.group_view.view.*

class MyExpandedAdapter(context: Context) : ExpandedAdapter<String, String>() {
    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, position: Int, item: String) {
        if (holder is ChildViewHolder) {
            holder.bindData(item)
        }
    }
    private val layoutInflater = LayoutInflater.from(context)
    override fun onBindGroupHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        tem: ExpandedItem<String, String>,
        group: String
    ) {
        if (holder is GroupViewHolder) {
            holder.bindData(tem)
        }
    }

    override fun createRealViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_PARENT -> {
                return GroupViewHolder(view)
            }
            VIEW_TYPE_CHILD -> {
                return ChildViewHolder(view)
            }
            else -> {
                return GroupViewHolder(view)
            }
        }
    }

    override fun getGroupView(parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.group_view, parent, false)
    }

    override fun getChildView(parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.child_view, parent, false)
    }

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindData(expandedItem: ExpandedItem<String, String>) {
            itemView.tv1.text = expandedItem.group
        }
    }

    class ChildViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindData(expandedItem: String) {
            itemView.tv2.text = expandedItem
        }
    }
}