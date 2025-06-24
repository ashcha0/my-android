package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.CityAdapter
import com.example.myapplication.model.City

class CitySelectActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etSearchCity: EditText
    private lateinit var rvCityList: RecyclerView
    private lateinit var cityAdapter: CityAdapter
    
    private val allCities = listOf(
        City("北京市", "北京", "101010100"),
        City("上海市", "上海", "101020100"),
        City("广州市", "广东", "101280101"),
        City("深圳市", "广东", "101280601"),
        City("天津市", "天津", "101030100"),
        City("重庆市", "重庆", "101040100"),
        City("杭州市", "浙江", "101210101"),
        City("南京市", "江苏", "101190101"),
        City("苏州市", "江苏", "101190401"),
        City("成都市", "四川", "101270101"),
        City("武汉市", "湖北", "101200101"),
        City("西安市", "陕西", "101110101"),
        City("郑州市", "河南", "101180101"),
        City("济南市", "山东", "101120101"),
        City("青岛市", "山东", "101120201"),
        City("大连市", "辽宁", "101070201"),
        City("沈阳市", "辽宁", "101070101"),
        City("长春市", "吉林", "101060101"),
        City("哈尔滨市", "黑龙江", "101050101"),
        City("昆明市", "云南", "101290101"),
        City("贵阳市", "贵州", "101260101"),
        City("南宁市", "广西", "101300101"),
        City("海口市", "海南", "101310101"),
        City("三亚市", "海南", "101310201"),
        City("拉萨市", "西藏", "101140101"),
        City("银川市", "宁夏", "101170101"),
        City("乌鲁木齐市", "新疆", "101130101"),
        City("兰州市", "甘肃", "101160101"),
        City("西宁市", "青海", "101150101"),
        City("呼和浩特市", "内蒙古", "101080101")
    )
    
    private var filteredCities = allCities.toMutableList()

    companion object {
        const val EXTRA_SELECTED_CITY = "extra_selected_city"
        const val REQUEST_CODE_CITY_SELECT = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_select)

        initViews()
        setupRecyclerView()
        setupClickListeners()
        setupSearch()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        etSearchCity = findViewById(R.id.et_search_city)
        rvCityList = findViewById(R.id.rv_city_list)
    }

    private fun setupRecyclerView() {
        cityAdapter = CityAdapter(filteredCities) { city ->
            onCitySelected(city)
        }
        rvCityList.layoutManager = LinearLayoutManager(this)
        rvCityList.adapter = cityAdapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupSearch() {
        etSearchCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCities(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterCities(query: String) {
        filteredCities.clear()
        if (query.isEmpty()) {
            filteredCities.addAll(allCities)
        } else {
            filteredCities.addAll(
                allCities.filter { city ->
                    city.name.contains(query, ignoreCase = true) ||
                    city.parent.contains(query, ignoreCase = true)
                }
            )
        }
        cityAdapter.notifyDataSetChanged()
    }

    private fun onCitySelected(city: City) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_SELECTED_CITY, city)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}