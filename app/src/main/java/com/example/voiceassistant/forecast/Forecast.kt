package com.example.voiceassistant.forecast

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Forecast:Serializable { //главный клас, описывающий весь ответ
    @SerializedName("main")
    @Expose
    var main: Main? = null

    @SerializedName("weather")
    @Expose
    var weather: ArrayList<Weather?> = ArrayList()
}

class Main { //вложенный класс для main в json
    @SerializedName("temp")
    @Expose
    var temp: Double? = null
}

class Weather { //вложенный для массива weather в json
    @SerializedName("description")
    @Expose
    var description: String? = null
}