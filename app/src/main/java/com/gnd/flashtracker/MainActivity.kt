package com.gnd.flashtracker

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.core.view.WindowInsetsCompat
import coil.load
import com.google.android.gms.location.Priority

import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    lateinit var locationSpinner: Spinner
    lateinit var locationSpinnerAdapter: ArrayAdapter<String>
    lateinit var inputRange: EditText
    lateinit var findButton: Button
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var coordinates_list: MutableList<Int>
    lateinit var weatherInfoText: TextView
    lateinit var degreeText: TextView
    lateinit var weatherIcon: ImageView
    lateinit var location_info: TextView

    // sonra düzeltcem
    //düzelt sikmim
    var latitude: Double=0.0
    var longitude: Double=0.0

    // silmezsek ölürü!!!!!
    // sil o zaman amk
    val key="8a914d5718194ad0b2d172949261102"
    //

    var location_list=listOf("Current Location","Ankara","Istanbul","Izmir","Samsun","All lightning events on the world.")//0,1,2,3,4
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)
        var selectedItem:Int=-1
        coordinates_list= mutableListOf()
        inputRange=findViewById(R.id.input_range)
        findButton=findViewById(R.id.find_button)
        locationSpinner=findViewById(R.id.location_spinner)
        locationSpinnerAdapter= ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,location_list)
        locationSpinner.adapter=locationSpinnerAdapter
        weatherIcon=findViewById(R.id.weatherIcon)
        weatherInfoText=findViewById(R.id.weatherInfo)
        degreeText=findViewById(R.id.degreeText)
        location_info=findViewById(R.id.location_info)
        getCurrentLocationWithPermission()

        val serviceIntent = Intent(this, WebSocketService::class.java)
        startService(serviceIntent)

        locationSpinner.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedItem=position
                if (position==5){
                    inputRange.visibility= View.GONE
                    findButton.setText("Detect Strikes")

                }else{
                    inputRange.visibility= View.VISIBLE
                    findButton.setText("Detect Strikes")


                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedItem=-1
            }

        }
        findButton.setOnClickListener {
            if(selectedItem==-1){
                Toast.makeText(this,"Please choose any location", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            when(selectedItem){
                0->sendToIntent(latitude,longitude)
                1->sendToIntent(39.925533,32.866287)//Ankara
                2->sendToIntent(41.015137,28.979530)//Istanbul
                3->sendToIntent(38.423733,27.142826)//Izmir
                4->sendToIntent(41.28667,36.33)//Samsun
                5->getAllFlashes()
            }
        }
    }

    fun getAllFlashes(){
        val intent= Intent(this, ligthnings_RV::class.java)
        val listAllFlashes=true
        intent.putExtra("listAllFlashes",listAllFlashes)
        startActivity(intent)
    }
    fun getCurrentLocationWithPermission(){
        Log.d("Girdi","Grid")
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),100)
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if(location!=null){
                Log.d("Bilgi mesajı:","Konum başarıylı aldı.")

                val lat=location.latitude.toDouble()
                val lng=location.longitude.toDouble()
                latitude=lat
                longitude=lng
                setWeather()
                Log.d("Bilgi mesajı:","Konum başarıylı aldı.")


            }
            else{
                Toast.makeText(this,"Konum bilgisi alınamadı. Tekrar deneyin.", Toast.LENGTH_LONG).show()
                fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,null).addOnSuccessListener {location->
                    if(location!=null){
                        Log.d("Bilgi mesajı:","Konum başarıylı aldı.")

                        Toast.makeText(this,"Güncel konum alınıyor", Toast.LENGTH_LONG).show()
                        val lat=location.latitude.toDouble()
                        val lng=location.longitude.toDouble()
                        latitude=lat
                        longitude=lng
                        setWeather()
                        Log.d("Bilgi mesajı:","Konum başarıylı aldı.")

                    }
                }

            }
        }
    }
    fun setWeather(){
        val service= WeatherClient.service

        val query="$latitude,$longitude"
        val call=service.getCityWeather(key,query)
        call.enqueue(object : Callback<WeatherResponse>{
            override fun onResponse(
                call: Call<WeatherResponse?>,
                response: Response<WeatherResponse?>
            ) {
                if(response.isSuccessful){
                    val weather=response.body()
                    Log.d("Weather Info:","${weather?.current?.condition?.text}")
                    degreeText.setText(weather?.current?.temp_c.toString()+"°C")
                    weatherInfoText.setText(weather?.current?.condition?.text)
                    val imageUrl="https://"+weather?.current?.condition?.icon
                    weatherIcon.load(imageUrl){
                        crossfade(true)
                        placeholder(R.drawable.loading)
                    }
                    val location=weather?.location?.name+"\n"+weather?.location?.country
                    location_info.setText(location)
                }
            }

            override fun onFailure(
                call: Call<WeatherResponse?>,
                t: Throwable
            ) {
                Log.d("HATA:",t.toString())
            }

        })

    }
    fun sendToIntent(lat: Double, lng: Double){
        val intent= Intent(this, ligthnings_RV::class.java)
        intent.putExtra("latitude",lat)
        intent.putExtra("longitude",lng)
        val range=inputRange.text.toString()
        val value = range.toIntOrNull() ?: 1000000
        intent.putExtra("range",value)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if(requestCode==100&&grantResults.isNotEmpty()&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
            getCurrentLocationWithPermission()
        }
    }
}