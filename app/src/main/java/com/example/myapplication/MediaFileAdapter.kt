package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class MediaFileAdapter(
    private val context: Context,
    private val mediaFiles: List<MediaFile>
) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int = mediaFiles.size

    override fun getItem(position: Int): MediaFile = mediaFiles[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.item_media_file, parent, false)
            viewHolder = ViewHolder(
                view.findViewById(R.id.ivMediaType),
                view.findViewById(R.id.tvFileName),
                view.findViewById(R.id.tvFileInfo),
                view.findViewById(R.id.tvFileType)
            )
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val mediaFile = mediaFiles[position]
        
        // 设置媒体类型图标
        if (mediaFile.isVideo) {
            viewHolder.mediaTypeIcon.setImageResource(android.R.drawable.ic_media_play)
        } else {
            viewHolder.mediaTypeIcon.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
        }
        
        // 设置文件名
        viewHolder.fileName.text = mediaFile.title
        
        // 设置文件信息
        val artist = mediaFile.artist ?: "未知艺术家"
        val duration = mediaFile.getDurationString()
        val size = mediaFile.getSizeString()
        viewHolder.fileInfo.text = "$artist • $duration • $size"
        
        // 设置文件类型
        viewHolder.fileType.text = if (mediaFile.isVideo) "视频" else "音频"
        
        return view
    }

    private data class ViewHolder(
        val mediaTypeIcon: ImageView,
        val fileName: TextView,
        val fileInfo: TextView,
        val fileType: TextView
    )
}