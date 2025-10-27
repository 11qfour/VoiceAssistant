package com.example.voiceassistant.forecast

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastApi {
    @GET("/data/2.5/weather?appid=5b70ecd7bcfe126f45ea6277ffebc2d5&lang=ru&units=metric&mode=xml") //units=metric - цельсии
    fun getCurrentWeather(@Query("q") city: String?): Call<Forecast?>?
}