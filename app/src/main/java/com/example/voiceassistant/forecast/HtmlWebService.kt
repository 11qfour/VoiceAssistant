package com.example.voiceassistant.forecast

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import androidx.core.util.Consumer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.JsonObject
import retrofit2.http.Url

class HtmlWebService {
    class NumberResponse {
        @SerializedName("str")
        @Expose
        var str: String? = null
    }

    interface HtmlWebApi {
        @GET("/json/convert/num2str")
        fun getNumberAsString(@Query("num") number: Int): Call<NumberResponse?>?

        @GET("/geo/api.php?json")
        fun getCityInfo(@Query("city_name") city: String): Call<JsonObject?>?
    }

    class HtmlWebService {
        fun getApi(): HtmlWebApi? {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://htmlweb.ru")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(HtmlWebApi::class.java)
        }
    }

    class HtmlWebToString {
        fun numberToString(number: Int, callback: Consumer<String>) {
            val call = HtmlWebService().getApi()?.getNumberAsString(number)
            call?.enqueue(object : Callback<NumberResponse?> {
                override fun onResponse(call: Call<NumberResponse?>, response: Response<NumberResponse?>) {
                    val resultStr = response.body()?.str
                    if (resultStr != null) {
                        // Убираем лишние "рублей 00 копеек", если они есть
                        val cleanResult = resultStr.replace(" рублей 00 копеек", "")
                        val resultStr2 = cleanResult.replace(" рубля 00 копеек", "")
                        val resultStr3 = resultStr2.replace(" рубль 00 копеек", "")
                        callback.accept("$number это $resultStr3")
                    } else {
                        callback.accept("Не могу превратить это число в строку.")
                    }
                }
                override fun onFailure(call: Call<NumberResponse?>, t: Throwable) {
                    callback.accept("Ошибка при запросе к сервису чисел.")
                }
            })
        }

        fun cityInfoToString(city: String, callback: Consumer<String>) {
            val call = HtmlWebService().getApi()?.getCityInfo(city)
            // Тип Call теперь JsonObject
            call?.enqueue(object: Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                    val resultObject = response.body()
                    
                    if (resultObject != null && resultObject.has("0")) {
                        // Берем первый объект по ключу "0"
                        val firstCity = resultObject.getAsJsonObject("0")

                        val name = firstCity.get("name")?.asString
                        val region = firstCity.get("rajon")?.asString
                        val country = firstCity.get("country")?.asString
                        val telcod = firstCity.get("telcod")?.asString

                        val answer = "Город $name находится в регионе $region, " +
                                "страна: $country. Телефонный код: $telcod."
                        callback.accept(answer)
                    } else {
                        callback.accept("Не нашел информацию о городе $city.")
                    }
                }
                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    callback.accept("Ошибка при запросе информации о городе.")
                }
            })
        }
    }
}