package com.gnd.lightningtracker

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import retrofit2.Call
import retrofit2.Callback
import java.util.Locale
import kotlin.math.pow
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LightningsRV : AppCompatActivity() {

    private lateinit var headerWeatherIc: ImageView
    private lateinit var headerAddress: TextView
    private lateinit var headerWeaInfo: TextView
    private lateinit var headerDegree:TextView
    private lateinit var recyclerView: RecyclerView
    private var range: Int = 1000000
    private var listAllFlashes: Boolean= true
    private lateinit var dataList: MutableList<Data>
    val key= BuildConfig.WEATHER_API_KEY

    @SuppressLint("UnspecifiedRegisterReceiverFlag", "SetTextI18n")
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

        val service= WeatherClient.service
        headerAddress=findViewById(R.id.headerAddress)
        headerWeatherIc=findViewById(R.id.headerWeatherIc)
        headerDegree=findViewById(R.id.headerDegree)
        headerWeaInfo=findViewById(R.id.headerWeatherInfo)

        recyclerView=findViewById(R.id.recyclerview)
        recyclerView.layoutManager= LinearLayoutManager(this)
        range= intent.getIntExtra("range",1000000)
        listAllFlashes=intent.getBooleanExtra("listAllFlashes",false)

        if(!listAllFlashes){
            val latitude=intent.getDoubleExtra("latitude",0.0)
            val longitude=intent.getDoubleExtra("longitude",0.0)

            headerAddress.text = "Loading..."
            lifecycleScope.launch{
                val location=getAddress(this@LightningsRV,latitude,longitude)
                headerAddress.text = location
            }

            val query="$latitude,$longitude"
            val call=service.getCityWeather(key,query)
            call.enqueue(object : Callback<WeatherResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<WeatherResponse?>,
                    response: Response<WeatherResponse?>
                ) {
                    if(response.isSuccessful){
                        val weather=response.body()
                        val imageUrl="https://"+weather?.current?.condition?.icon
                        headerWeatherIc.load(imageUrl){
                            crossfade(true)
                            placeholder(R.drawable.loading)
                        }
                        headerDegree.text = weather?.current?.temp_c.toString()+"°C"
                        headerWeaInfo.text = weather?.current?.condition?.text
                    }
                }

                override fun onFailure(
                    call: Call<WeatherResponse?>,
                    t: Throwable
                ) {
                    Log.d("HATA:",t.toString())
                }

            })
        }else{
            headerWeatherIc.visibility= View.INVISIBLE
            headerWeaInfo.visibility= View.INVISIBLE
            headerDegree.visibility= View.INVISIBLE
            headerAddress.text = "All Lightning Events On The World"
        }

        dataList=mutableListOf()
        val adapter= RVAdapter(lifecycleScope,dataList,listAllFlashes){data ->
            val lat=data.latitude
            val lon=data.longitude
            val label="Flash Point"
            val zoomLevel = 1

            val uri = "geo:$lat,$lon?z=$zoomLevel&q=$lat,$lon($label)".toUri()
            val mapsIntent= Intent(Intent.ACTION_VIEW,uri)
            if (mapsIntent.resolveActivity(packageManager)!=null){
                mapsIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapsIntent)
            }
        }

        recyclerView.adapter=adapter
        recyclerView.itemAnimator = SlideInItemAnimator()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {


                val lat = intent.getDoubleExtra("lat", 0.0)
                val lon = intent.getDoubleExtra("lon", 0.0)

                val timeThreshold = intent.getBooleanExtra("timeThreshold",false)
                val uniqueLightning = intent.getBooleanExtra("uniqueLightning",false)

                Log.d("Websocket reconnect status:", "$newCon")

                if (newCon && firstCon){
                    if (timeThreshold){
                        printOnScreen(lat,lon)
                    }
                    else{
                        newCon = false
                        firstCon = false
                        printOnScreen(lat,lon)
                    }
                }
                else{
                    if (newCon && uniqueLightning){
                        if (newCon){
                            newCon = false
                        }
                    }
                    else{
                        if (!newCon){
                            printOnScreen(lat,lon)
                        }
                    }
                }

            }
        }

        val filter = IntentFilter("WS_MESSAGE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }

        onBackPressedDispatcher.addCallback(this) {
            unregisterReceiver(receiver)
            isEnabled = false
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun printOnScreen(lat: Double, lon: Double){
        runOnUiThread {
            if(!listAllFlashes){
                val distance=calculateDistance(lat,lon)
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
                val data= Data(lat.toString(),lon.toString(),newCon&&firstCon)
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
    suspend fun getAddress(context: Context,latitude: Double,longitude: Double): String {
        return withContext(Dispatchers.IO) {
            var result: String
            val geocoder = Geocoder(context, Locale.ENGLISH)
            try {
                @Suppress("DEPRECATION") val addressList =
                    geocoder.getFromLocation(latitude, longitude, 2)
                if (!addressList.isNullOrEmpty()) {
                    val address = addressList[0]
                    val cityName = address.adminArea
                    val townName = address.subAdminArea
                    val addressInfo = "$cityName/$townName"
                    result = addressInfo
                } else {
                    result = "Address is not found"
                }
            } catch (e: Exception) {
                result = "Cannot detect the address"
            }
            result
        }
    }
    private fun calculateDistance(lat: Double, lon: Double): Double{
        val r = 6371.0
        val selectedLocationLat = intent.getDoubleExtra("latitude", 0.0)
        val selectedLocationLon = intent.getDoubleExtra("longitude", 0.0)

        val lat1Rad = Math.toRadians(selectedLocationLat)
        val lat2Rad = Math.toRadians(lat)
        val dLatRad = Math.toRadians(lat - selectedLocationLat)
        val dLonRad = Math.toRadians(lon - selectedLocationLon)

        val a = sin(dLatRad / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(dLonRad / 2).pow(2)

        val d = 2 * r * asin(sqrt(a))


        return d


    }

}