package pro.kosenkov.croncalculator.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import pro.kosenkov.croncalculator.model.CronInput

class CronExpressionBuilderTest {

    @Test
    fun `build returns 5-part cron when seconds are disabled`() {
        val input = CronInput(
            includeSeconds = false,
            seconds = "0",
            minutes = "15",
            hours = "10",
            dayOfMonth = "1",
            month = "6",
            dayOfWeek = "MON"
        )

        val result = CronExpressionBuilder.build(input)

        assertEquals("15 10 1 6 MON", result)
    }

    @Test
    fun `build returns 6-part cron when seconds are enabled`() {
        val input = CronInput(
            includeSeconds = true,
            seconds = "30",
            minutes = "15",
            hours = "10",
            dayOfMonth = "1",
            month = "6",
            dayOfWeek = "MON"
        )

        val result = CronExpressionBuilder.build(input)

        assertEquals("30 15 10 1 6 MON", result)
    }

    @Test
    fun `build uses wildcards correctly without seconds`() {
        val input = CronInput(
            includeSeconds = false,
            seconds = "*",
            minutes = "*",
            hours = "*",
            dayOfMonth = "*",
            month = "*",
            dayOfWeek = "*"
        )

        val result = CronExpressionBuilder.build(input)

        assertEquals("* * * * *", result)
    }

    @Test
    fun `build replaces wildcard seconds with zero when seconds are enabled`() {
        val input = CronInput(
            includeSeconds = true,
            seconds = "*",
            minutes = "*",
            hours = "*",
            dayOfMonth = "*",
            month = "*",
            dayOfWeek = "*"
        )

        val result = CronExpressionBuilder.build(input)

        assertEquals("0 * * * * *", result)
    }

    @Test
    fun `build keeps step values`() {
        val input = CronInput(
            includeSeconds = true,
            seconds = "0/10",
            minutes = "*/5",
            hours = "*/2",
            dayOfMonth = "*",
            month = "*",
            dayOfWeek = "*"
        )

        val result = CronExpressionBuilder.build(input)

        assertEquals("0/10 */5 */2 * * *", result)
    }

    @Test
    fun `build keeps range values`() {
        val input = CronInput(
            includeSeconds = false,
            seconds = "0",
            minutes = "10-20",
            hours = "8-18",
            dayOfMonth = "1-15",
            month = "1-6",
            dayOfWeek = "MON-FRI"
        )

        val result = CronExpressionBuilder.build(input)

        assertEquals("10-20 8-18 1-15 1-6 MON-FRI", result)
    }

    @Test
    fun `build keeps list values`() {
        val input = CronInput(
            includeSeconds = false,
            seconds = "0",
            minutes = "0,15,30,45",
            hours = "9,12,18",
            dayOfMonth = "1,10,20",
            month = "1,6,12",
            dayOfWeek = "MON,WED,FRI"
        )

        val result = CronExpressionBuilder.build(input)

        assertEquals("0,15,30,45 9,12,18 1,10,20 1,6,12 MON,WED,FRI", result)
    }

    @Test
    fun `build ignores seconds field value when includeSeconds is false`() {
        val input = CronInput(
            includeSeconds = false,
            seconds = "59",
            minutes = "0",
            hours = "12",
            dayOfMonth = "*",
            month = "*",
            dayOfWeek = "*"
        )

        val result = CronExpressionBuilder.build(input)

        assertEquals("0 12 * * *", result)
    }

    @Test
    fun `build preserves special quartz-like symbols if supported by input`() {
        val input = CronInput(
            includeSeconds = true,
            seconds = "0",
            minutes = "0",
            hours = "12",
            dayOfMonth = "?",
            month = "*",
            dayOfWeek = "MON#2"
        )

        val result = CronExpressionBuilder.build(input)

        assertEquals("0 0 12 ? * MON#2", result)
    }

    @Test
    fun `build produces expected expression for daily schedule at midnight`() {
        val input = CronInput(
            includeSeconds = false,
            seconds = "0",
            minutes = "0",
            hours = "0",
            dayOfMonth = "*",
            month = "*",
            dayOfWeek = "*"
        )

        val result = CronExpressionBuilder.build(input)

        assertEquals("0 0 * * *", result)
    }

    @Test
    fun `build keeps explicit zero seconds when seconds are enabled`() {
        val input = CronInput(
            includeSeconds = true,
            seconds = "0",
            minutes = "5",
            hours = "10",
            dayOfMonth = "*",
            month = "*",
            dayOfWeek = "*"
        )

        val result = CronExpressionBuilder.build(input)

        assertEquals("0 5 10 * * *", result)
    }
}