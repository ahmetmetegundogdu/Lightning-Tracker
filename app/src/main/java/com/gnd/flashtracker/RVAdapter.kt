package com.gnd.flashtracker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import android.content.Context
import android.location.Geocoder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RVAdapter(private val scope: CoroutineScope,
                var itemList: MutableList<Data>,
                val listAllFlashes: Boolean,
                private val onItemClick:(Data)-> Unit): RecyclerView.Adapter<RVAdapter.RVViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RVViewHolder {
        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.item_layout,parent,false)
        return RVViewHolder(itemView)
    }
    class RVViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvDistance: TextView=itemView.findViewById(R.id.distanceView)
        val rvAddress:TextView=itemView.findViewById(R.id.addressView)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RVViewHolder, position: Int) {
        val currentData=itemList[position]

        holder.itemView.isSelected = currentData.connectionStatus
        holder.rvAddress.text = "Loading..."

        val currentPosition = holder.bindingAdapterPosition
        holder.itemView.tag = currentPosition

        if(!listAllFlashes){

            scope.launch {
                val region=getAddress(holder.itemView.context,currentData.latitude.toDouble(),currentData.longitude.toDouble())
                holder.rvAddress.text = "Address: $region "
                if (holder.itemView.tag == currentPosition) {
                    holder.rvAddress.text = region
                }
            }
            holder.rvDistance.text = "Distance: "+"%.2f".format(Locale.US, currentData.distance) + " km"
        }
        else{

            scope.launch {
                val region=getAddress(holder.itemView.context,currentData.latitude.toDouble(),currentData.longitude.toDouble())
                holder.rvAddress.text = "Address: $region "
                if (holder.itemView.tag == currentPosition) {
                    holder.rvAddress.text = region
                }
            }

            holder.rvDistance.text = "Lat: %.4f  Lon: %.4f".format(
                currentData.latitude.toDouble(),
                currentData.longitude.toDouble())
        }
        holder.itemView.setOnClickListener { onItemClick(currentData) }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
    suspend fun getAddress(context: Context, lat: Double, lon: Double): String {

        // I/O thread'ine geçiş yap (Arka planda çalıştır)
        return withContext(Dispatchers.IO) {
            val geocoder = Geocoder(context, Locale.ENGLISH)
            var resultAddress: String

            try {
                // Bu bloklayıcı işlem artık UI'ı dondurmaz
                @Suppress("DEPRECATION") val addressList = geocoder.getFromLocation(lat, lon, 1)

                if (!addressList.isNullOrEmpty()) {
                    val address = addressList[0]
                    // index=0 genellikle tam adresi verir, garanti olsun diye kontrol edilebilir
                    resultAddress = address.getAddressLine(0) ?: "Bilinmeyen Konum"
                } else {
                    // Senin manuel fallback kodun buraya gelir
                    resultAddress = getRegionFallback(lat, lon)
                }
            } catch (e: Exception) {
                Log.e("GeocoderError", e.toString())
                // Hata durumunda da fallback çalışsın
                resultAddress = getRegionFallback(lat, lon)
            }

            // Sonucu döndür (return@withContext kullanımı)
            resultAddress
        }
    }
    fun getRegionFallback(lat: Double, lon: Double): String {
        return  when {
            (lat in 40.0..47.0) && (lon in 27.0..42.0) -> "Black Sea"
            (lat in 30.0..46.0) && (lon in -6.0..36.0) -> "Mediterranean Sea"
            (lat in -66.0..66.0) && (lon >= 120.0 || lon <= -80.0) -> "Pacific Ocean"
            (lat in -60.0..70.0) && (lon in -70.0..20.0) -> "Atlantic Ocean"
            (lat in -60.0..25.0) && (lon in 20.0..120.0) -> "Indian Ocean"
            (lat in -90.0..-60.0) -> "Southern Ocean"
            (lat in 60.0..90.0) -> "Arctic Ocean"
            else -> "Land or Other Water Body"
        }
    }
}