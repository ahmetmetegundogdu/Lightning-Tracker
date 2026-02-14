package com.gnd.flashtracker

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherClient {
    val service= Retrofit.Builder()
        .baseUrl("https://api.weatherapi.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)
}