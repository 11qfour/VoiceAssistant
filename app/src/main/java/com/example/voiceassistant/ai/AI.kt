package com.example.voiceassistant.ai

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.voiceassistant.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import androidx.core.util.Consumer
import com.example.voiceassistant.forecast.ForecastToString
import com.example.voiceassistant.forecast.HtmlWebService
import java.util.regex.Pattern

class AI(private val context: Context){

    private val simpleAnswers = mapOf(
        R.string.question_hello to R.string.answer_hello,
        R.string.question_how_are_you to R.string.answer_how_are_you,
        R.string.question_what_are_you_doing to R.string.answer_what_are_you_doing
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAnswer(question: String, callback: Consumer<String>){
        val cleanedQuestion = question.trim().lowercase()

        val cityInfoPattern: Pattern =
            Pattern.compile("^что за город (\\p{L}+)", Pattern.CASE_INSENSITIVE)

        val weatherPattern: Pattern =
            Pattern.compile("погода (?:в городе )?(\\p{L}+)$", Pattern.CASE_INSENSITIVE)

        val numberPattern: Pattern =
            Pattern.compile("сколько будет (\\d+)$", Pattern.CASE_INSENSITIVE)

        val cityInfoMatcher = cityInfoPattern.matcher(cleanedQuestion)
        if (cityInfoMatcher.find()) {
            val cityName = cityInfoMatcher.group(1)
            if (cityName != null) {
                HtmlWebService.HtmlWebToString().cityInfoToString(cityName, callback)
                return
            }
        }

        val weatherMatcher = weatherPattern.matcher(cleanedQuestion)
        if (weatherMatcher.find()) {
            val cityName: String? = weatherMatcher.group(1)
            ForecastToString().getForecast(cityName, callback)
            return
        }

        val numberMatcher = numberPattern.matcher(cleanedQuestion)
        if (numberMatcher.find()) {
            val number = numberMatcher.group(1)?.toIntOrNull()
            if (number != null) {
                HtmlWebService.HtmlWebToString().numberToString(number, callback)
                return
            }
        }


        val answer = when {
            cleanedQuestion.contains(context.getString(R.string.question_what_time_is_it)) ->
                getCurrentTime()

            cleanedQuestion.contains(context.getString(R.string.question_what_day_is_it)) ->
                getCurrentDate()

            cleanedQuestion.contains(context.getString(R.string.question_what_day_of_week)) ->
                getDayOfWeek()

            cleanedQuestion.contains(context.getString(R.string.question_days_until)) ->
                getDaysUntil(cleanedQuestion)

            else -> getSimpleAnswer(cleanedQuestion)
        }

        callback.accept(answer)
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

    private fun getDegreeWord(temperature: Int): String {
        val lastDigit = temperature % 10
        val lastTwoDigits = temperature % 100

        if (lastTwoDigits in 11..14) {
            return "градусов"
        }

        return when (lastDigit) {
            1 -> "градус"
            2, 3, 4 -> "градуса"
            else -> "градусов"
        }
    }
}