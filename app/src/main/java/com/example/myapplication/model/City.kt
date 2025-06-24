package com.example.myapplication.model

import java.io.Serializable

data class City(
    val name: String,      // 城市名称，如"北京市"
    val parent: String,    // 所属省份，如"北京"
    val code: String       // 城市代码，如"101010100"
) : Serializable