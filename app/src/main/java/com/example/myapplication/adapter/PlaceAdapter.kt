package com.example.myapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.google.android.libraries.places.api.model.AutocompletePrediction
class PlaceAdapter(private val predictionsList: List<AutocompletePrediction>, private val placeClickListener: PlaceClickListener) :
    RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_item, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val prediction = predictionsList[position]
        holder.placeName.text = prediction.getPrimaryText(null).toString()
        holder.placeItemLayout.setOnClickListener{
            Log.d("PlaceItem======>", prediction.toString())
            placeClickListener.onPlaceClick(prediction)
            // Hide the keyboard
            (holder.itemView.context as MainActivity).hideKeyboard(holder.itemView)
        }
    }

    override fun getItemCount(): Int {
        return predictionsList.size
    }

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var placeName: TextView = itemView.findViewById(R.id.placeName)
        var placeItemLayout: LinearLayout = itemView.findViewById(R.id.placeItemLayout)
    }

    interface PlaceClickListener {
        fun onPlaceClick(prediction: AutocompletePrediction)
    }
}
