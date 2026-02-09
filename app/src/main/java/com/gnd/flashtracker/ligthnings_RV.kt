package com.gnd.flashtracker

import android.content.Context
import android.content.Intent
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
import android.os.Handler
import android.os.Looper
import androidx.activity.addCallback

import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONObject
import java.util.Locale
import kotlin.math.pow


class ligthnings_RV : AppCompatActivity() {

    lateinit var latitudeText: TextView
    lateinit var longitudeText: TextView

    private var newCon: Boolean = false
    private var currentTime: Long = 0
    private val client = OkHttpClient()
    private lateinit var recyclerView: RecyclerView
    private var webSocket: WebSocket? = null
    private var range: Int=1000000
    private var listAllFlashes: Boolean=false
    private lateinit var dataList: MutableList<Data>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ligthnings_rv)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recyclerView=findViewById(R.id.recyclerview)
        recyclerView.layoutManager= LinearLayoutManager(this)
        Log.d("s","aaaa")
        range= intent.getIntExtra("range",1000000)
        listAllFlashes=intent.getBooleanExtra("listAllFlashes",false)

        connectToWebSocket()

        onBackPressedDispatcher.addCallback(this) {
            webSocket?.close(1000,null)
            isEnabled = false
            onBackPressedDispatcher.onBackPressed()
        }
    }
    private fun connectToWebSocket() {
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

        val request = Request.Builder()
            .url("wss://ws1.blitzortung.org/") // URL
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Bağlandı!")
                // Blitzortung sunucusuna 'ben geldim' mesajı (Handshake benzeri)
                webSocket.send("""{"a":111}""")
                newCon= true
                currentTime = System.currentTimeMillis()*1000000
            }

            @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {

                    val decodedData = decode(text)


                    if (decodedData.startsWith("{")) {
                        val jsonObject = JSONObject(decodedData)


                        val lat = jsonObject.optDouble("lat", 0.0)
                        val lon = jsonObject.optDouble("lon", 0.0)
                        val delay = jsonObject.optDouble("delay",-1.0)
                        val time = jsonObject.optLong("time",0)

                        if (newCon){
                            if (currentTime<=time-(delay*1000000000)){
                                newCon = false

                                runOnUiThread {
                                    if (lat != 0.0 && lon != 0.0) {

                                        Log.d("WebSocket", "Yıldırım Düştü: $lat, $lon")
                                        if(!listAllFlashes){
                                            val distance=calculateDistance(lat,lon)
                                            Log.d("Distance:",distance.toString())
                                            Log.d("Range:",range.toString())
                                            if(range.toDouble()>=distance){
                                                val data= Data(lat.toString(),lon.toString(),distance)
                                                dataList.add(0, data)
                                                val address=getAddress(baseContext,lat,lon)
                                                Log.d("Adress:",address)
                                                adapter.notifyItemInserted(0)
                                                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                                val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()


                                                if (firstVisibleItemPosition == 0 || firstVisibleItemPosition == -1) {
                                                    recyclerView.scrollToPosition(0)
                                                }
                                            }
                                        }else{
                                            val data= Data(lat.toString(),lon.toString())
                                            dataList.add(0, data)
                                            adapter.notifyItemInserted(0)
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
                                Log.d("ignored","$currentTime --- $time")
                            }
                        }
                        else{

                            runOnUiThread {
                                if (lat != 0.0 && lon != 0.0) {

                                    Log.d("WebSocket", "Yıldırım Düştü: $lat, $lon")
                                    if(!listAllFlashes){
                                        val distance=calculateDistance(lat,lon)
                                        Log.d("Distance:",distance.toString())
                                        Log.d("Range:",range.toString())
                                        if(range.toDouble()>=distance){
                                            val data= Data(lat.toString(),lon.toString(),distance)
                                            dataList.add(0, data)
                                            val address=getAddress(baseContext,lat,lon)
                                            Log.d("Adress:",address)
                                            adapter.notifyItemInserted(0)
                                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                            val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()


                                            if (firstVisibleItemPosition == 0 || firstVisibleItemPosition == -1) {
                                                recyclerView.scrollToPosition(0)
                                            }
                                        }
                                    }else{
                                        val data= Data(lat.toString(),lon.toString())
                                        dataList.add(0, data)
                                        adapter.notifyItemInserted(0)
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
                } catch (e: Exception) {
                    Log.e("WebSocket", "Veri İşleme Hatası: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Kapanıyor: $code")
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Bağlantı Hatası: ${t.message}")
                // Hata olursa 3 saniye sonra tekrar dene
                Handler(Looper.getMainLooper()).postDelayed({
                    connectToWebSocket()
                }, 1000)
            }
        }

        webSocket = client.newWebSocket(request, listener)
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
    // Senin Decode Fonksiyonun (Class içine gömdüm)
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