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
import androidx.annotation.RequiresApi
import androidx.core.util.Consumer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceassistant.ai.AI
import com.example.voiceassistant.message.Message
import com.example.voiceassistant.message.MessageListAdapter
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
        val userQuestion = questionText.text.toString()
        if (userQuestion.isEmpty()) {
            return // Если поле пустое, ничего не делаем
        }

        // Добавляем сообщение пользователя в список и обновляем UI
        val userMessage = Message(userQuestion, isSend = true)
        messageListAdapter.messageList.add(userMessage)
        messageListAdapter.notifyItemInserted(messageListAdapter.messageList.size - 1)

        // Сразу прокручиваем к сообщению пользователя
        chatMessageList.scrollToPosition(messageListAdapter.messageList.size - 1)

        // Очищаем поле ввода и скрываем клавиатуру
        questionText.text.clear()
        dismissKeyboard()

        // Запускаем получение ответа от AI, передавая ему callback
        ai.getAnswer(userQuestion, Consumer { answer ->
            // все действия с UI должны быть в основном потоке!
            runOnUiThread {
                //Добавляем ответ ассистента в список и обновляем UI
                val assistantMessage = Message(answer, isSend = false)
                messageListAdapter.messageList.add(assistantMessage)
                messageListAdapter.notifyItemInserted(messageListAdapter.messageList.size - 1)

                // Прокручиваем список к НОВОМУ последнему сообщению (ответу ассистента)
                chatMessageList.scrollToPosition(messageListAdapter.messageList.size - 1)

                // Озвучиваем ответ
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        })
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