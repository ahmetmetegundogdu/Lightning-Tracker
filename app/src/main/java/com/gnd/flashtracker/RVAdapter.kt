package com.gnd.flashtracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import android.content.Context
import android.location.Geocoder
import android.util.Log

class RVAdapter(var itemList: MutableList<Data>, val listAllFlashes: Boolean, private val onItemClick:(Data)-> Unit): RecyclerView.Adapter<RVAdapter.RVViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RVViewHolder {
        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.item_layout,parent,false)
        return RVViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: RVViewHolder,
        position: Int
    ) {
        val currentData=itemList[position]

        holder.itemView.isSelected = currentData.connectionStatus

        if(!listAllFlashes){

            val region=getAddress(holder.itemView.context,currentData.latitude.toDouble(),currentData.longitude.toDouble())
            holder.rvAddress.setText("Address: $region ")

            holder.rvDistance.setText("Distance: "+"%.2f".format(Locale.US, currentData.distance) + " km")

        }else{

            val region=getAddress(holder.itemView.context,currentData.latitude.toDouble(),currentData.longitude.toDouble())
            holder.rvAddress.setText("Address: $region ")

            holder.rvDistance.setText("Lat: %.4f  Lon: %.4f".format(
                currentData.latitude.toDouble(),
                currentData.longitude.toDouble())
            )
        }
        holder.itemView.setOnClickListener { onItemClick(currentData) }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class RVViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvDistance: TextView=itemView.findViewById(R.id.distanceView)
        val rvAddress:TextView=itemView.findViewById(R.id.addressView)

    }

    fun getAddress(context: Context,lat: Double,lon: Double): String {
        val geocoder= Geocoder(context, Locale.ENGLISH)
        try {
            val addressList=geocoder.getFromLocation(lat,lon,1)
            if(!addressList.isNullOrEmpty()){
                val address=addressList[0]
                val x=address.getAddressLine(0)
                val cityName=address.adminArea
                val townName=address.subAdminArea
                val addressInfo=cityName+"/"+townName
                return x
            }else{
                val region = when {
                    (lat in 40.0..47.0) && (lon in 27.0..42.0) -> "Black Sea"
                    (lat in 30.0..46.0) && (lon in -6.0..36.0) -> "Mediterranean Sea"
                    (lat in -66.0..66.0) && (lon >= 120.0 || lon <= -80.0) -> "Pacific Ocean"
                    (lat in -60.0..70.0) && (lon in -70.0..20.0) -> "Atlantic Ocean"
                    (lat in -60.0..25.0) && (lon in 20.0..120.0) -> "Indian Ocean"
                    (lat in -90.0..-60.0) -> "Southern Ocean"
                    (lat in 60.0..90.0) -> "Arctic Ocean"
                    else -> "Land or Other Water Body"
                }
                return region
            }
        }catch(e: Exception) {
            Log.d("Error:",e.toString())

        }
        return ""
    }
}