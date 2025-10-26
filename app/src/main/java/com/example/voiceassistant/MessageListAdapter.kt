package com.example.voiceassistant

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MessageListAdapter : RecyclerView.Adapter<MessageViewHolder>(){
    var messageList = mutableListOf<Message>()

    // Константы для определения типа ячейки (отправленное или полученное)
    private companion object {
        const val ASSISTANT_TYPE = 0
        const val USER_TYPE = 1
    }

    // Этот метод создает ViewHolder, "надувая" нужный XML-макет
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // В зависимости от типа, выбираем макет для пользователя или ассистента
        val view = if (viewType == USER_TYPE) {
            inflater.inflate(R.layout.user_message, parent, false)
        } else {
            inflater.inflate(R.layout.assistant_message, parent, false)
        }
        return MessageViewHolder(view)
    }

    // Этот метод связывает ViewHolder с данными по конкретной позиции
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.bind(message)
    }

    // Этот метод возвращает общее количество элементов в списке
    override fun getItemCount(): Int = messageList.size

    // Этот метод определяет тип ячейки (пользователь/ассистент) для конкретной позиции
    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.isSend) {
            USER_TYPE
        } else {
            ASSISTANT_TYPE
        }
    }
}