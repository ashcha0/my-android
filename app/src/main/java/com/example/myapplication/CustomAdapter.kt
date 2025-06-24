package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class CustomAdapter(private val context: Context, private val dataList: List<ItemData>) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.list_item, parent, false)
            viewHolder = ViewHolder(
                view.findViewById(R.id.ivItemImage),
                view.findViewById(R.id.tvItemTitle),
                view.findViewById(R.id.tvItemDescription)
            )
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val item = dataList[position]
        viewHolder.imageView.setImageResource(item.imageResId)
        viewHolder.titleTextView.text = item.title
        viewHolder.descriptionTextView.text = item.description

        return view
    }

    private data class ViewHolder(
        val imageView: ImageView,
        val titleTextView: TextView,
        val descriptionTextView: TextView
    )
}