package com.example.voiceassistant

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var sendButton : Button
    private lateinit var chatWindow : TextView
    private lateinit var questionText : EditText
    private lateinit var chatScrollView: ScrollView

    private val ai = AI(this)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendButton = findViewById(R.id.sendButton)
        chatWindow =  findViewById(R.id.chatWindow)
        questionText = findViewById(R.id.questionField)
        chatScrollView = findViewById(R.id.chatScrollView)

        sendButton.setOnClickListener{
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

    private fun onSend() {
        if(questionText.text.isNotEmpty()){
            val userQuestion = questionText.text.toString()
            val answer= ai.getAnswer(userQuestion)

            chatWindow.append("\n\nВы: $userQuestion")
            chatWindow.append("\n\nАссистент: $answer")

            questionText.text.clear()
            dismissKeyboard();

            chatScrollView.post { chatScrollView.fullScroll(View.FOCUS_DOWN) }
        };
    }
}