package com.example.voiceassistant

import android.content.Context

class AI(private val context: Context){

    private val answers = mapOf(
        R.string.question_hello to R.string.answer_hello,
        R.string.question_how_are_you to R.string.answer_how_are_you,
        R.string.question_what_are_you_doing to R.string.answer_what_are_you_doing
    )

    fun getAnswer(question: String) : String{
        val cleanedQuestion = question.trim().lowercase() //убирая пробелы по краяем приводим к нижнему регистру
        val foundEntry = answers.entries.find {
            entry ->  val keyword = context.getString(entry.key).lowercase()
            cleanedQuestion.contains(keyword)
        }
        return if (foundEntry != null) {
            context.getString(foundEntry.value)
        } else {
            context.getString(R.string.answer_default)
        }
    }
}