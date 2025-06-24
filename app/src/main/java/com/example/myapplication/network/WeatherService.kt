package com.example.myapplication.network

import com.example.myapplication.api.WeatherApi
import com.example.myapplication.model.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 天气服务网络管理类
 */
class WeatherService {
    
    companion object {
        private const val BASE_URL = "http://t.weather.itboy.net/"
        
        @Volatile
        private var INSTANCE: WeatherService? = null
        
        fun getInstance(): WeatherService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WeatherService().also { INSTANCE = it }
            }
        }
    }
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val weatherApi = retrofit.create(WeatherApi::class.java)
    
    /**
     * 获取天气数据
     * @param cityCode 城市代码
     * @param onSuccess 成功回调
     * @param onError 失败回调
     */
    fun getWeather(
        cityCode: String,
        onSuccess: (WeatherResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        weatherApi.getWeatherByCity(cityCode).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        if (weatherResponse.status == 200) {
                            onSuccess(weatherResponse)
                        } else {
                            onError("获取天气数据失败: ${weatherResponse.message}")
                        }
                    } ?: onError("响应数据为空")
                } else {
                    onError("网络请求失败: ${response.code()}")
                }
            }
            
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                onError("网络连接失败: ${t.message}")
            }
        })
    }
}