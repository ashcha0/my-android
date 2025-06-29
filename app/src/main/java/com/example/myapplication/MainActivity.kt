package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
    
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var tvCityName: TextView
    private lateinit var btnLocation: ImageButton
    private lateinit var btnCitySelect: ImageButton
    private lateinit var btnDiary: ImageButton
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
        btnDiary = findViewById(R.id.btn_diary)
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
        
        btnDiary.setOnClickListener {
            val intent = Intent(this, DiaryActivity::class.java)
            startActivity(intent)
        }
        
        // 音乐播放按钮点击事件
    findViewById<ImageButton>(R.id.btn_music_player).setOnClickListener {
        val intent = Intent(this, MusicPlayerActivity::class.java)
        startActivity(intent)
    }
    }

    private fun loadWeatherData() {
        Log.d(TAG, "开始加载天气数据，城市代码: $currentCityCode, 城市名称: $currentCityName")
        showLoading(true)
        
        weatherService.getWeather(currentCityCode,
            onSuccess = { weatherResponse ->
                Log.d(TAG, "天气数据获取成功: ${weatherResponse.cityInfo.city}")
                runOnUiThread {
                    showLoading(false)
                    // 更新城市名称显示
                    tvCityName.text = weatherResponse.cityInfo.city
                    displayWeatherData(weatherResponse)
                }
            },
            onError = { error ->
                Log.e(TAG, "天气数据获取失败: $error")
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this@MainActivity, "获取天气数据失败: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun displayWeatherData(weatherResponse: WeatherResponse) {
        val data = weatherResponse.data
        
        Log.d(TAG, "开始显示天气数据")
        Log.d(TAG, "当前温度: ${data.wendu}℃")
        Log.d(TAG, "湿度: ${data.shidu}")
        Log.d(TAG, "PM2.5: ${data.pm25}")
        Log.d(TAG, "空气质量: ${data.quality}")
        Log.d(TAG, "健康提示: ${data.ganmao}")
        Log.d(TAG, "预报数据条数: ${data.forecast.size}")
        
        // 显示当前天气
        if (data.forecast.isNotEmpty()) {
            val todayForecast = data.forecast[0]
            Log.d(TAG, "今日天气: ${todayForecast.type}, 温度范围: ${todayForecast.low} - ${todayForecast.high}")
            
            tvCurrentTemp.text = "${data.wendu}℃"
            tvCurrentWeather.text = todayForecast.type
            
            val iconResId = WeatherIconUtils.getWeatherIcon(todayForecast.type)
            ivCurrentWeatherIcon.setImageResource(iconResId)
        } else {
            Log.e(TAG, "预报数据为空，无法显示当前天气")
        }
        
        // 显示空气质量等信息
        tvAirQuality.text = getAirQualityText(data.quality)
        tvHumidity.text = "${data.shidu}"
        tvPm25.text = data.pm25.toString()
        tvHealthTip.text = data.ganmao
        
        // 更新预报列表 - 处理多日数据
        Log.d(TAG, "更新预报列表，共${data.forecast.size}天数据")
        data.forecast.forEachIndexed { index, forecast ->
            Log.d(TAG, "第${index + 1}天: ${forecast.date} ${forecast.type} ${forecast.low}-${forecast.high} 风力:${forecast.fl}")
        }
        
        forecastAdapter.updateData(data.forecast)
        Log.d(TAG, "天气数据显示完成")
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
        Log.d(TAG, "MainActivity开始获取当前位置")
        locationUtils.getCurrentLocation(
            onSuccess = { location ->
                Log.d(TAG, "MainActivity成功获取位置: 纬度=${location.latitude}, 经度=${location.longitude}")
                
                // 根据经纬度获取城市代码
                val cityCode = locationUtils.getCityCodeByLocation(location.latitude, location.longitude)
                Log.d(TAG, "获取到城市代码: $cityCode")
                
                // 更新当前城市信息
                val oldCityCode = currentCityCode
                val oldCityName = currentCityName
                currentCityCode = cityCode
                
                // 根据城市代码设置城市名称
                currentCityName = getCityNameByCode(cityCode)
                Log.d(TAG, "城市信息更新: $oldCityName($oldCityCode) -> $currentCityName($currentCityCode)")
                
                tvCityName.text = currentCityName
                loadWeatherData()
            },
            onError = { error ->
                Log.e(TAG, "MainActivity获取位置失败: $error")
                Toast.makeText(this, "获取位置失败: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    /**
     * 根据城市代码获取城市名称
     */
    private fun getCityNameByCode(cityCode: String): String {
        val cityNameMap = mapOf(
            "101010100" to "北京市",
            "101020100" to "上海市",
            "101030100" to "天津市",
            "101040100" to "重庆市",
            "101070101" to "沈阳市",
            "101070201" to "大连市",
            "101060101" to "长春市",
            "101050101" to "哈尔滨市",
            "101190101" to "南京市",
            "101210101" to "杭州市",
            "101120101" to "济南市",
            "101120201" to "青岛市",
            "101230101" to "福州市",
            "101230201" to "厦门市",
            "101240101" to "南昌市",
            "101180101" to "郑州市",
            "101200101" to "武汉市",
            "101250101" to "长沙市",
            "101280101" to "广州市",
            "101280601" to "深圳市",
            "101300101" to "南宁市",
            "101310101" to "海口市",
            "101270101" to "成都市",
            "101260101" to "贵阳市",
            "101290101" to "昆明市",
            "101140101" to "拉萨市",
            "101110101" to "西安市",
            "101160101" to "兰州市",
            "101150101" to "西宁市",
            "101170101" to "银川市",
            "101130101" to "乌鲁木齐市"
        )
        
        return cityNameMap[cityCode] ?: "未知城市"
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