package pro.kosenkov.croncalculator.domain

import android.annotation.SuppressLint

object CronHumanReadableFormatter {

    fun format(cron: String): String {
        val parts = cron.trim().split(Regex("\\s+"))

        if (parts.size !in 5..6) {
            return "Не удалось распознать cron-выражение"
        }

        val hasSeconds = parts.size == 6
        val offset = if (hasSeconds) 1 else 0

        val minutes = parts[offset]
        val hours = parts[offset + 1]
        val dayOfMonth = parts[offset + 2]
        val month = parts[offset + 3]
        val dayOfWeek = parts[offset + 4]

        if (isEveryMinute(minutes, hours, dayOfMonth, month, dayOfWeek)) {
            return "Каждую минуту"
        }

        if (isEveryNMinutes(minutes, hours, dayOfMonth, month, dayOfWeek)) {
            return "Каждые ${minutes.removePrefix("*/")} минут"
        }

        val timePart = describeTime(hours, minutes)
        val dayOfWeekPart = describeDayOfWeek(dayOfWeek)
        val dayOfMonthPart = describeDayOfMonth(dayOfMonth)
        val monthPart = describeMonth(month)

        val resultParts = mutableListOf<String>()

        if (dayOfWeek != "*") {
            resultParts += dayOfWeekPart
        } else if (dayOfMonth != "*") {
            resultParts += dayOfMonthPart
        }

        resultParts += timePart

        if (month != "*") {
            resultParts += monthPart
        }

        val result = resultParts.joinToString(" ")
        return result.replaceFirstChar { it.uppercase() }
    }

    private fun isEveryMinute(
        minutes: String,
        hours: String,
        dayOfMonth: String,
        month: String,
        dayOfWeek: String
    ): Boolean {
        return minutes == "*" &&
                hours == "*" &&
                dayOfMonth == "*" &&
                month == "*" &&
                dayOfWeek == "*"
    }

    private fun isEveryNMinutes(
        minutes: String,
        hours: String,
        dayOfMonth: String,
        month: String,
        dayOfWeek: String
    ): Boolean {
        return minutes.startsWith("*/") &&
                hours == "*" &&
                dayOfMonth == "*" &&
                month == "*" &&
                dayOfWeek == "*"
    }

    @SuppressLint("DefaultLocale")
    private fun describeTime(hours: String, minutes: String): String {
        val exactHours = hours.toIntOrNull()
        val exactMinutes = minutes.toIntOrNull()

        if (exactHours != null && exactMinutes != null) {
            return "в ${String.format("%02d:%02d", exactHours, exactMinutes)}"
        }

        if (hours == "*" && minutes.startsWith("*/")) {
            return "каждые ${minutes.removePrefix("*/")} минут"
        }

        if (hours == "*" && minutes == "*") {
            return "каждый час и каждую минуту"
        }

        if (hours == "*" && isRange(minutes)) {
            return "в каждую минуту ${describeRange(minutes)}"
        }

        if (hours == "*" && isList(minutes)) {
            return "в минуты ${describeList(minutes)} каждого часа"
        }

        if (hours == "*" && isRangeWithStep(minutes)) {
            return "каждые ${extractStep(minutes)} минут в диапазоне ${extractRange(minutes)}"
        }

        if (minutes == "*" && exactHours != null) {
            val hour = String.format("%02d", exactHours)
            return "каждую минуту с $hour:00 до $hour:59"
        }

        if (isRange(hours) && exactMinutes != null) {
            return "в ${String.format("%02d", exactMinutes)} минуту ${describeRange(hours)} часа"
        }

        if (isList(hours) && exactMinutes != null) {
            return "в ${String.format("%02d", exactMinutes)} минуту в часы ${describeList(hours)}"
        }

        if (isRangeWithStep(hours) && exactMinutes != null) {
            return "в ${String.format("%02d", exactMinutes)} минуту каждые ${extractStep(hours)} часа в диапазоне ${extractRange(hours)}"
        }

        return "по времени: часы=$hours, минуты=$minutes"
    }

