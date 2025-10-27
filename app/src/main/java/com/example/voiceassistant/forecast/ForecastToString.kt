package com.example.voiceassistant.forecast

import android.util.Log
import androidx.core.util.Consumer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForecastToString {
    fun getForecast(city: String?, callback: Consumer<String>) {
        val api: ForecastApi? = ForecastService().getApi()
        val call: Call<Forecast?>? = api?.getCurrentWeather(city)

        call!!.enqueue(object : Callback<Forecast?> { //асинхронный вызов
            // Этот метод вызывается при успешном ответе от сервера
            override fun onResponse(call: Call<Forecast?>?, response: Response<Forecast?>?) {
                val result = response?.body()

                if (result != null) {
                    val tempString = result.temperature?.value
                    val temp = tempString?.toDoubleOrNull()?.toInt()
                    val description = result.weather?.value

                    val answer = "В городе $city сейчас $temp градуса, $description"
                    callback.accept(answer)
                } else {
                    callback.accept("Не могу узнать погоду для города $city")
                }
            }

            override fun onFailure(call: Call<Forecast?>, t: Throwable) {
                Log.w("WEATHER", t.message ?: "Unknown error")
                callback.accept("Произошла ошибка при получении данных о погоде.")
            }
        })
    }
}