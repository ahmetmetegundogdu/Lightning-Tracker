package com.gnd.flashtracker

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.util.Locale
import kotlin.math.pow

class ligthnings_RV : AppCompatActivity() {

    lateinit var latitudeText: TextView
    lateinit var longitudeText: TextView
    private var firstCon: Boolean = true
    private lateinit var recyclerView: RecyclerView
    private var range: Int = 1000000
    private var listAllFlashes: Boolean= true
    private lateinit var dataList: MutableList<Data>


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ligthnings_rv)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView=findViewById(R.id.recyclerview)
        recyclerView.layoutManager= LinearLayoutManager(this)
        range= intent.getIntExtra("range",1000000)
        listAllFlashes=intent.getBooleanExtra("listAllFlashes",false)

        dataList=mutableListOf()
        val adapter= RVAdapter(dataList,listAllFlashes){data ->
            val lat=data.latitude
            val lon=data.longitude
            val label="Flash Point"
            val uri= Uri.parse("geo:$lat,$lon?q=$lat,$lon($label)")
            val mapsIntent= Intent(Intent.ACTION_VIEW,uri)
            if (mapsIntent.resolveActivity(packageManager)!=null){
                mapsIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapsIntent)
            }
        }

        recyclerView.adapter=adapter
        recyclerView.itemAnimator = SlideInItemAnimator()
        Log.d("Info","RecyclerView kuruldu.")

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                val text = intent.getStringExtra("text") ?: ""
                val currentTime = intent.getLongExtra("currentTime",0)

                try {
                    val decodedData = decode(text)
                    val jsonObject = JSONObject(decodedData)
                    val lat = jsonObject.optDouble("lat", 0.0)
                    val lon = jsonObject.optDouble("lon", 0.0)
                    val delay = jsonObject.optDouble("delay",-1.0)
                    val time = jsonObject.optLong("time",0)

                    Log.d("Websocket reconnect status:", "$newCon")

                    if (firstCon){
                        if (newCon){
                            if (currentTime > time - delay*1000000000){
                                //kırmızı yazdır
                                runOnUiThread {
                                    Log.d("WebSocket", "Yıldırım Düştü: $lat, $lon")
                                    if(!listAllFlashes){
                                        val distance=calculateDistance(lat,lon)
                                        Log.d("Distance:",distance.toString())
                                        Log.d("Range:",range.toString())
                                        if(range.toDouble()>=distance){
                                            val data= Data(lat.toString(),lon.toString(),newCon&&firstCon,distance)
                                            dataList.add(0, data)
                                            recyclerView.adapter?.notifyItemInserted(0)
                                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                            val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                                            if (firstVisibleItemPosition == 0 || firstVisibleItemPosition == -1) {
                                                recyclerView.scrollToPosition(0)
                                            }
                                        }
                                    }
                                    else{
                                        val data= Data(lat.toString(),lon.toString(),newCon)
                                        dataList.add(0, data)
                                        recyclerView.adapter?.notifyItemInserted(0)
                                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                        val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                                        if (firstVisibleItemPosition == 0 || firstVisibleItemPosition == -1) {
                                            recyclerView.scrollToPosition(0)
                                        }
                                    }
                                }
                            }
                            else{
                                newCon = false
                                firstCon = false
                                //mavi yazdır
                                runOnUiThread {
                                    Log.d("WebSocket", "Yıldırım Düştü: $lat, $lon")
                                    if(!listAllFlashes){
                                        val distance=calculateDistance(lat,lon)
                                        Log.d("Distance:",distance.toString())
                                        Log.d("Range:",range.toString())
                                        if(range.toDouble()>=distance){
                                            val data= Data(lat.toString(),lon.toString(),newCon&&firstCon,distance)
                                            dataList.add(0, data)
                                            recyclerView.adapter?.notifyItemInserted(0)
                                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                            val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                                            if (firstVisibleItemPosition == 0 || firstVisibleItemPosition == -1) {
                                                recyclerView.scrollToPosition(0)
                                            }
                                        }
                                    }
                                    else{
                                        val data= Data(lat.toString(),lon.toString(),newCon)
                                        dataList.add(0, data)
                                        recyclerView.adapter?.notifyItemInserted(0)
                                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                        val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                                        if (firstVisibleItemPosition == 0 || firstVisibleItemPosition == -1) {
                                            recyclerView.scrollToPosition(0)
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            //mavi yazdır
                            runOnUiThread {
                                Log.d("WebSocket", "Yıldırım Düştü: $lat, $lon")
                                if(!listAllFlashes){
                                    val distance=calculateDistance(lat,lon)
                                    Log.d("Distance:",distance.toString())
                                    Log.d("Range:",range.toString())
                                    if(range.toDouble()>=distance){
                                        val data= Data(lat.toString(),lon.toString(),newCon&&firstCon,distance)
                                        dataList.add(0, data)
                                        recyclerView.adapter?.notifyItemInserted(0)
                                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                        val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                                        if (firstVisibleItemPosition == 0 || firstVisibleItemPosition == -1) {
                                            recyclerView.scrollToPosition(0)
                                        }
                                    }
                                }
                                else{
                                    val data= Data(lat.toString(),lon.toString(),newCon)
                                    dataList.add(0, data)
                                    recyclerView.adapter?.notifyItemInserted(0)
                                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                    val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                                    if (firstVisibleItemPosition == 0 || firstVisibleItemPosition == -1) {
                                        recyclerView.scrollToPosition(0)
                                    }
                                }
                            }
                        }
                    }
                    else{
                        if (newCon){
                            if (currentTime > (time - delay*1000000000)){
                                //yazdırma
                                //bura boş komutanım
                            }
                            else{
                                newCon = false
                                firstCon = false
                                //mavi yazdır
                                runOnUiThread {
                                    Log.d("WebSocket", "Yıldırım Düştü: $lat, $lon")
                                    if(!listAllFlashes){
                                        val distance=calculateDistance(lat,lon)
                                        Log.d("Distance:",distance.toString())
                                        Log.d("Range:",range.toString())
                                        if(range.toDouble()>=distance){
                                            val data= Data(lat.toString(),lon.toString(),newCon&&firstCon,distance)
                                            dataList.add(0, data)
                                            recyclerView.adapter?.notifyItemInserted(0)
                                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                            val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                                            if (firstVisibleItemPosition == 0 || firstVisibleItemPosition == -1) {
                                                recyclerView.scrollToPosition(0)
                                            }
                                        }
                                    }else{
                                        val data= Data(lat.toString(),lon.toString(),newCon)
                                        dataList.add(0, data)
                                        recyclerView.adapter?.notifyItemInserted(0)
                                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                        val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                                        if (firstVisibleItemPosition == 0 || firstVisibleItemPosition == -1) {
                                            recyclerView.scrollToPosition(0)
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            //mavi yazdır
                            runOnUiThread {
                                Log.d("WebSocket", "Yıldırım Düştü: $lat, $lon")
                                if(!listAllFlashes){
                                    val distance=calculateDistance(lat,lon)
                                    Log.d("Distance:",distance.toString())
                                    Log.d("Range:",range.toString())
                                    if(range.toDouble()>=distance){
                                        val data= Data(lat.toString(),lon.toString(),newCon&&firstCon,distance)
                                        dataList.add(0, data)
                                        recyclerView.adapter?.notifyItemInserted(0)
                                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                        val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                                        if (firstVisibleItemPosition == 0 || firstVisibleItemPosition == -1) {
                                            recyclerView.scrollToPosition(0)
                                        }
                                    }
                                }else{
                                    val data= Data(lat.toString(),lon.toString(),newCon)
                                    dataList.add(0, data)
                                    recyclerView.adapter?.notifyItemInserted(0)
                                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                    val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                                    if (firstVisibleItemPosition == 0 || firstVisibleItemPosition == -1) {
                                        recyclerView.scrollToPosition(0)
                                    }
                                }
                            }
                        }
                    }
                }
                catch (e: Exception) {
                    Log.e(":|", "program yarra yedi")
                }
            }
        }

        val filter = IntentFilter("WS_MESSAGE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }

        onBackPressedDispatcher.addCallback(this) {
            unregisterReceiver(receiver)
            isEnabled = false
            onBackPressedDispatcher.onBackPressed()
        }
    }


    fun getAddress(context: Context,latitude: Double,longitude: Double): String {
        val geocoder= Geocoder(context, Locale.ENGLISH)
        try {
            val addressList=geocoder.getFromLocation(latitude,longitude,5)
            if(!addressList.isNullOrEmpty()){
                val address=addressList[0]
                val x=address.getAddressLine(0)
                val cityName=address.adminArea
                val townName=address.subAdminArea
                val addressInfo=cityName+"/"+townName
                return addressInfo
            }else{
                return "Address is not found"
            }
        }catch(e: Exception) {
            Log.d("Error:",e.toString())

        }
        return ""
    }
    private fun calculateDistance(lat: Double, lon: Double): Double{
        val r = 6371.0
        val selectedLocationLat = intent.getDoubleExtra("latitude", 0.0)
        val selectedLocationLon = intent.getDoubleExtra("longitude", 0.0)

        val lat1Rad = Math.toRadians(selectedLocationLat)
        val lat2Rad = Math.toRadians(lat)
        val dLatRad = Math.toRadians(lat - selectedLocationLat)
        val dLonRad = Math.toRadians(lon - selectedLocationLon)

        val a = Math.sin(dLatRad / 2).pow(2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(dLonRad / 2).pow(2)

        val d = 2 * r * Math.asin(Math.sqrt(a))


        return d


    }
    private fun decode(input: String): String {
        try {
            if (input.isEmpty()) return ""

            val dictionary = mutableMapOf<Int, String>()
            val chars = input.toCharArray()
            var c = chars[0].toString()
            var f = c
            val result = mutableListOf<String>()
            result.add(c)

            var h = 256
            var o = h

            var i = 1
            while (i < chars.size) {
                val a = chars[i].code
                val entry = if (a < h) {
                    chars[i].toString()
                } else {
                    dictionary[a] ?: (f + c)
                }

                result.add(entry)
                c = entry[0].toString()
                dictionary[o] = f + c
                o++
                f = entry
                i++
            }
            return result.joinToString("")
        } catch (e: Exception) {
            Log.e("Decode", "Hata: ${e.message}")
            return "{}"
        }
    }
}