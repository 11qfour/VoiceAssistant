package com.example.voiceassistant

import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.example.voiceassistant.message.MessageEntity
import java.util.Date
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class Message (
    val text: String,
    val isSend: Boolean,
    val date: LocalDateTime
) : Parcelable{
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun fromEntity(entity: MessageEntity): Message {
            return Message(
                text = entity.text,
                date = LocalDateTime.parse(entity.date, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                isSend = entity.isSend
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedDate(): String {
        return date.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
}