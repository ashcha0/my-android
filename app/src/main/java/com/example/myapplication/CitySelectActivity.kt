package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.CityAdapter
import com.example.myapplication.model.City

class CitySelectActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CitySelectActivity"
        const val EXTRA_SELECTED_CITY = "extra_selected_city"
        const val REQUEST_CODE_CITY_SELECT = 1001
    }

    private lateinit var btnBack: ImageButton
    private lateinit var etSearchCity: EditText
    private lateinit var rvCityList: RecyclerView
    private lateinit var cityAdapter: CityAdapter
    
    // 根据城市代码文档构建的完整城市列表
    private val allCities = listOf(
        // 北京
        City("北京", "北京", "101010100"),
        City("朝阳", "北京", "101010300"),
        City("海淀", "北京", "101010200"),
        City("顺义", "北京", "101010400"),
        City("怀柔", "北京", "101010500"),
        City("通州", "北京", "101010600"),
        City("昌平", "北京", "101010700"),
        City("延庆", "北京", "101010800"),
        City("丰台", "北京", "101010900"),
        City("石景山", "北京", "101011000"),
        City("大兴", "北京", "101011100"),
        City("房山", "北京", "101011200"),
        City("密云", "北京", "101011300"),
        City("门头沟", "北京", "101011400"),
        City("平谷", "北京", "101011500"),
        
        // 天津
        City("天津", "天津市", "101030100"),
        City("武清", "天津市", "101030200"),
        City("宝坻", "天津市", "101030300"),
        City("东丽", "天津市", "101030400"),
        City("西青", "天津市", "101030500"),
        City("北辰", "天津市", "101030600"),
        City("宁河", "天津市", "101030700"),
        City("汉沽", "天津市", "101030800"),
        City("静海", "天津市", "101030900"),
        City("津南", "天津市", "101031000"),
        City("塘沽", "天津市", "101031100"),
        City("大港", "天津市", "101031200"),
        City("蓟县", "天津市", "101031400"),
        
        // 上海
        City("上海", "上海", "101020100"),
        City("闵行", "上海", "101020200"),
        City("宝山", "上海", "101020300"),
        City("嘉定", "上海", "101020500"),
        City("南汇", "上海", "101020600"),
        City("金山", "上海", "101020700"),
        City("青浦", "上海", "101020800"),
        City("松江", "上海", "101020900"),
        City("奉贤", "上海", "101021000"),
        City("崇明", "上海", "101021100"),
        City("徐家汇", "上海", "101021200"),
        City("浦东", "上海", "101021300"),
        
        // 河北
        City("石家庄", "河北", "101090101"),
        City("保定", "河北", "101090201"),
        City("张家口", "河北", "101090301"),
        City("承德", "河北", "101090402"),
        City("唐山", "河北", "101090501"),
        City("廊坊", "河北", "101090601"),
        City("沧州", "河北", "101090701"),
        City("衡水", "河北", "101090801"),
        City("邢台", "河北", "101090901"),
        City("邯郸", "河北", "101091001"),
        City("秦皇岛", "河北", "101091101"),
        
        // 河南
        City("郑州", "河南", "101180101"),
        City("安阳", "河南", "101180201"),
        City("新乡", "河南", "101180301"),
        City("许昌", "河南", "101180401"),
        City("平顶山", "河南", "101180501"),
        City("信阳", "河南", "101180601"),
        City("南阳", "河南", "101180701"),
        City("开封", "河南", "101180801"),
        City("洛阳", "河南", "101180901"),
        City("商丘", "河南", "101181001"),
        City("焦作", "河南", "101181101"),
        City("鹤壁", "河南", "101181201"),
        City("濮阳", "河南", "101181301"),
        City("周口", "河南", "101181401"),
        City("漯河", "河南", "101181501"),
        City("驻马店", "河南", "101181601"),
        City("三门峡", "河南", "101181701"),
        City("济源", "河南", "101181801"),
        
        // 安徽
        City("合肥", "安徽", "101220101"),
        City("蚌埠", "安徽", "101220201"),
        City("芜湖", "安徽", "101220301"),
        City("淮南", "安徽", "101220401"),
        City("马鞍山", "安徽", "101220501"),
        City("安庆", "安徽", "101220601"),
        City("宿州", "安徽", "101220701"),
        City("阜阳", "安徽", "101220801"),
        City("亳州", "安徽", "101220901"),
        City("黄山", "安徽", "101221001"),
        City("滁州", "安徽", "101221101"),
        City("淮北", "安徽", "101221201"),
        City("铜陵", "安徽", "101221301"),
        City("宣城", "安徽", "101221401"),
        City("六安", "安徽", "101221501"),
        City("巢湖", "安徽", "101221601"),
        City("池州", "安徽", "101221701"),
        
        // 浙江
        City("杭州", "浙江", "101210101"),
        City("湖州", "浙江", "101210201"),
        City("嘉兴", "浙江", "101210301"),
        City("宁波", "浙江", "101210401"),
        City("绍兴", "浙江", "101210501"),
        City("台州", "浙江", "101210601"),
        City("温州", "浙江", "101210701"),
        City("丽水", "浙江", "101210801"),
        City("金华", "浙江", "101210901"),
        City("衢州", "浙江", "101211001"),
        City("舟山", "浙江", "101211101"),
        
        // 重庆
        City("重庆", "重庆", "101040100"),
        City("永川", "重庆", "101040200"),
        City("合川", "重庆", "101040300"),
        City("南川", "重庆", "101040400"),
        City("江津", "重庆", "101040500"),
        City("万盛", "重庆", "101040600"),
        City("渝北", "重庆", "101040700"),
        City("北碚", "重庆", "101040800"),
        City("巴南", "重庆", "101040900"),
        City("长寿", "重庆", "101041000"),
        City("黔江", "重庆", "101041100"),
        City("万州天城", "重庆", "101041200"),
        City("万州龙宝", "重庆", "101041300"),
        City("涪陵", "重庆", "101041400"),
        
        // 福建
        City("福州", "福建", "101230101"),
        City("厦门", "福建", "101230201"),
        City("宁德", "福建", "101230301"),
        City("莆田", "福建", "101230401"),
        City("泉州", "福建", "101230501"),
        City("漳州", "福建", "101230601"),
        City("龙岩", "福建", "101230701"),
        City("三明", "福建", "101230801"),
        City("南平", "福建", "101230901"),
        
        // 甘肃
        City("兰州", "甘肃", "101160101"),
        City("定西", "甘肃", "101160201"),
        City("平凉", "甘肃", "101160301"),
        City("庆阳", "甘肃", "101160401"),
        City("武威", "甘肃", "101160501"),
        City("金昌", "甘肃", "101160601"),
        City("张掖", "甘肃", "101160701"),
        City("酒泉", "甘肃", "101160801"),
        City("天水", "甘肃", "101160901"),
        City("武都", "甘肃", "101161001"),
        City("临夏", "甘肃", "101161101"),
        City("合作", "甘肃", "101161201"),
        City("白银", "甘肃", "101161301"),
        City("嘉峪关", "甘肃", "101161401"),
        
        // 广东
        City("广州", "广东", "101280101"),
        City("韶关", "广东", "101280201"),
        City("惠州", "广东", "101280301"),
        City("梅州", "广东", "101280401"),
        City("汕头", "广东", "101280501"),
        City("深圳", "广东", "101280601"),
        City("珠海", "广东", "101280701"),
        City("佛山", "广东", "101280800"),
        City("肇庆", "广东", "101280901"),
        City("湛江", "广东", "101281001"),
        City("江门", "广东", "101281101"),
        City("河源", "广东", "101281201"),
        City("清远", "广东", "101281301"),
        City("云浮", "广东", "101281401"),
        City("潮州", "广东", "101281501"),
        City("东莞", "广东", "101281601"),
        City("中山", "广东", "101281701"),
        City("阳江", "广东", "101281801"),
        City("揭阳", "广东", "101281901"),
        City("茂名", "广东", "101282001"),
        City("汕尾", "广东", "101282101"),
        
        // 广西
        City("南宁", "广西", "101300101"),
        City("崇左", "广西", "101300201"),
        City("柳州", "广西", "101300301"),
        City("来宾", "广西", "101300401"),
        City("桂林", "广西", "101300501"),
        City("梧州", "广西", "101300601"),
        City("贺州", "广西", "101300701"),
        City("贵港", "广西", "101300801"),
        City("玉林", "广西", "101300901"),
        City("百色", "广西", "101301001"),
        City("钦州", "广西", "101301101"),
        City("河池", "广西", "101301201"),
        City("北海", "广西", "101301301"),
        City("防城港", "广西", "101301401"),
        
        // 贵州
        City("贵阳", "贵州", "101260101"),
        City("遵义", "贵州", "101260201"),
        City("安顺", "贵州", "101260301"),
        City("都匀", "贵州", "101260401"),
        City("凯里", "贵州", "101260501"),
        City("铜仁", "贵州", "101260601"),
        City("毕节", "贵州", "101260701"),
        City("六盘水", "贵州", "101260801"),
        City("兴义", "贵州", "101260906"),
        
        // 云南
        City("昆明", "云南", "101290101"),
        City("大理", "云南", "101290201"),
        City("红河", "云南", "101290301"),
        City("曲靖", "云南", "101290401"),
        City("保山", "云南", "101290501"),
        City("文山", "云南", "101290601"),
        City("玉溪", "云南", "101290701"),
        City("楚雄", "云南", "101290801"),
        City("普洱", "云南", "101290901"),
        City("昭通", "云南", "101291001"),
        City("临沧", "云南", "101291101"),
        City("怒江", "云南", "101291201"),
        City("香格里拉", "云南", "101291301"),
        City("丽江", "云南", "101291401"),
        City("德宏", "云南", "101291501"),
        City("景洪", "云南", "101291601"),
        
        // 内蒙古
        City("呼和浩特", "内蒙古", "101080101"),
        City("包头", "内蒙古", "101080201"),
        City("乌海", "内蒙古", "101080301"),
        City("集宁", "内蒙古", "101080401"),
        City("通辽", "内蒙古", "101080501"),
        City("赤峰", "内蒙古", "101080601"),
        City("鄂尔多斯", "内蒙古", "101080701"),
        City("临河", "内蒙古", "101080801"),
        City("锡林浩特", "内蒙古", "101080901"),
        City("呼伦贝尔", "内蒙古", "101081000"),
        City("乌兰浩特", "内蒙古", "101081101"),
        City("阿拉善左旗", "内蒙古", "101081201"),
        
        // 江西
        City("南昌", "江西", "101240101"),
        City("九江", "江西", "101240201"),
        City("上饶", "江西", "101240301"),
        City("抚州", "江西", "101240401"),
        City("宜春", "江西", "101240501"),
        City("吉安", "江西", "101240601"),
        City("赣州", "江西", "101240701"),
        City("景德镇", "江西", "101240801"),
        City("萍乡", "江西", "101240901"),
        City("新余", "江西", "101241001"),
        City("鹰潭", "江西", "101241101"),
        
        // 湖北
        City("武汉", "湖北", "101200101"),
        City("襄樊", "湖北", "101200201"),
        City("鄂州", "湖北", "101200301"),
        City("孝感", "湖北", "101200401"),
        City("黄冈", "湖北", "101200501"),
        City("黄石", "湖北", "101200601"),
        City("咸宁", "湖北", "101200701"),
        City("荆州", "湖北", "101200801"),
        City("宜昌", "湖北", "101200901"),
        City("恩施", "湖北", "101201001"),
        City("十堰", "湖北", "101201101"),
        City("神农架", "湖北", "101201201"),
        City("随州", "湖北", "101201301"),
        City("荆门", "湖北", "101201401"),
        City("天门", "湖北", "101201501"),
        City("仙桃", "湖北", "101201601"),
        City("潜江", "湖北", "101201701"),
        
        // 四川
        City("成都", "四川", "101270101"),
        City("攀枝花", "四川", "101270201"),
        City("自贡", "四川", "101270301"),
        City("绵阳", "四川", "101270401"),
        City("南充", "四川", "101270501"),
        City("达州", "四川", "101270601"),
        City("遂宁", "四川", "101270701"),
        City("广安", "四川", "101270801"),
        City("巴中", "四川", "101270901"),
        City("泸州", "四川", "101271001"),
        City("宜宾", "四川", "101271101"),
        City("内江", "四川", "101271201"),
        City("资阳", "四川", "101271301"),
        City("乐山", "四川", "101271401"),
        City("眉山", "四川", "101271501"),
        City("凉山", "四川", "101271601"),
        City("雅安", "四川", "101271701"),
        City("甘孜", "四川", "101271801"),
        City("阿坝", "四川", "101271901"),
        City("德阳", "四川", "101272001"),
        City("广元", "四川", "101272101"),
        
        // 宁夏
        City("银川", "宁夏", "101170101"),
        City("石嘴山", "宁夏", "101170201"),
        City("吴忠", "宁夏", "101170301"),
        City("固原", "宁夏", "101170401"),
        City("中卫", "宁夏", "101170501"),
        
        // 青海
        City("西宁", "青海省", "101150101"),
        City("海东", "青海省", "101150201"),
        City("黄南", "青海省", "101150301"),
        City("海南", "青海省", "101150401"),
        City("果洛", "青海省", "101150501"),
        City("玉树", "青海省", "101150601"),
        City("海西", "青海省", "101150701"),
        City("海北", "青海省", "101150801"),
        
        // 山东
        City("济南", "山东", "101120101"),
        City("青岛", "山东", "101120201"),
        City("淄博", "山东", "101120301"),
        City("德州", "山东", "101120401"),
        City("烟台", "山东", "101120501"),
        City("潍坊", "山东", "101120601"),
        City("济宁", "山东", "101120701"),
        City("泰安", "山东", "101120801"),
        City("临沂", "山东", "101120901"),
        City("菏泽", "山东", "101121001"),
        City("滨州", "山东", "101121101"),
        City("东营", "山东", "101121201"),
        City("威海", "山东", "101121301"),
        City("枣庄", "山东", "101121401"),
        City("日照", "山东", "101121501"),
        City("莱芜", "山东", "101121601"),
        City("聊城", "山东", "101121701")
    )
    
    private var filteredCities = allCities.toMutableList()

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
        Log.d(TAG, "选择城市: ${city.name}, 代码: ${city.code}")
        val resultIntent = Intent().apply {
            putExtra(EXTRA_SELECTED_CITY, city)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}