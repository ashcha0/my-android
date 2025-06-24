package com.example.myapplication.api

import com.example.myapplication.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 天气API接口
 */
interface WeatherApi {
    
    /**
     * 根据城市代码获取天气信息
     * @param cityCode 城市代码，如：101010100（北京）
     */
    @GET("api/weather/city/{cityCode}")
    fun getWeatherByCity(@Path("cityCode") cityCode: String): Call<WeatherResponse>
}

/**
 * 常用城市代码
 */
object CityCode {
    const val BEIJING = "101010100"      // 北京
    const val SHANGHAI = "101020100"     // 上海
    const val GUANGZHOU = "101280101"    // 广州
    const val SHENZHEN = "101280601"     // 深圳
    const val HANGZHOU = "101210101"     // 杭州
    const val NANJING = "101190101"      // 南京
    const val WUHAN = "101200101"        // 武汉
    const val CHENGDU = "101270101"      // 成都
    const val XIAN = "101110101"         // 西安
    const val TIANJIN = "101030100"      // 天津
}