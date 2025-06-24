package com.example.myapplication.network

import android.util.Log
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
        private const val TAG = "WeatherService"
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
        Log.d(TAG, "开始请求天气数据，城市代码: $cityCode")
        Log.d(TAG, "请求URL: ${BASE_URL}api/weather/city/$cityCode")
        
        weatherApi.getWeatherByCity(cityCode).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                Log.d(TAG, "收到响应，状态码: ${response.code()}")
                
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        Log.d(TAG, "响应状态: ${weatherResponse.status}, 消息: ${weatherResponse.message}")
                        Log.d(TAG, "城市信息: ${weatherResponse.cityInfo.city}")
                        Log.d(TAG, "预报数据条数: ${weatherResponse.data.forecast.size}")
                        
                        if (weatherResponse.status == 200) {
                            // 验证预报数据完整性
                            if (weatherResponse.data.forecast.isNotEmpty()) {
                                Log.d(TAG, "天气数据获取成功，包含${weatherResponse.data.forecast.size}天预报")
                                onSuccess(weatherResponse)
                            } else {
                                Log.e(TAG, "预报数据为空")
                                onError("预报数据为空")
                            }
                        } else {
                            Log.e(TAG, "API返回错误状态: ${weatherResponse.status}")
                            onError("获取天气数据失败: ${weatherResponse.message}")
                        }
                    } ?: run {
                        Log.e(TAG, "响应体为空")
                        onError("响应数据为空")
                    }
                } else {
                    Log.e(TAG, "HTTP请求失败，状态码: ${response.code()}")
                    onError("网络请求失败: ${response.code()}")
                }
            }
            
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e(TAG, "网络请求失败", t)
                onError("网络连接失败: ${t.message}")
            }
        })
    }
}