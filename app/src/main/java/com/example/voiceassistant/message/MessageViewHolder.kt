package com.example.voiceassistant.message

import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceassistant.Message
import com.example.voiceassistant.R
import java.text.SimpleDateFormat
import java.util.Locale

class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    // Находим View-элементы по их ID в макете ячейки
    private val messageText: TextView = itemView.findViewById(R.id.messageTextView)
    private val messageDate: TextView = itemView.findViewById(R.id.messageDateView)

    // заполнение View данными из объекта Message
    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(message: Message) {
        messageText.text = message.text
        messageDate.text = message.getFormattedDate()
    }
}