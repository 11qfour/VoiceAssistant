package com.example.voiceassistant.forecast

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import java.io.Serializable

@Root(name = "current", strict = false)
class Forecast:Serializable { //главный клас, описывающий весь ответ
    @field:Element(name = "temperature") //вложенный тег
    var temperature: Temperature? = null

    @field:Element(name = "weather")
    var weather: Weather? = null
}

@Root(name = "temperature", strict = false)
class Temperature {
    @field:Attribute(name = "value")
    var value: String? = null
}

@Root(name = "weather", strict = false)
class Weather {
    @field:Attribute(name = "value")
    var value: String? = null
}