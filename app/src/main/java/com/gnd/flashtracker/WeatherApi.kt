package com.gnd.flashtracker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val location:LocationData,
    val current:CurrentData
)
data class LocationData(val name: String,val country: String,val tzId: String)
data class CurrentData(val tempC: Double, val condition:WeatherCondition)
data class WeatherCondition(val text: String,val icon: String)

interface WeatherApi {
    @GET("current.json")
    fun getCityWeather(
        @Query("key") apiKey: String,
        @Query("q") query: String
    ): Call<WeatherResponse>
}