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

        // 2. Проверяем вопрос о погоде ПЕРЕД блоком `when`
        val cityPattern: Pattern =
            Pattern.compile("погода.* (\\p{L}+)", Pattern.CASE_INSENSITIVE)
        val matcher = cityPattern.matcher(cleanedQuestion)

        if (matcher.find()) {
            val cityName: String? = matcher.group(1)

            // Если вопрос о погоде, запускаем асинхронный запрос
            ForecastToString().getForecast(cityName, Consumer { weatherString ->
                // Когда придет ответ из сети, вызываем callback
                callback.accept(weatherString)
            })
            // И сразу выходим из функции, чтобы не выполнять `when`
            return
        }

        // 3. Если вопрос НЕ о погоде, выполняется твоя старая синхронная логика
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

        // 4. Для синхронных ответов вызываем callback немедленно
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
}