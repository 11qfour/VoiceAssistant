package com.example.voiceassistant.message

import android.os.Parcelable
import java.util.Date
import kotlinx.parcelize.Parcelize
@Parcelize
data class Message (
    val text: String,
    val isSend: Boolean,
    val date: Date = Date() //создается автоматически при создании объекта
) : Parcelable