package com.example.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.model.Forecast
import com.example.myapplication.utils.WeatherIconUtils

class WeatherDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvDetailDate: TextView
    private lateinit var tvDetailWeek: TextView
    private lateinit var ivDetailWeatherIcon: ImageView
    private lateinit var tvDetailWeatherType: TextView
    private lateinit var tvDetailHighTemp: TextView
    private lateinit var tvDetailLowTemp: TextView
    private lateinit var tvDetailWind: TextView
    private lateinit var tvDetailSun: TextView
    private lateinit var tvDetailAqi: TextView
    private lateinit var tvDetailNotice: TextView

    companion object {
        const val EXTRA_FORECAST = "extra_forecast"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_detail)

        initViews()
        setupClickListeners()
        loadWeatherDetail()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        tvDetailDate = findViewById(R.id.tv_detail_date)
        tvDetailWeek = findViewById(R.id.tv_detail_week)
        ivDetailWeatherIcon = findViewById(R.id.iv_detail_weather_icon)
        tvDetailWeatherType = findViewById(R.id.tv_detail_weather_type)
        tvDetailHighTemp = findViewById(R.id.tv_detail_high_temp)
        tvDetailLowTemp = findViewById(R.id.tv_detail_low_temp)
        tvDetailWind = findViewById(R.id.tv_detail_wind)
        tvDetailSun = findViewById(R.id.tv_detail_sun)
        tvDetailAqi = findViewById(R.id.tv_detail_aqi)
        tvDetailNotice = findViewById(R.id.tv_detail_notice)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadWeatherDetail() {
        val forecast = intent.getSerializableExtra(EXTRA_FORECAST) as? Forecast
        forecast?.let {
            displayWeatherDetail(it)
        }
    }

    private fun displayWeatherDetail(forecast: Forecast) {
        // 设置日期和星期
        tvDetailDate.text = forecast.ymd
        tvDetailWeek.text = forecast.week

        // 设置天气图标和类型
        val iconResId = WeatherIconUtils.getWeatherIcon(forecast.type)
        ivDetailWeatherIcon.setImageResource(iconResId)
        tvDetailWeatherType.text = forecast.type

        // 设置温度
        tvDetailHighTemp.text = forecast.high
        tvDetailLowTemp.text = forecast.low

        // 设置风向风力
        tvDetailWind.text = "${forecast.fx} ${forecast.fl}"

        // 设置日出日落
        tvDetailSun.text = "${forecast.sunrise} / ${forecast.sunset}"

        // 设置空气质量
        tvDetailAqi.text = forecast.aqi.toString()

        // 设置天气提示
        tvDetailNotice.text = forecast.notice
    }
}