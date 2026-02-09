package com.gnd.flashtracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

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

        if(!listAllFlashes){
            holder.rvLatitude.setText("Lightning detected "+"%.2f".format(Locale.US, currentData.distance) + " km away.")

        }else{
            holder.rvLatitude.setText("Lightning detected LAT:${currentData.latitude}")
        }
        holder.itemView.setOnClickListener { onItemClick(currentData) }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class RVViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvLatitude: TextView=itemView.findViewById(R.id.distanceView)
    }
}