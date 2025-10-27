package com.example.voiceassistant.message

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.voiceassistant.Message
import java.time.format.DateTimeFormatter

class MessageEntity(
    var text: String,
    var date: String,
    var isSend: Boolean
) {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun fromMessage(message: Message): MessageEntity {
            return MessageEntity(
                text = message.text,
                date = message.date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                isSend = message.isSend
            )
        }
    }
}