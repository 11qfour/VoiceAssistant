package com.example.voiceassistant

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var chatMessageList: RecyclerView
    private val messageListAdapter = MessageListAdapter()
    private lateinit var sendButton : Button
    private lateinit var questionText : EditText
    private lateinit var textToSpeech: TextToSpeech
    private val ai = AI(this)

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Сначала инициализируем все View
        chatMessageList = findViewById(R.id.chatMessageList)
        sendButton = findViewById(R.id.sendButton)
        questionText = findViewById(R.id.questionField)

        // Настраиваем RecyclerView
        chatMessageList.layoutManager = LinearLayoutManager(this)
        chatMessageList.adapter = messageListAdapter

        // Восстанавливаем состояние ПОСЛЕ инициализации View
        if (savedInstanceState != null) {
            val savedHistory = savedInstanceState.getParcelableArrayList<Message>("chat_history")
            if (savedHistory != null) {
                messageListAdapter.messageList.addAll(savedHistory)
                // Важно: уведомляем адаптер о восстановлении данных
                messageListAdapter.notifyDataSetChanged()
            }
        }

        // Остальная инициализация
        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.getDefault()
            }
        }

        sendButton.setOnClickListener {
            onSend()
        }
    }

    private fun dismissKeyboard() {
        val view : View? = this.currentFocus
        if(view!=null){
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onSend() {
        if (questionText.text.isNotEmpty()) {
            val userQuestion = questionText.text.toString()

            // 1. Добавляем сообщение пользователя в список
            val userMessage = Message(userQuestion, isSend = true)
            messageListAdapter.messageList.add(userMessage)
            // Оповещаем адаптер о новом элементе
            messageListAdapter.notifyItemInserted(messageListAdapter.messageList.size - 1)

            val answer = ai.getAnswer(userQuestion)

            // 2. Добавляем ответ ассистента в список
            val assistantMessage = Message(answer, isSend = false)
            messageListAdapter.messageList.add(assistantMessage)
            // Оповещаем адаптер о втором новом элементе
            messageListAdapter.notifyItemInserted(messageListAdapter.messageList.size - 1)

            // 3. Прокручиваем список к последнему сообщению
            chatMessageList.scrollToPosition(messageListAdapter.messageList.size - 1)

            textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null)
            questionText.text.clear()
            dismissKeyboard()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("chat_history", ArrayList(messageListAdapter.messageList))
    }
    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}