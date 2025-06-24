package com.example.myapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 天气API响应数据模型
 */
data class WeatherResponse(
    val message: String,
    val status: Int,
    val date: String,
    val time: String,
    val cityInfo: CityInfo,
    val data: WeatherData
)

/**
 * 城市信息数据模型
 */
data class CityInfo(
    val city: String,
    val citykey: String,
    val parent: String,
    val updateTime: String
)

/**
 * 天气数据模型
 */
data class WeatherData(
    val shidu: String,          // 湿度
    val pm25: Float,            // PM2.5
    val pm10: Float,            // PM10
    val quality: String,        // 空气质量
    val wendu: String,          // 温度
    val ganmao: String,         // 感冒提醒
    val forecast: List<Forecast>, // 未来天气预报
    val yesterday: Forecast     // 昨天天气
)

/**
 * 天气预报数据模型
 */
data class Forecast(
    val date: String,       // 日期
    val high: String,        // 最高温度
    val low: String,         // 最低温度
    val ymd: String,         // 年月日
    val week: String,        // 星期
    val sunrise: String,     // 日出时间
    val sunset: String,      // 日落时间
    val aqi: Int,            // 空气质量指数
    val fx: String,          // 风向
    val fl: String,          // 风力
    val type: String,        // 天气类型
    val notice: String       // 提示
) : Serializable