package com.example.myapplication.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

/**
 * 定位工具类
 */
class LocationUtils(private val context: Context) {
    
    companion object {
        private const val TAG = "LocationUtils"
    }
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    /**
     * 检查定位权限
     */
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 请求定位权限
     */
    fun requestLocationPermission(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            requestCode
        )
    }
    
    /**
     * 获取当前位置
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onSuccess: (Location) -> Unit, onError: (String) -> Unit) {
        Log.d(TAG, "开始获取当前位置")
        
        if (!hasLocationPermission()) {
            Log.e(TAG, "缺少定位权限")
            onError("缺少定位权限")
            return
        }
        
        // 检查GPS是否开启
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        Log.d(TAG, "GPS状态: $gpsEnabled, 网络定位状态: $networkEnabled")
        
        if (!gpsEnabled && !networkEnabled) {
            Log.e(TAG, "GPS和网络定位都未开启")
            onError("请开启GPS或网络定位")
            return
        }
        
        val cancellationTokenSource = CancellationTokenSource()
        
        Log.d(TAG, "开始请求位置信息")
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                Log.d(TAG, "成功获取位置: 纬度=${location.latitude}, 经度=${location.longitude}, 精度=${location.accuracy}米")
                onSuccess(location)
            } else {
                Log.e(TAG, "位置信息为空")
                onError("无法获取位置信息")
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "获取位置失败: ${exception.message}", exception)
            onError("获取位置失败: ${exception.message}")
        }
    }
    
    /**
     * 根据经纬度获取城市代码
     */
    fun getCityCodeByLocation(latitude: Double, longitude: Double): String {
        Log.d(TAG, "开始根据经纬度获取城市代码: 纬度=$latitude, 经度=$longitude")
        
        // 首先尝试使用地理编码
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val cityName = address.locality ?: address.subAdminArea ?: address.adminArea
                Log.d(TAG, "地理编码结果: 城市=$cityName, 省份=${address.adminArea}, 国家=${address.countryName}")
                
                // 根据城市名称返回对应的城市代码
                val cityCode = getCityCodeByName(cityName)
                Log.d(TAG, "城市代码映射结果: $cityName -> $cityCode")
                return cityCode
            } else {
                Log.w(TAG, "地理编码未返回结果，尝试使用坐标范围判断")
            }
        } catch (e: Exception) {
            Log.e(TAG, "地理编码失败: ${e.message}，尝试使用坐标范围判断", e)
        }
        
        // 地理编码失败时，使用坐标范围判断城市
        val cityCode = getCityCodeByCoordinates(latitude, longitude)
        Log.d(TAG, "坐标范围判断结果: 纬度=$latitude, 经度=$longitude -> $cityCode")
        return cityCode
    }
    
    /**
     * 根据坐标范围判断城市代码（备用方案）
     */
    private fun getCityCodeByCoordinates(latitude: Double, longitude: Double): String {
        // 主要城市的坐标范围（大致范围）
        return when {
            // 沈阳市范围：纬度41.4-42.0，经度123.0-123.8
            latitude in 41.4..42.0 && longitude in 123.0..123.8 -> {
                Log.d(TAG, "坐标匹配沈阳市范围")
                "101070101"
            }
            // 北京市范围：纬度39.4-40.6，经度115.7-117.4
            latitude in 39.4..40.6 && longitude in 115.7..117.4 -> {
                Log.d(TAG, "坐标匹配北京市范围")
                "101010100"
            }
            // 上海市范围：纬度30.7-31.9，经度120.9-122.0
            latitude in 30.7..31.9 && longitude in 120.9..122.0 -> {
                Log.d(TAG, "坐标匹配上海市范围")
                "101020100"
            }
            // 广州市范围：纬度22.8-23.9，经度112.9..114.0
            latitude in 22.8..23.9 && longitude in 112.9..114.0 -> {
                Log.d(TAG, "坐标匹配广州市范围")
                "101280101"
            }
            // 深圳市范围：纬度22.4-22.8，经度113.7-114.6
            latitude in 22.4..22.8 && longitude in 113.7..114.6 -> {
                Log.d(TAG, "坐标匹配深圳市范围")
                "101280601"
            }
            // 天津市范围：纬度38.8-40.3，经度116.8-118.0
            latitude in 38.8..40.3 && longitude in 116.8..118.0 -> {
                Log.d(TAG, "坐标匹配天津市范围")
                "101030100"
            }
            // 重庆市范围：纬度28.1-32.2，经度105.2-110.1
            latitude in 28.1..32.2 && longitude in 105.2..110.1 -> {
                Log.d(TAG, "坐标匹配重庆市范围")
                "101040100"
            }
            // 大连市范围：纬度38.8-40.5，经度120.5-123.5
            latitude in 38.8..40.5 && longitude in 120.5..123.5 -> {
                Log.d(TAG, "坐标匹配大连市范围")
                "101070201"
            }
            // 杭州市范围：纬度29.8-30.6，经度119.7-120.7
            latitude in 29.8..30.6 && longitude in 119.7..120.7 -> {
                Log.d(TAG, "坐标匹配杭州市范围")
                "101210101"
            }
            // 南京市范围：纬度31.4-32.6，经度118.2-119.2
            latitude in 31.4..32.6 && longitude in 118.2..119.2 -> {
                Log.d(TAG, "坐标匹配南京市范围")
                "101190101"
            }
            // 武汉市范围：纬度29.9-31.4，经度113.7-115.0
            latitude in 29.9..31.4 && longitude in 113.7..115.0 -> {
                Log.d(TAG, "坐标匹配武汉市范围")
                "101200101"
            }
            // 成都市范围：纬度30.1-31.4，经度103.5-104.9
            latitude in 30.1..31.4 && longitude in 103.5..104.9 -> {
                Log.d(TAG, "坐标匹配成都市范围")
                "101270101"
            }
            // 西安市范围：纬度33.7-34.8，经度108.6-109.8
            latitude in 33.7..34.8 && longitude in 108.6..109.8 -> {
                Log.d(TAG, "坐标匹配西安市范围")
                "101110101"
            }
            else -> {
                Log.w(TAG, "坐标不在已知城市范围内，使用默认北京")
                "101010100" // 默认北京
            }
        }
    }
    
    /**
     * 根据城市名称获取城市代码
     */
    private fun getCityCodeByName(cityName: String?): String {
        if (cityName == null) return "101010100"
        
        // 城市名称到城市代码的映射
        val cityCodeMap = mapOf(
            "北京" to "101010100",
            "北京市" to "101010100",
            "上海" to "101020100",
            "上海市" to "101020100",
            "天津" to "101030100",
            "天津市" to "101030100",
            "重庆" to "101040100",
            "重庆市" to "101040100",
            "沈阳" to "101070101",
            "沈阳市" to "101070101",
            "大连" to "101070201",
            "大连市" to "101070201",
            "长春" to "101060101",
            "长春市" to "101060101",
            "哈尔滨" to "101050101",
            "哈尔滨市" to "101050101",
            "南京" to "101190101",
            "南京市" to "101190101",
            "杭州" to "101210101",
            "杭州市" to "101210101",
            "济南" to "101120101",
            "济南市" to "101120101",
            "青岛" to "101120201",
            "青岛市" to "101120201",
            "福州" to "101230101",
            "福州市" to "101230101",
            "厦门" to "101230201",
            "厦门市" to "101230201",
            "南昌" to "101240101",
            "南昌市" to "101240101",
            "郑州" to "101180101",
            "郑州市" to "101180101",
            "武汉" to "101200101",
            "武汉市" to "101200101",
            "长沙" to "101250101",
            "长沙市" to "101250101",
            "广州" to "101280101",
            "广州市" to "101280101",
            "深圳" to "101280601",
            "深圳市" to "101280601",
            "南宁" to "101300101",
            "南宁市" to "101300101",
            "海口" to "101310101",
            "海口市" to "101310101",
            "成都" to "101270101",
            "成都市" to "101270101",
            "贵阳" to "101260101",
            "贵阳市" to "101260101",
            "昆明" to "101290101",
            "昆明市" to "101290101",
            "拉萨" to "101140101",
            "拉萨市" to "101140101",
            "西安" to "101110101",
            "西安市" to "101110101",
            "兰州" to "101160101",
            "兰州市" to "101160101",
            "西宁" to "101150101",
            "西宁市" to "101150101",
            "银川" to "101170101",
            "银川市" to "101170101",
            "乌鲁木齐" to "101130101",
            "乌鲁木齐市" to "101130101"
        )
        
        return cityCodeMap[cityName] ?: "101010100" // 默认北京
    }
}