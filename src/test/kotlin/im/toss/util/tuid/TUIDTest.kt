package im.toss.util.tuid

import im.toss.test.equalsTo
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

internal class TUIDTest {
    @Test
    fun test() {
        TUID("1jpaGA0mOSnUQPa6j1yG39IW9KTX").run {
            instant equalsTo ZonedDateTime.parse("2020-06-29T01:34:26.715094700+09:00").toInstant()
            datetime.toOffsetDateTime() equalsTo ZonedDateTime.parse("2020-06-29T01:34:26.715094700+09:00").toOffsetDateTime()
            runtime equalsTo 24197467695L
            random equalsTo -1595093560L
            count equalsTo 578
            type equalsTo 1831
        }
    }
    @Test
    fun `TUID length equals 28`() {
        TUID().value.length equalsTo 28
    }

    @Test
    fun `TUID runtime`() {
        TUID().runtime equalsTo TUIDGenerator.DEFAULT.runtimeFingerprint
    }

    @TestFactory
    fun `typeIdentifier test`(): List<DynamicTest> {
        return listOf(
            0 to 0,
            1 to 1,
            100 to 100,
            1000 to 1000,
            3000 to -844,
            5000 to 1156 // 0~3843
        ).map { (typeIdentifier, expected) ->
            dynamicTest("if typeIdentifier is $typeIdentifier, expect $expected") {
                TUID(tuid(typeIdentifier, Instant.now(), 0, 0, 0)).type equalsTo expected
            }
        }
    }

    @Test
    fun instantTest() {
        val timestamp = ZonedDateTime.parse("2020-06-28T12:51:17.000+09:00", DateTimeFormatter.ISO_ZONED_DATE_TIME)
        val instant = Instant.ofEpochSecond(timestamp.toEpochSecond(), 123_456_789)
        val tuidValue = tuid(0, instant, 999, 0, 1234)
        val tuid = TUID(tuidValue)
        tuid.instant equalsTo instant
    }

    @Test
    fun sequentialTest() {
        val ids = (0..1000).map {
            TUID(Random.nextInt())
        }

        ids.forEach { println(it) }

        ids equalsTo ids.sorted()
    }

    @Test
    fun toStringTest() {
        val timestamp = ZonedDateTime.parse("2020-06-28T12:51:17.000+09:00", DateTimeFormatter.ISO_ZONED_DATE_TIME)
        val instant = Instant.ofEpochSecond(timestamp.toEpochSecond(), 123_456_789)
        val tuidValue = tuid(0, instant, 999, 9999, 1234)
        val tuid = TUID(tuidValue)
        tuid.toString() equalsTo "1jpOLd08M0kX0002bH0000JuG700"
    }
}