    private fun describeDayOfMonth(value: String): String {
        return when {
            value == "*" -> "Каждый день"

            isSingleNumber(value) -> "Каждый $value день месяца"

            isRange(value) -> "Каждый день месяца ${describeRange(value)}"

            isList(value) -> "В дни месяца ${describeList(value)}"

            isRangeWithStep(value) -> "Каждые ${extractStep(value)} дня месяца в диапазоне ${extractRange(value)}"

            value.startsWith("*/") -> "Каждые ${value.removePrefix("*/")} дней месяца"

            else -> "В день месяца: $value"
        }
    }

    private fun describeMonth(value: String): String {
        return when {
            value == "*" -> ""

            isSingleNumber(value) -> "в ${monthName(value)}"

            isRange(value) -> "в месяцах ${describeRange(value)}"

            isList(value) -> "в месяцах ${describeListWithMapper(value) { monthName(it) }}"

            isRangeWithStep(value) -> "каждые ${extractStep(value)} месяца в диапазоне ${extractRange(value)}"

            value.startsWith("*/") -> "каждые ${value.removePrefix("*/")} месяца"

            else -> "в месяце: $value"
        }
    }

    private fun describeDayOfWeek(value: String): String {
        return when {
            value == "*" -> "Каждый день"

            isSingleNumber(value) -> singleDayOfWeek(value)

            isRange(value) -> {
                val (start, end) = value.split("-")
                "Каждый день недели ${dayName(start)}–${dayName(end)}"
            }

            isList(value) -> "В дни недели ${describeListWithMapper(value) { dayName(it) }}"

            isRangeWithStep(value) -> {
                val range = extractRange(value)
                val step = extractStep(value)
                "Каждые $step дня недели в диапазоне $range"
            }

            value.startsWith("*/") -> "Каждые ${value.removePrefix("*/")} дней недели"

            else -> "В день недели: $value"
        }
    }

    private fun singleDayOfWeek(value: String): String {
        return when (value) {
            "1" -> "Каждый понедельник"
            "2" -> "Каждый вторник"
            "3" -> "Каждую среду"
            "4" -> "Каждый четверг"
            "5" -> "Каждую пятницу"
            "6" -> "Каждую субботу"
            "0", "7" -> "Каждое воскресенье"
            else -> "В день недели: $value"
        }
    }

    private fun dayName(value: String): String {
        return when (value) {
            "1" -> "понедельник"
            "2" -> "вторник"
            "3" -> "среда"
            "4" -> "четверг"
            "5" -> "пятница"
            "6" -> "суббота"
            "0", "7" -> "воскресенье"
            else -> value
        }
    }

    private fun monthName(value: String): String {
        return when (value) {
            "1" -> "январе"
            "2" -> "феврале"
            "3" -> "марте"
            "4" -> "апреле"
            "5" -> "мае"
            "6" -> "июне"
            "7" -> "июле"
            "8" -> "августе"
            "9" -> "сентябре"
            "10" -> "октябре"
            "11" -> "ноябре"
            "12" -> "декабре"
            else -> value
        }
    }

    private fun describeRange(value: String): String {
        val (start, end) = value.split("-")
        return "с $start по $end"
    }

    private fun describeList(value: String): String {
        val items = value.split(",").map { it.trim() }
        return joinNatural(items)
    }

    private fun describeListWithMapper(value: String, mapper: (String) -> String): String {
        val items = value.split(",").map { mapper(it.trim()) }
        return joinNatural(items)
    }

    private fun joinNatural(items: List<String>): String {
        return when (items.size) {
            0 -> ""
            1 -> items[0]
            2 -> "${items[0]} и ${items[1]}"
            else -> items.dropLast(1).joinToString(", ") + " и " + items.last()
        }
    }

    private fun extractRange(value: String): String {
        return value.substringBefore("/")
    }

    private fun extractStep(value: String): String {
        return value.substringAfter("/")
    }

    private fun isSingleNumber(value: String): Boolean {
        return value.toIntOrNull() != null
    }

    private fun isRange(value: String): Boolean {
        return value.contains("-") && !value.contains("/") && !value.contains(",")
    }

    private fun isList(value: String): Boolean {
        return value.contains(",")
    }

    private fun isRangeWithStep(value: String): Boolean {
        return value.contains("-") && value.contains("/")
    }
}