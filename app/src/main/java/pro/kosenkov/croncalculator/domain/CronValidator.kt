package pro.kosenkov.croncalculator.domain

import pro.kosenkov.croncalculator.model.CronInput

object CronValidator {

    fun validate(input: CronInput): String? {
        if (input.includeSeconds) {
            validateSimpleCronPart(
                value = input.seconds,
                min = 0,
                max = 59,
                fieldName = "Секунды"
            )?.let { return it }
        }

        validateSimpleCronPart(
            value = input.minutes,
            min = 0,
            max = 59,
            fieldName = "Минуты"
        )?.let { return it }

        validateSimpleCronPart(
            value = input.hours,
            min = 0,
            max = 23,
            fieldName = "Часы"
        )?.let { return it }

        validateSimpleCronPart(
            value = input.dayOfMonth,
            min = 1,
            max = 31,
            fieldName = "День месяца"
        )?.let { return it }

        return null
    }

    private fun validateSimpleCronPart(
        value: String,
        min: Int,
        max: Int,
        fieldName: String
    ): String? {
        if (value == "*") return null

        if (value.startsWith("*/")) {
            val step = value.removePrefix("*/").toIntOrNull()
            if (step == null || step <= 0) {
                return "$fieldName: шаг должен быть положительным числом, например */5"
            }
            return null
        }

        val number = value.toIntOrNull()
            ?: return "$fieldName: допустимы только *, число или шаг вида */5"

        if (number !in min..max) {
            return "$fieldName: допустимый диапазон $min..$max"
        }

        return null
    }
}