package com.example.myapplication.utils

import com.example.myapplication.R

/**
 * 天气图标工具类
 */
object WeatherIconUtils {
    
    /**
     * 根据天气类型获取对应的图标资源ID
     * @param weatherType 天气类型
     * @return 图标资源ID
     */
    fun getWeatherIcon(weatherType: String): Int {
        return when (weatherType) {
            "晴" -> R.drawable.ic_sunny
            "多云" -> R.drawable.ic_cloudy
            "阴" -> R.drawable.ic_overcast
            "小雨" -> R.drawable.ic_light_rain
            "中雨" -> R.drawable.ic_moderate_rain
            "大雨" -> R.drawable.ic_heavy_rain
            "暴雨" -> R.drawable.ic_storm
            "雷阵雨" -> R.drawable.ic_thunderstorm
            "小雪" -> R.drawable.ic_light_snow
            "中雪" -> R.drawable.ic_moderate_snow
            "大雪" -> R.drawable.ic_heavy_snow
            "雾" -> R.drawable.ic_fog
            "霾" -> R.drawable.ic_haze
            "沙尘暴" -> R.drawable.ic_sandstorm
            else -> R.drawable.ic_unknown
        }
    }
    
    /**
     * 获取天气描述
     * @param weatherType 天气类型
     * @return 天气描述
     */
    fun getWeatherDescription(weatherType: String): String {
        return when (weatherType) {
            "晴" -> "阳光明媚，适合户外活动"
            "多云" -> "云朵较多，注意防晒"
            "阴" -> "天空阴沉，可能有降水"
            "小雨" -> "小雨绵绵，记得带伞"
            "中雨" -> "雨势适中，出行注意安全"
            "大雨" -> "雨势较大，尽量减少外出"
            "暴雨" -> "暴雨天气，注意安全防护"
            "雷阵雨" -> "雷雨天气，避免户外活动"
            "小雪" -> "雪花飞舞，注意保暖"
            "中雪" -> "雪势适中，路面湿滑"
            "大雪" -> "大雪纷飞，出行困难"
            "雾" -> "能见度低，注意交通安全"
            "霾" -> "空气质量差，减少户外活动"
            "沙尘暴" -> "沙尘天气，做好防护措施"
            else -> "天气状况未知"
        }
    }
}