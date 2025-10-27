package com.example.voiceassistant

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.util.Consumer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.voiceassistant.ai.AI
import com.example.voiceassistant.databinding.ActivityMainBinding
import com.example.voiceassistant.message.MessageEntity
import java.time.LocalDateTime
import java.util.Locale

class MainActivity : AppCompatActivity() {
    // Объект ViewBinding для доступа ко всем View элементам
    private lateinit var binding: ActivityMainBinding

    private val messageListAdapter = MessageListAdapter()

    private lateinit var ai: AI

    private lateinit var textToSpeech: TextToSpeech

    private val APP_PREFERENCES = "mysettings"
    private val THEME = "THEME"
    private var sPref: SharedPreferences? = null
    private var isLight = true

    private val LOG_TAG = "VoiceAssistant"

    private val CHAT_HISTORY = "chat_history"

    private lateinit var dbHelper: DBHelper
    private lateinit var database: SQLiteDatabase

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        sPref = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        isLight = sPref!!.getBoolean(THEME, true)
        if (!isLight) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        dbHelper = DBHelper(this)
        database = dbHelper.writableDatabase

        Log.i(LOG_TAG, "onCreate")

        ai = AI(this)

        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale("ru")
            }
        }

        binding.chatMessageList.adapter = messageListAdapter
        binding.chatMessageList.layoutManager = LinearLayoutManager(this)

        binding.sendButton.setOnClickListener {
            onSend()
        }

        if (savedInstanceState != null) {
            // Если есть Bundle (поворот экрана, смена темы) - восстанавливаем из него
            val savedList = savedInstanceState.getParcelableArrayList<Message>(CHAT_HISTORY)
            if (savedList != null) {
                messageListAdapter.messageList = savedList
            }
        } else {
            // Если Bundle пуст (холодный старт приложения) - загружаем из БД
            loadMessagesFromDb()
        }
        // Прокручиваем к последнему сообщению после загрузки
        binding.chatMessageList.scrollToPosition(messageListAdapter.messageList.size - 1)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadMessagesFromDb() {
        val cursor = database.query(DBHelper.TABLE_NAME, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            // Убедимся, что мы правильно получаем индексы колонок
            val messageIndex = cursor.getColumnIndexOrThrow(DBHelper.FIELD_MESSAGE)
            val dateIndex = cursor.getColumnIndexOrThrow(DBHelper.FIELD_DATE)
            val sendIndex = cursor.getColumnIndexOrThrow(DBHelper.FIELD_SEND) // <--- ВОТ ЭТО МЕСТО

            messageListAdapter.messageList.clear() // Очищаем список перед загрузкой

            do {
                // --- ПРОВЕРЯЕМ ЛОГИКУ ЗДЕСЬ ---
                val text = cursor.getString(messageIndex)
                val date = cursor.getString(dateIndex)
                val isSendInt = cursor.getInt(sendIndex) // Получаем 0 или 1

                // Превращаем 0/1 в true/false
                val isSendBoolean = isSendInt == 1

                val entity = MessageEntity(text, date, isSendBoolean)
                messageListAdapter.messageList.add(Message.fromEntity(entity))

            } while (cursor.moveToNext())
        }
        cursor.close()

        // Уведомляем адаптер, что данные изменились
        messageListAdapter.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onSend() {
        val userQuestion = binding.questionField.text.toString()
        if (userQuestion.isEmpty()) {
            return
        }

        val userMessage = Message(userQuestion, isSend = true, LocalDateTime.now())
        messageListAdapter.messageList.add(userMessage)
        messageListAdapter.notifyItemInserted(messageListAdapter.messageList.size - 1)
        binding.chatMessageList.scrollToPosition(messageListAdapter.messageList.size - 1)
        binding.questionField.text.clear()
        dismissKeyboard()

        ai.getAnswer(userQuestion, Consumer { answer ->
            runOnUiThread {
                val assistantMessage = Message(answer, isSend = false, LocalDateTime.now())
                messageListAdapter.messageList.add(assistantMessage)
                messageListAdapter.notifyItemInserted(messageListAdapter.messageList.size - 1)
                binding.chatMessageList.scrollToPosition(messageListAdapter.messageList.size - 1)
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        })
    }

    private fun dismissKeyboard() {
        val view: View? = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.day_settings -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                isLight = true
            }
            R.id.night_settings -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                isLight = false
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(CHAT_HISTORY, messageListAdapter.messageList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStop() {
        super.onStop()
        Log.i(LOG_TAG, "onStop")

        val editor = sPref!!.edit()
        editor.putBoolean(THEME, isLight)
        editor.apply()

        database.delete(DBHelper.TABLE_NAME, null, null)

        for (message in messageListAdapter.messageList) {
            val entity = MessageEntity.fromMessage(message)
            val contentValues = ContentValues()

            contentValues.put(DBHelper.FIELD_MESSAGE, entity.text)
            contentValues.put(DBHelper.FIELD_DATE, entity.date)

            // --- ПРОВЕРЯЕМ ЛОГИКУ ЗДЕСЬ ---
            // Правильно ли мы конвертируем Boolean в Int (0 или 1)
            val isSendInt = if (entity.isSend) 1 else 0
            contentValues.put(DBHelper.FIELD_SEND, isSendInt)

            database.insert(DBHelper.TABLE_NAME, null, contentValues)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(LOG_TAG, "onDestroy")

        // Важно освобождать ресурсы TTS
        textToSpeech.stop()
        textToSpeech.shutdown()

        database.close()
        dbHelper.close()
    }

    // Ты можешь добавить и остальные методы жизненного цикла для логирования
    override fun onStart() { super.onStart(); Log.i(LOG_TAG, "onStart") }
    override fun onResume() { super.onResume(); Log.i(LOG_TAG, "onResume") }
    override fun onPause() { super.onPause(); Log.i(LOG_TAG, "onPause") }
    override fun onRestart() { super.onRestart(); Log.i(LOG_TAG, "onRestart") }
}