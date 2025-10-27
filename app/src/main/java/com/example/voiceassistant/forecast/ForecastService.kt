package com.example.voiceassistant.forecast

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class ForecastService {
    fun getApi(): ForecastApi? {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org")
            .addConverterFactory(SimpleXmlConverterFactory.create())  // конвертер для XML
            .client(client)
            .build()
        return retrofit.create(ForecastApi::class.java)
    }
}