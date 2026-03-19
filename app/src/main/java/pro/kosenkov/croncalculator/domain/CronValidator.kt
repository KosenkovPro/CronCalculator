package pro.kosenkov.croncalculator.domain

import pro.kosenkov.croncalculator.model.CronInput

object CronValidator {

    fun validate(input: CronInput): String? {
        if (input.includeSeconds) {
            validateCronPart(
                value = input.seconds,
                min = 0,
                max = 59,
                fieldName = "Секунды"
            )?.let { return it }
        }

        validateCronPart(
            value = input.minutes,
            min = 0,
            max = 59,
            fieldName = "Минуты"
        )?.let { return it }

        validateCronPart(
            value = input.hours,
            min = 0,
            max = 23,
            fieldName = "Часы"
        )?.let { return it }

        validateCronPart(
            value = input.dayOfMonth,
            min = 1,
            max = 31,
            fieldName = "День месяца"
        )?.let { return it }

        return null
    }

    private fun validateCronPart(
        value: String,
        min: Int,
        max: Int,
        fieldName: String
    ): String? {
        if (value == "*") {
            return null
        }

        val parts = value.split(",")

        for (part in parts) {
            val error = validateCronToken(
                token = part.trim(),
                min = min,
                max = max,
                fieldName = fieldName
            )
            if (error != null) return error
        }

        return null
    }

    private fun validateCronToken(
        token: String,
        min: Int,
        max: Int,
        fieldName: String
    ): String? {
        if (token.isEmpty()) {
            return "$fieldName: пустой элемент в списке"
        }

        if (token == "*") {
            return null
        }

        // */5
        if (token.startsWith("*/")) {
            val step = token.removePrefix("*/").toIntOrNull()
            if (step == null || step <= 0) {
                return "$fieldName: шаг должен быть положительным числом, например */5"
            }
            return null
        }

        // 1-10/2
        if (token.contains("/")) {
            val rangeAndStep = token.split("/")
            if (rangeAndStep.size != 2) {
                return "$fieldName: неверный формат шага"
            }

            val rangePart = rangeAndStep[0]
            val stepPart = rangeAndStep[1]

            val step = stepPart.toIntOrNull()
            if (step == null || step <= 0) {
                return "$fieldName: шаг должен быть положительным числом"
            }

            return validateRange(
                value = rangePart,
                min = min,
                max = max,
                fieldName = fieldName
            )
        }

        // 1-5
        if (token.contains("-")) {
            return validateRange(
                value = token,
                min = min,
                max = max,
                fieldName = fieldName
            )
        }

        // одно число
        val number = token.toIntOrNull()
            ?: return "$fieldName: допустимы *, число, диапазон 1-5, список 1,2,3 или шаг */5"

        if (number !in min..max) {
            return "$fieldName: допустимый диапазон $min..$max"
        }

        return null
    }

    private fun validateRange(
        value: String,
        min: Int,
        max: Int,
        fieldName: String
    ): String? {
        val bounds = value.split("-")
        if (bounds.size != 2) {
            return "$fieldName: неверный формат диапазона"
        }

        val start = bounds[0].toIntOrNull()
        val end = bounds[1].toIntOrNull()

        if (start == null || end == null) {
            return "$fieldName: диапазон должен содержать числа, например 1-5"
        }

        if (start !in min..max || end !in min..max) {
            return "$fieldName: допустимый диапазон $min..$max"
        }

        if (start > end) {
            return "$fieldName: начало диапазона не может быть больше конца"
        }

        return null
    }
}