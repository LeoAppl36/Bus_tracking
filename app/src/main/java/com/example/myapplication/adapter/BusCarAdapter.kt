package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.models.BusCar

class BusCarAdapter(private val busCars: List<BusCar>) : RecyclerView.Adapter<BusCarAdapter.BusCarViewHolder>() {

    class BusCarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val busCarId: TextView = itemView.findViewById(R.id.busCarId)
        val busCarPosition: TextView = itemView.findViewById(R.id.busCarPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusCarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus_car, parent, false)
        return BusCarViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusCarViewHolder, position: Int) {
        val busCar = busCars[position]
        holder.busCarId.text = "${busCar.id}, ${busCar.license_plate}"
        holder.busCarPosition.text = "Lat: ${busCar.position.latitude}, Lon: ${busCar.position.longitude}"
    }

    override fun getItemCount(): Int {
        return busCars.size
    }
}