package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.City

class CityAdapter(
    private val cities: List<City>,
    private val onCityClickListener: (City) -> Unit
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_city, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = cities[position]
        holder.bind(city)
    }

    override fun getItemCount(): Int = cities.size

    inner class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCityName: TextView = itemView.findViewById(R.id.tv_city_name)
        private val tvCityParent: TextView = itemView.findViewById(R.id.tv_city_parent)
        private val tvCityCode: TextView = itemView.findViewById(R.id.tv_city_code)

        fun bind(city: City) {
            tvCityName.text = city.name
            tvCityParent.text = city.parent
            tvCityCode.text = city.code

            itemView.setOnClickListener {
                onCityClickListener(city)
            }
        }
    }
}