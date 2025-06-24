package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.WeatherForecastAdapter
import com.example.myapplication.api.CityCode
import com.example.myapplication.model.City
import com.example.myapplication.model.Forecast
import com.example.myapplication.model.WeatherResponse
import com.example.myapplication.network.WeatherService
import com.example.myapplication.utils.LocationUtils
import com.example.myapplication.utils.WeatherIconUtils

class MainActivity : AppCompatActivity() {

    private lateinit var tvCityName: TextView
    private lateinit var btnLocation: ImageButton
    private lateinit var btnCitySelect: ImageButton
    private lateinit var tvCurrentTemp: TextView
    private lateinit var tvCurrentWeather: TextView
    private lateinit var ivCurrentWeatherIcon: ImageView
    private lateinit var tvAirQuality: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvPm25: TextView
    private lateinit var tvHealthTip: TextView
    private lateinit var rvWeatherForecast: RecyclerView
    private lateinit var progressLoading: ProgressBar

    private lateinit var weatherService: WeatherService
    private lateinit var forecastAdapter: WeatherForecastAdapter
    private lateinit var locationUtils: LocationUtils

    private var currentCityCode = CityCode.BEIJING // 默认北京
    private var currentCityName = "北京市"

    private val citySelectLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedCity = result.data?.getSerializableExtra(CitySelectActivity.EXTRA_SELECTED_CITY) as? City
            selectedCity?.let {
                currentCityCode = it.code
                currentCityName = it.name
                tvCityName.text = it.name
                loadWeatherData()
            }
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "需要位置权限才能获取当前位置天气", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        initViews()
        initServices()
        setupRecyclerView()
        setupClickListeners()
        loadWeatherData()
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rv_weather_forecast)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        tvCityName = findViewById(R.id.tv_city_name)
        btnLocation = findViewById(R.id.btn_location)
        btnCitySelect = findViewById(R.id.btn_city_select)
        tvCurrentTemp = findViewById(R.id.tv_current_temp)
        tvCurrentWeather = findViewById(R.id.tv_current_weather)
        ivCurrentWeatherIcon = findViewById(R.id.iv_current_weather_icon)
        tvAirQuality = findViewById(R.id.tv_air_quality)
        tvHumidity = findViewById(R.id.tv_humidity)
        tvPm25 = findViewById(R.id.tv_pm25)
        tvHealthTip = findViewById(R.id.tv_health_tip)
        rvWeatherForecast = findViewById(R.id.rv_weather_forecast)
        progressLoading = findViewById(R.id.progress_loading)
    }

    private fun initServices() {
        weatherService = WeatherService()
        locationUtils = LocationUtils(this)
    }

    private fun setupRecyclerView() {
        forecastAdapter = WeatherForecastAdapter(mutableListOf()) { forecast ->
            openWeatherDetail(forecast)
        }
        rvWeatherForecast.layoutManager = LinearLayoutManager(this)
        rvWeatherForecast.adapter = forecastAdapter
    }

    private fun setupClickListeners() {
        btnLocation.setOnClickListener {
            requestLocationAndGetWeather()
        }

        btnCitySelect.setOnClickListener {
            val intent = Intent(this, CitySelectActivity::class.java)
            citySelectLauncher.launch(intent)
        }
    }

    private fun loadWeatherData() {
        showLoading(true)
        weatherService.getWeather(currentCityCode,
            onSuccess = { weatherResponse ->
                runOnUiThread {
                    showLoading(false)
                    displayWeatherData(weatherResponse)
                }
            },
            onError = { error ->
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this@MainActivity, "获取天气数据失败: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun displayWeatherData(weatherResponse: WeatherResponse) {
        val data = weatherResponse.data
        
        // 显示当前天气
        tvCurrentTemp.text = "${data.wendu}℃"
        tvCurrentWeather.text = data.forecast[0].type
        
        val iconResId = WeatherIconUtils.getWeatherIcon(data.forecast[0].type)
        ivCurrentWeatherIcon.setImageResource(iconResId)
        
        // 显示空气质量等信息
        tvAirQuality.text = getAirQualityText(data.quality)
        tvHumidity.text = "${data.shidu}"
        tvPm25.text = data.pm25.toString()
        tvHealthTip.text = data.ganmao
        
        // 更新预报列表
        forecastAdapter.updateData(data.forecast)
    }

    private fun getAirQualityText(quality: String): String {
        return when {
            quality.contains("优") -> "优"
            quality.contains("良") -> "良"
            quality.contains("轻度") -> "轻度污染"
            quality.contains("中度") -> "中度污染"
            quality.contains("重度") -> "重度污染"
            quality.contains("严重") -> "严重污染"
            else -> quality
        }
    }

    private fun requestLocationAndGetWeather() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getCurrentLocation() {
        locationUtils.getCurrentLocation(
            onSuccess = { location ->
                // 根据经纬度获取城市代码（简化实现）
                val cityCode = locationUtils.getCityCodeByLocation(location.latitude, location.longitude)
                currentCityCode = cityCode
                currentCityName = "当前位置"
                tvCityName.text = currentCityName
                loadWeatherData()
            },
            onError = { error ->
                Toast.makeText(this, "获取位置失败: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun openWeatherDetail(forecast: Forecast) {
        val intent = Intent(this, WeatherDetailActivity::class.java)
        intent.putExtra(WeatherDetailActivity.EXTRA_FORECAST, forecast)
        startActivity(intent)
    }

    private fun showLoading(show: Boolean) {
        progressLoading.visibility = if (show) View.VISIBLE else View.GONE
    }
}