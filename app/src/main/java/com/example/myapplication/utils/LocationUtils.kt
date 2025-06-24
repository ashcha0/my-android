package com.example.myapplication.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/**
 * 定位工具类
 */
class LocationUtils(private val context: Context) {
    
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
        if (!hasLocationPermission()) {
            onError("缺少定位权限")
            return
        }
        
        // 检查GPS是否开启
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
            !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            onError("请开启GPS或网络定位")
            return
        }
        
        val cancellationTokenSource = CancellationTokenSource()
        
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                onSuccess(location)
            } else {
                onError("无法获取位置信息")
            }
        }.addOnFailureListener { exception ->
            onError("获取位置失败: ${exception.message}")
        }
    }
    
    /**
     * 根据经纬度获取城市代码（简化版，实际应用中需要使用地理编码服务）
     * 这里仅做示例，返回北京的城市代码
     */
    fun getCityCodeByLocation(latitude: Double, longitude: Double): String {
        // 实际应用中，应该使用地理编码服务根据经纬度获取城市信息
        // 这里简化处理，默认返回北京
        return "101010100"
    }
}