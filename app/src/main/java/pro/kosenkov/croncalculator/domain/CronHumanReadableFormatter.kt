package pro.kosenkov.croncalculator.domain

object CronHumanReadableFormatter {

    fun format(cron: String): String {
        val parts = cron.split(" ")

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

        if (minutes.startsWith("*/") &&
            hours == "*" &&
            dayOfMonth == "*" &&
            month == "*" &&
            dayOfWeek == "*"
        ) {
            return "Каждые ${minutes.removePrefix("*/")} минут"
        }

        if (minutes == "*" &&
            hours == "*" &&
            dayOfMonth == "*" &&
            month == "*" &&
            dayOfWeek == "*"
        ) {
            return "Каждую минуту"
        }

        val time = formatTime(hours, minutes)

        if (dayOfWeek != "*") {
            return "${describeDayOfWeek(dayOfWeek)} в $time"
        }

        if (dayOfMonth != "*") {
            return "Каждый $dayOfMonth день месяца в $time"
        }

        return "Каждый день в $time"
    }

    private fun formatTime(hours: String, minutes: String): String {
        val h = hours.toIntOrNull()
        val m = minutes.toIntOrNull()

        return if (h != null && m != null) {
            String.format("%02d:%02d", h, m)
        } else {
            "$hours:$minutes"
        }
    }

    private fun describeDayOfWeek(value: String): String {
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
}