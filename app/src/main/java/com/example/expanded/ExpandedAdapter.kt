package com.example.expanded

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

@Suppress("UNCHECKED_CAST")
abstract class ExpandedAdapter<K, V> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_PARENT = 1
        const val VIEW_TYPE_CHILD = 2
        private const val TAG = "ExpandedAdapter"
    }


    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    private var expandedItems: ArrayList<ExpandedItem<K, V>> = ArrayList()
    private var showDatas = ArrayList<Any>()
    override fun getItemCount(): Int {

        return showDatas.size
    }

    fun addExpandedItem(expandedItem: ExpandedItem<K, V>) {
        expandedItems.add(expandedItem)
    }

    fun refreshDatas() {
        for (expandedItem in expandedItems) {
            showDatas.add(expandedItem)
            if (expandedItem.isExpanded) {
                showDatas.add(expandedItem.children)
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if (showDatas[position] is ExpandedItem<*, *>) {
            return VIEW_TYPE_PARENT
        } else {
            return VIEW_TYPE_CHILD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View = getGroupView(parent = parent)
        when (viewType) {
            VIEW_TYPE_PARENT -> {
                view = getGroupView(parent = parent)
            }
            VIEW_TYPE_CHILD -> {
                view = getChildView(parent = parent)
            }
        }
        return createRealViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = showDatas[position]
        if (item is ExpandedItem<*, *>) {
            onBindGroupHolder(holder, position, item as ExpandedItem<K, V>, item.group)
            holder.itemView.setOnClickListener {
                onItemClickListener?.onGroupItemClick(position = position, it)
                if (item.isExpanded) {
                    collapseGroup(position)
                } else {
                    expandGroup(position)
                }
            }

        } else {
            onBindChildHolder(holder, position, item = item as V)
            holder.itemView.setOnClickListener {
                onItemClickListener?.onChildItemClick(position = position, it)
            }
        }
    }

    private fun expandGroup(position: Int) {
        val item = showDatas[position]
        if (item !is ExpandedItem<*, *>) {
            return
        }
        if (item.isExpanded){
            return
        }
        item.isExpanded = true
        val tempChilds = item.children
        showDatas.addAll(position + 1, tempChilds)
        notifyItemRangeInserted(position + 1, tempChilds.size)
        notifyItemRangeChanged(position + 1, showDatas.size - (position + 1))
    }

    private fun collapseGroup(position: Int) {
        val item = showDatas[position]

        if (item !is ExpandedItem<*, *>) {
            return
        }
        if (!item.isExpanded){
            return
        }
        item.isExpanded = false
        val tempChilds = item.children
        val tempSize = showDatas.size
        showDatas.removeAll(tempChilds)
        try {
            Log.d(TAG, "collapseGroup:tempSize $tempSize  current size ${showDatas.size}")
//            notifyItemRangeRemoved(position + 1, tempChilds.size - showDatas.size)
//            notifyItemRangeChanged(position + 1, tempSize - (position + 1))
            notifyDataSetChanged()
        }catch (e :IndexOutOfBoundsException){
            Log.e(TAG, "collapseGroup: ", e)
        }

    }

    abstract fun onBindChildHolder(holder: RecyclerView.ViewHolder, position: Int, item: V)

    abstract fun onBindGroupHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        tem: ExpandedItem<K, V>,
        group: K
    )

    abstract fun createRealViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder

    abstract fun getGroupView(parent: ViewGroup): View
    abstract fun getChildView(parent: ViewGroup): View
}

interface OnItemClickListener {
    fun onGroupItemClick(position: Int, view: View?)
    fun onChildItemClick(position: Int, view: View?)
}