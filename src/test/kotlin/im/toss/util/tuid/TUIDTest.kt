package im.toss.util.tuid

import im.toss.test.equalsTo
import im.toss.util.data.encoding.i62.I62
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureNanoTime

internal class TUIDTest {
    @Test
    fun test() {
        TUID("Uzzzzz15ftgFAbwd5I0hIej11STX").run {
            instant equalsTo Instant.parse("2869-12-18T01:36:31.999999999Z")
            datetime.withZoneSameInstant(ZoneId.of("Asia/Seoul")).toOffsetDateTime() equalsTo ZonedDateTime.parse("2869-12-18T10:36:31.999999999+09:00").toOffsetDateTime()
            fingerprint equalsTo 9722026020L
            random equalsTo 10319821
            sequence equalsTo 63
            type equalsTo 109463
        }
    }
    @Test
    fun `TUID length equals 28`() {
        TUID().value.length equalsTo 28
    }

    @Test
    fun `TUID runtime`() {
        TUID().fingerprint equalsTo TUIDGenerator.DEFAULT.fingerprint
    }

    @Test
    fun `TUID Version`() {
        TUID("1jqqUl05DsR6nLRW5ZmF6Cz01000").version equalsTo 1
        TUID("1jqqUl2BLneRnLRW5ZmF6Cz01000").version equalsTo 3
    }

    @TestFactory
    fun `typeIdentifier test`(): List<DynamicTest> {
        return listOf(
            0 to 0,
            1 to 1,
            100 to 100,
            1000 to 1000,
            5000 to 5000,
            25000 to 25000,
            55000 to 55000
        ).map { (typeIdentifier, expected) ->
            dynamicTest("if typeIdentifier is $typeIdentifier, expect $expected") {
                TUID(tuid_v1(typeIdentifier, Instant.now(), 0, 0, 0)).type equalsTo expected
            }
        }
    }

    @Test
    fun instantTest() {
        val timestamp = ZonedDateTime.parse("2020-06-28T12:51:17.000+09:00", DateTimeFormatter.ISO_ZONED_DATE_TIME)
        val instant = Instant.ofEpochSecond(timestamp.toEpochSecond(), 123_456_789)
        val tuidValue = tuid_v1(0, instant, 999, 0, 1234)
        val tuid = TUID(tuidValue)
        tuid.instant equalsTo instant
    }

    @Test
    fun sequentialTest() {
        val ids = (0..1000).map {
            TUID(it)
        }

        ids.forEach { println(it) }

        ids equalsTo ids.sorted()
    }

    @Test
    fun toStringTest() {
        val timestamp = ZonedDateTime.parse("2020-06-28T12:51:17.000+09:00", DateTimeFormatter.ISO_ZONED_DATE_TIME)
        val instant = Instant.ofEpochSecond(timestamp.toEpochSecond(), 123_456_789)
        val tuidValue = tuid_v1(0, instant, 999, 9999, 1234)
        val tuid = TUID(tuidValue)
        tuid.toString() equalsTo "1jpOLd08M0kX0002bH000JuG7000"
    }

    @Test
    fun tuidPerformanceTest() {
        TUID() // warmup
        val totalNanoElapsed = AtomicLong()
        for(i in 1..1_000_000) {
            totalNanoElapsed.addAndGet(measureNanoTime { tuid() })
        }

        val millisElapsed = TimeUnit.NANOSECONDS.toMillis(totalNanoElapsed.get())
        println("generate tuid $millisElapsed ms elapsed in 1,000,000 times")
    }
}