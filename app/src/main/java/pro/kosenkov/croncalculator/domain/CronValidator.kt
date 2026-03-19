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

        validateCronPart(
            value = input.month,
            min = 1,
            max = 12,
            fieldName = "Месяц"
        )?.let { return it }

        validateCronPart(
            value = input.dayOfWeek,
            min = 0,
            max = 7,
            fieldName = "День недели"
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

        // ? — обычно только для "День месяца" и "День недели"
        if (token == "?") {
            return if (fieldName == "День месяца" || fieldName == "День недели") {
                null
            } else {
                "$fieldName: символ ? допустим только для полей 'День месяца' и 'День недели'"
            }
        }

        // L — обычно только для "День месяца"
        if (token == "L") {
            return if (fieldName == "День месяца") {
                null
            } else {
                "$fieldName: символ L допустим только для поля 'День месяца'"
            }
        }

        // W — например 15W, только для "День месяца"
        if (token.endsWith("W")) {
            if (fieldName != "День месяца") {
                return "$fieldName: формат W допустим только для поля 'День месяца'"
            }

            val day = token.removeSuffix("W").toIntOrNull()
                ?: return "$fieldName: неверный формат W (пример: 15W)"

            if (day !in min..max) {
                return "$fieldName: допустимый диапазон $min..$max"
            }

            return null
        }

        // # — например 2#1, только для "День недели"
        if (token.contains("#")) {
            if (fieldName != "День недели") {
                return "$fieldName: формат # допустим только для поля 'День недели'"
            }

            val parts = token.split("#")
            if (parts.size != 2) {
                return "$fieldName: неверный формат # (пример: 2#1)"
            }

            val day = parts[0].toIntOrNull()
                ?: return "$fieldName: неверный день недели в формате #"

            val nth = parts[1].toIntOrNull()
                ?: return "$fieldName: неверный порядковый номер в формате #"

            if (day !in 0..7) {
                return "$fieldName: день недели в формате # должен быть в диапазоне 0..7"
            }

            if (nth !in 1..5) {
                return "$fieldName: порядковый номер в формате # должен быть в диапазоне 1..5"
            }

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
            ?: return "$fieldName: допустимы *, число, диапазон 1-5, список 1,2,3, шаг */5, а также специальные форматы Quartz"

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