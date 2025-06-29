package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Diary
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日记列表适配器
 */
class DiaryAdapter(
    private var diaries: MutableList<Diary>,
    private val onItemClick: (Diary) -> Unit,
    private val onItemLongClick: (Diary) -> Unit
) : RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>() {
    
    class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_diary_title)
        val tvContent: TextView = itemView.findViewById(R.id.tv_diary_content)
        val tvDate: TextView = itemView.findViewById(R.id.tv_diary_date)
        val tvMood: TextView = itemView.findViewById(R.id.tv_diary_mood)
        val tvTime: TextView = itemView.findViewById(R.id.tv_diary_time)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diary, parent, false)
        return DiaryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val diary = diaries[position]
        
        holder.tvTitle.text = diary.title
        holder.tvContent.text = if (diary.content.length > 100) {
            diary.content.substring(0, 100) + "..."
        } else {
            diary.content
        }
        holder.tvDate.text = diary.date
        holder.tvMood.text = diary.mood
        
        // 格式化时间显示
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.tvTime.text = timeFormat.format(Date(diary.createTime))
        
        // 设置点击事件
        holder.itemView.setOnClickListener {
            onItemClick(diary)
        }
        
        // 设置长按事件
        holder.itemView.setOnLongClickListener {
            onItemLongClick(diary)
            true
        }
    }
    
    override fun getItemCount(): Int = diaries.size
    
    /**
     * 更新数据
     */
    fun updateData(newDiaries: List<Diary>) {
        diaries.clear()
        diaries.addAll(newDiaries)
        notifyDataSetChanged()
    }
    
    /**
     * 添加日记
     */
    fun addDiary(diary: Diary) {
        diaries.add(0, diary)
        notifyItemInserted(0)
    }
    
    /**
     * 删除日记
     */
    fun removeDiary(diary: Diary) {
        val position = diaries.indexOf(diary)
        if (position >= 0) {
            diaries.removeAt(position)
            notifyItemRemoved(position)
        }
    }
    
    /**
     * 更新日记
     */
    fun updateDiary(diary: Diary) {
        val position = diaries.indexOfFirst { it.id == diary.id }
        if (position >= 0) {
            diaries[position] = diary
            notifyItemChanged(position)
        }
    }
}