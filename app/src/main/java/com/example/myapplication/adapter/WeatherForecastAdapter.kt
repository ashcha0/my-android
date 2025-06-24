package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Forecast
import com.example.myapplication.utils.WeatherIconUtils

/**
 * 天气预报列表适配器
 */
class WeatherForecastAdapter(
    private var forecastList: List<Forecast>,
    private val onItemClick: (Forecast) -> Unit
) : RecyclerView.Adapter<WeatherForecastAdapter.ViewHolder>() {
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvWeek: TextView = itemView.findViewById(R.id.tv_week)
        val ivWeatherIcon: ImageView = itemView.findViewById(R.id.iv_weather_icon)
        val tvWeatherType: TextView = itemView.findViewById(R.id.tv_weather_type)
        val tvHighTemp: TextView = itemView.findViewById(R.id.tv_high_temp)
        val tvLowTemp: TextView = itemView.findViewById(R.id.tv_low_temp)
        val tvWindInfo: TextView = itemView.findViewById(R.id.tv_wind_info)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather_forecast, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val forecast = forecastList[position]
        
        with(holder) {
            tvDate.text = forecast.ymd
            tvWeek.text = forecast.week
            tvWeatherType.text = forecast.type
            tvHighTemp.text = forecast.high
            tvLowTemp.text = forecast.low
            tvWindInfo.text = "${forecast.fx} ${forecast.fl}"
            
            // 设置天气图标
            ivWeatherIcon.setImageResource(WeatherIconUtils.getWeatherIcon(forecast.type))
            
            // 设置点击事件
            itemView.setOnClickListener {
                onItemClick(forecast)
            }
        }
    }
    
    override fun getItemCount(): Int = forecastList.size
    
    /**
     * 更新数据
     */
    fun updateData(newForecastList: List<Forecast>) {
        forecastList = newForecastList
        notifyDataSetChanged()
    }
}