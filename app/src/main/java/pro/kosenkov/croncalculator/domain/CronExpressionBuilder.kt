package pro.kosenkov.croncalculator.domain

import pro.kosenkov.croncalculator.model.CronInput

object CronExpressionBuilder {

    fun build(input: CronInput): String {
        return if (input.includeSeconds) {
            val finalSeconds = if (input.seconds == "*") "0" else input.seconds
            "$finalSeconds ${input.minutes} ${input.hours} ${input.dayOfMonth} ${input.month} ${input.dayOfWeek}"
        } else {
            "${input.minutes} ${input.hours} ${input.dayOfMonth} ${input.month} ${input.dayOfWeek}"
        }
    }
}