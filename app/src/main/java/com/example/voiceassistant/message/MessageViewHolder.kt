package com.example.voiceassistant.message

import android.view.View
import android.widget.TextView
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
    fun bind(message: Message) {
        messageText.text = message.text
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        messageDate.text = formatter.format(message.date)
    }
}