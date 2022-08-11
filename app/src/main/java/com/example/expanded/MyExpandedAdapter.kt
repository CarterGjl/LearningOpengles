package com.example.expanded

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ChildViewBinding
import com.example.myapplication.databinding.GroupViewBinding

class MyExpandedAdapter(context: Context) : ExpandedAdapter<String, String>() {

    private lateinit var childViewBinding: ChildViewBinding
    private lateinit var groupViewBinding: GroupViewBinding

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
                return GroupViewHolder(view, groupViewBinding)
            }
            VIEW_TYPE_CHILD -> {
                return ChildViewHolder(view, childViewBinding)
            }
            else -> {
                return GroupViewHolder(view, groupViewBinding)
            }
        }
    }

    override fun getGroupView(parent: ViewGroup): View {
        groupViewBinding = GroupViewBinding.inflate(layoutInflater, parent, false)
        return groupViewBinding.root
    }

    override fun getChildView(parent: ViewGroup): View {
        childViewBinding = ChildViewBinding.inflate(layoutInflater, parent, false)
        return childViewBinding.root
    }

    class GroupViewHolder(view: View, private val groupViewBinding: GroupViewBinding) :
        RecyclerView.ViewHolder(view) {
        fun bindData(expandedItem: ExpandedItem<String, String>) {
            groupViewBinding.tv1.text = expandedItem.group
        }
    }

    class ChildViewHolder(view: View, private val childViewBinding: ChildViewBinding) :
        RecyclerView.ViewHolder(view) {
        fun bindData(expandedItem: String) {
            childViewBinding.tv2.text = expandedItem
        }
    }
}