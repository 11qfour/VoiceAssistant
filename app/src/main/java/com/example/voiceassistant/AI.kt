package com.example.voiceassistant

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

class AI(private val context: Context){

    private val simpleAnswers = mapOf(
        R.string.question_hello to R.string.answer_hello,
        R.string.question_how_are_you to R.string.answer_how_are_you,
        R.string.question_what_are_you_doing to R.string.answer_what_are_you_doing
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAnswer(question: String): String {
        val cleanedQuestion = question.trim().lowercase()

        // Проверяем новые, более сложные вопросы
        // `when` - это как `switch` в других языках
        return when {
            // "Который час?"
            cleanedQuestion.contains(context.getString(R.string.question_what_time_is_it)) ->
                getCurrentTime()

            // "Какой сегодня день?"
            cleanedQuestion.contains(context.getString(R.string.question_what_day_is_it)) ->
                getCurrentDate()

            // "Какой день недели?"
            cleanedQuestion.contains(context.getString(R.string.question_what_day_of_week)) ->
                getDayOfWeek()

            // "Сколько дней до ...?"
            cleanedQuestion.contains(context.getString(R.string.question_days_until)) ->
                getDaysUntil(cleanedQuestion)

            // Если ничего не подошло, ищем в старом словаре
            else -> getSimpleAnswer(cleanedQuestion)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTime(): String {
        val time = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return String.format(context.getString(R.string.answer_template_time), time.format(formatter))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDate(): String {
        val date = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        return String.format(context.getString(R.string.answer_template_date), date.format(formatter))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDayOfWeek(): String {
        val day: DayOfWeek = LocalDate.now().dayOfWeek
        // Получаем название дня недели для русской локали
        val dayName = day.getDisplayName(TextStyle.FULL, Locale("ru"))
        return String.format(context.getString(R.string.answer_template_day_of_week), dayName)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDaysUntil(question: String): String {
        // Ищем дату в формате dd.MM.yyyy в вопросе
        val regex = "\\d{2}\\.\\d{2}\\.\\d{4}".toRegex()
        val matchResult = regex.find(question)

        if (matchResult != null) {
            val dateString = matchResult.value
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            return try {
                val targetDate = LocalDate.parse(dateString, formatter)
                val today = LocalDate.now()
                val daysBetween = ChronoUnit.DAYS.between(today, targetDate)

                String.format(context.getString(R.string.answer_template_days_until), dateString, daysBetween)
            } catch (e: Exception) {
                context.getString(R.string.answer_error_date_parse)
            }
        }
        return context.getString(R.string.answer_error_date_parse)
    }

    private fun getSimpleAnswer(cleanedQuestion: String): String {
        val foundEntry = simpleAnswers.entries.find { entry ->
            val keyword = context.getString(entry.key).lowercase()
            cleanedQuestion.contains(keyword)
        }
        return if (foundEntry != null) {
            context.getString(foundEntry.value)
        } else {
            context.getString(R.string.answer_default)
        }
    }
}