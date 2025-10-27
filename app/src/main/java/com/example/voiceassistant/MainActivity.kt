package com.example.voiceassistant

import android.content.Context
import android.content.SharedPreferences
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
            val savedList = savedInstanceState.getParcelableArrayList<Message>(CHAT_HISTORY)
            if (savedList != null) {
                messageListAdapter.messageList = savedList
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onSend() {
        val userQuestion = binding.questionField.text.toString()
        if (userQuestion.isEmpty()) {
            return
        }

        val userMessage = Message(userQuestion, isSend = true)
        messageListAdapter.messageList.add(userMessage)
        messageListAdapter.notifyItemInserted(messageListAdapter.messageList.size - 1)
        binding.chatMessageList.scrollToPosition(messageListAdapter.messageList.size - 1)
        binding.questionField.text.clear()
        dismissKeyboard()

        ai.getAnswer(userQuestion, Consumer { answer ->
            runOnUiThread {
                val assistantMessage = Message(answer, isSend = false)
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

    override fun onStop() {
        super.onStop()
        Log.i(LOG_TAG, "onStop")

        val editor = sPref!!.edit()
        editor.putBoolean(THEME, isLight)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(LOG_TAG, "onDestroy")

        // Важно освобождать ресурсы TTS
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    // Ты можешь добавить и остальные методы жизненного цикла для логирования
    override fun onStart() { super.onStart(); Log.i(LOG_TAG, "onStart") }
    override fun onResume() { super.onResume(); Log.i(LOG_TAG, "onResume") }
    override fun onPause() { super.onPause(); Log.i(LOG_TAG, "onPause") }
    override fun onRestart() { super.onRestart(); Log.i(LOG_TAG, "onRestart") }
}