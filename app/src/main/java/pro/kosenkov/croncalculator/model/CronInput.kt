package pro.kosenkov.croncalculator.model

data class CronInput(
    val includeSeconds: Boolean,
    val seconds: String,
    val minutes: String,
    val hours: String,
    val dayOfMonth: String,
    val month: String,
    val dayOfWeek: String
)